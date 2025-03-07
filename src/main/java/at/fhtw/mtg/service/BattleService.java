package at.fhtw.mtg.service;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.mtg.dal.*;
import at.fhtw.mtg.model.User;
import at.fhtw.mtg.model.Deck;
import at.fhtw.mtg.model.Card;
import at.fhtw.mtg.util.BattleLogic;
import at.fhtw.mtg.model.BattleResult;
import at.fhtw.mtg.util.MtcgUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class BattleService extends BaseService {

    private static final Object lock = new Object();
    private static BattleMatch currentMatch = null;

    private UserDAO userDAO = new UserDAO();
    private DeckDAO deckDAO = new DeckDAO();
    private BattleResultDAO battleResultDAO = new BattleResultDAO();


    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
    public void setDeckDAO(DeckDAO deckDAO) {
        this.deckDAO = deckDAO;
    }
    public void setBattleResultDAO(BattleResultDAO battleResultDAO) {
        this.battleResultDAO = battleResultDAO;
    }

    /**
     * Handles POST /battles.
     * - If no one is waiting, the caller is placed in the waiting slot.
     * - If the same user calls again, they are informed they're still waiting or receive the result.
     * - If a different user is waiting, retrieves both players’ actual decks, simulates the battle,
     *   persists the battle result, and returns the battle log.
     * - Prevents a user from battling themselves.
     */
    @Override
    protected Response handlePost(Request request) {
        String auth = MtcgUtils.getAuthFromRequest(request);

        Response invalid = MtcgUtils.invalidAuth(auth);
        if(invalid != null) {return invalid;}

        String userId = SessionManager.getUserIdForToken(MtcgUtils.extractTokenFromAuth(auth));
        if(userId == null) {return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"Invalid credentials\"}");}

        try {
            User user = userDAO.getUserById(userId);
            if(user == null) {
                return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{\"error\":\"User not found\"}");
            }
            String callerUserId = user.getUserId();

            synchronized(lock) {
                // Check persistent storage for unpolled battle result for the caller.
                BattleResult persistedResult = battleResultDAO.getUnpolledBattleResult(callerUserId);
                if(persistedResult != null) {
                    battleResultDAO.markBattleResultAsPolled(persistedResult.getBattleId());
                    return new Response(HttpStatus.OK, ContentType.PLAIN_TEXT, persistedResult.getBattleLog());
                }


                // If the caller is already in the waiting slot.
                if(currentMatch != null && currentMatch.waitingUserId.equals(callerUserId)) {
                    return new Response(HttpStatus.OK, ContentType.JSON, "{\"message\":\"Still waiting for opponent\"}");
                }

                if(currentMatch == null) {
                    // No waiting player, queue the caller.
                    currentMatch = new BattleMatch();
                    currentMatch.waitingUserId = callerUserId;
                    currentMatch.battleResult = null;
                    return new Response(HttpStatus.OK, ContentType.JSON, "{\"message\":\"Waiting for opponent\"}");
                } else {
                    // ----- Actually simulate the fight -----
                    // Retrieve both players' decks.
                    Deck waitingDeck = deckDAO.getDeckByUserId(currentMatch.waitingUserId);
                    Deck callerDeck = deckDAO.getDeckByUserId(callerUserId);
                    if(waitingDeck == null || callerDeck == null) {
                        currentMatch = null;
                        return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"One or both players do not have a configured deck.\"}");
                    }
                    List<Card> deckWaiting = BattleLogic.getCardsFromDeck(waitingDeck);
                    List<Card> deckCaller = BattleLogic.getCardsFromDeck(callerDeck);

                    if(deckWaiting.isEmpty() || deckCaller.isEmpty()) {
                        currentMatch = null;
                        return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"One or both players have an empty deck.\"}");
                    }

                    // Simulate the battle.
                    BattleResult result = BattleLogic.simulateBattle(deckWaiting, deckCaller, currentMatch.waitingUserId, callerUserId);
                    // Generate a battle ID.
                    result.setBattleId(UUID.randomUUID().toString());
                    // Persist the result.
                    battleResultDAO.createBattleResult(result);
                    // Adjust the User Stats
                    adjustUserStats(result);
                    // Clear the waiting slot.
                    currentMatch = null;
                    return new Response(HttpStatus.OK, ContentType.PLAIN_TEXT, result.getBattleLog());
                }
            }
        } catch(Exception e) {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // Update the Userstats according to the result
    public static void adjustUserStats(BattleResult result){
        UserDAO userDAO = new UserDAO();

        try{
            User waiting = userDAO.getUserById(result.getWaitingUserId());
            User opponent = userDAO.getUserById(result.getOpponentUserId());

            waiting.setGamesPlayed(waiting.getGamesPlayed() + 1);
            waiting.setElo(waiting.getElo() + result.getWaitingEloChange());

            opponent.setGamesPlayed(opponent.getGamesPlayed() + 1);
            opponent.setElo(opponent.getElo() + result.getOpponentEloChange());


            // Opponent won
            if(result.getWinnerUserId().equals(result.getOpponentUserId())){
                waiting.setLosses(waiting.getLosses() + 1);
                opponent.setWins(opponent.getWins() + 1);
            }

            // Waiting Player won
            if(result.getWinnerUserId().equals(result.getWaitingUserId())){
                opponent.setLosses(opponent.getLosses() + 1);
                waiting.setWins(waiting.getWins() + 1);
            }

            userDAO.updateUser(waiting);
            userDAO.updateUser(opponent);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Internal class to manage a waiting match.
     */
    private static class BattleMatch {
        String waitingUserId;
        BattleResult battleResult;
    }
}
