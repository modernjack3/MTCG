package at.fhtw.mtg.service;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.mtg.dal.SessionManager;
import at.fhtw.mtg.dal.UserDAO;
import at.fhtw.mtg.model.User;
import at.fhtw.mtg.model.UserStats;
import at.fhtw.mtg.util.MtcgUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

public class ScoreboardService extends BaseService {

    private UserDAO userDAO = new UserDAO();
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * GET /scoreboard
     * Retrieves a sorted list of user stats ordered by ELO (highest first).
     * Exposes only: Name, Elo, Wins, Losses, TotalGames, and WinRate.
     */
    @Override
    protected Response handleGet(Request request) {

        String auth = MtcgUtils.getAuthFromRequest(request);

        Response invalid = MtcgUtils.invalidAuth(auth);
        if(invalid != null) {return invalid;}

        String userId = SessionManager.getUserIdForToken(MtcgUtils.extractTokenFromAuth(auth));
        if(userId == null) {return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"Invalid credentials\"}");}

        try {
            // Retrieve the list of users sorted by Elo descending.
            List<User> users = userDAO.getAllUsersSortedByElo();
            if (users == null || users.isEmpty()) {
                return new Response(HttpStatus.NO_CONTENT, ContentType.JSON, "[]");
            }
            // Map each User to UserStats.
            List<UserStats> statsList = new ArrayList<>();
            for (User user : users) {
                UserStats usr = getUserStats(user);
                statsList.add(usr);
            }
            String json = objectMapper.writeValueAsString(statsList);
            return new Response(HttpStatus.OK, ContentType.JSON, json);
        } catch (Exception e) {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private static UserStats getUserStats(User user) {
        UserStats usr = new UserStats();
        usr.setUsername(user.getUsername());
        usr.setElo(user.getElo());
        usr.setWins(user.getWins());
        usr.setLosses(user.getLosses());
        // Compute total games as the sum of wins and losses.
        int totalGames = user.getWins() + user.getLosses();
        usr.setTotalGames(totalGames);
        // Compute winrate, ensuring division by zero is handled.
        double winRate = totalGames > 0 ? ((double) user.getWins() / totalGames) * 100 : 0;
        usr.setWinRate(winRate);
        return usr;
    }
}
