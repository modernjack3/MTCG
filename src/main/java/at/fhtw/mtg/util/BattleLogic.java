package at.fhtw.mtg.util;

import at.fhtw.mtg.dal.CardDAO;
import at.fhtw.mtg.dal.UserDAO;
import at.fhtw.mtg.model.BattleResult;
import at.fhtw.mtg.model.Card;
import at.fhtw.mtg.model.Deck;
import at.fhtw.mtg.model.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BattleLogic {

    private static CardDAO cardDAO = new CardDAO();
    private static UserDAO userDAO = new UserDAO();

    /**
     * Calculates Winner of the round
     * P1 wins -> true
     * P2 wins -> false
     * Draw -> null
     */
    public static Boolean calculateRoundWinner(Card p1, Card p2, StringBuilder log) {

        if(winsByDefault(p1, p2, log)) {

            log.append(p1.getName()).append(" slays ").append(p2.getName()).append(" by special interaction!\n");
            return true;
        }
        if(winsByDefault(p2, p1, log)) {

            log.append(p2.getName()).append(" slays ").append(p1.getName()).append(" by special interaction!\n");
            return false;
        }

        // Apply the Elemental Modifiers
        Double p1Dmg = applyElemental(p1, p2, log);
        Double p2Dmg = applyElemental(p2, p1, log);

        if(p1Dmg.equals(p2Dmg)) {return null;}

        log.append(p1.getName()).append(" deals ").append(p1Dmg).append(" and ").append(p2.getName()).append(" deals ").append(p2Dmg).append(".\n");
        return p1Dmg > p2Dmg;
    }

    /**
     * Returns true if the attacker wins the round by a special rule
     */
    public static boolean winsByDefault(Card attacker, Card defender, StringBuilder log) {
        // Special rule: Goblins are too afraid of Dragons.
        if (attacker.getName().toLowerCase().contains("dragon") &&
                defender.getName().toLowerCase().contains("goblin")) {
            return true;
        }

        if (attacker.getName().toLowerCase().contains("wizard") &&
                defender.getName().toLowerCase().contains("ork")) {
            return true;
        }

        if (attacker.getName().toLowerCase().contains("spell") &&
                attacker.getElementType().equalsIgnoreCase("water") &&
                defender.getName().toLowerCase().contains("goblin")) {
            return true;
        }

        if (attacker.getName().toLowerCase().contains("kraken") &&
                defender.isSpell()) {
            return true;
        }

        if (attacker.getName().toLowerCase().contains("elf") &&
                attacker.getElementType().equalsIgnoreCase("fire") &&
                defender.getName().toLowerCase().contains("goblin")) {
            return true;
        }
        return false;
    }

    /**
     * Applies the Damage multiplicator for the attacker against the defender
     */
    public static double applyElemental(Card attacker, Card defender, StringBuilder log) {
        // If its a pure monster fight, ignore elements
        if(!attacker.isSpell() && !defender.isSpell()){
            return attacker.getDamage();
        }
        // Else apply the rules
        return switch (attacker.getElementType().toLowerCase()) {
            case "fire" -> switch (defender.getElementType().toLowerCase()) {
                case "fire" -> attacker.getDamage();
                case "water" -> attacker.getDamage() * 0.5;
                case "normal" -> attacker.getDamage() * 2;
                default -> 0;
            };
            case "water" -> switch (defender.getElementType().toLowerCase()) {
                case "fire" -> attacker.getDamage() * 2;
                case "water" -> attacker.getDamage();
                case "normal" -> attacker.getDamage() * 0.5;
                default -> 0;
            };
            case "normal" -> switch (defender.getElementType().toLowerCase()) {
                case "fire" -> attacker.getDamage() * 0.5;
                case "water" -> attacker.getDamage() * 2;
                case "normal" -> attacker.getDamage();
                default -> 0;
            };
            default -> 0;
        };
    }

    /**
     * Simulates a battle between two decks.
     * @param deck1 List of cards for user1.
     * @param deck2 List of cards for user2.
     * @param userId1 ID of user1.
     * @param userId2 ID of user2.
     * @return BattleResult with log, winner, elo changes, and rounds.
     */
    public static BattleResult simulateBattle(List<Card> deck1, List<Card> deck2, String userId1, String userId2) {
        BattleResult result = new BattleResult();
        result.setWaitingUserId(userId1);
        result.setOpponentUserId(userId2);

        String name1 = null;
        String name2 = null;
        User user1 = null;
        User user2 = null;

        try{
            user1 = userDAO.getUserById(userId1);
            user2 = userDAO.getUserById(userId2);
            name1 = user1.getName();
            name2 = user2.getName();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }

        StringBuilder log = new StringBuilder();
        Integer rounds = 0;
        int maxRounds = 100;
        Random rand = new Random();

        // Continue until one deck is empty or we reach max rounds.
        while (!deck1.isEmpty() && !deck2.isEmpty() && rounds < maxRounds) {
            rounds++;
            // Select a random card from each deck.
            Card card1 = deck1.get(rand.nextInt(deck1.size()));
            Card card2 = deck2.get(rand.nextInt(deck2.size()));

            log.append("Round <").append(rounds.toString()).append("> :\n");
            log.append(name1).append(" plays ").append(card1.toString()).append("\n");
            log.append(name2).append(" plays ").append(card2.toString()).append("\n");

            Boolean winner = calculateRoundWinner(card1, card2, log);

            String userWon = "Its a Draw! \n\n";
            if(winner != null) {
                if(winner) {
                    deck2.remove(card2);
                    deck1.add(card2);
                    userWon = name1 + " won the round! He takes " + card2.getName() + "\n\n";
                }
                else {
                    deck1.remove(card1);
                    deck2.add(card1);
                    userWon = name2 + " won the round! He takes " + card1.getName() + "\n\n";
                }
            }

            log.append(userWon);

        }
        if (deck1.isEmpty() && deck2.isEmpty()) {
            log.append("Battle ended in a draw after ").append(rounds).append(" rounds.\n");
            result.setWinnerUserId(null);
            result.setWaitingEloChange(0);
            result.setOpponentEloChange(0);
        } else if (deck1.isEmpty()) {
            log.append(name2).append(" wins the battle!\n");
            result.setWinnerUserId(userId2);
            result.setWaitingEloChange(calculateEloChange(user1, user2, false));
            result.setOpponentEloChange(calculateEloChange(user2, user1, true));
        } else {
            log.append(name1).append(" wins the battle!\n");
            result.setWinnerUserId(userId1);
            result.setWaitingEloChange(calculateEloChange(user1, user2, true));
            result.setOpponentEloChange(calculateEloChange(user2, user1, false));
        }
        result.setBattleLog(log.toString());
        return result;
    }

    public static List<Card> getCardsFromDeck(Deck deck) throws Exception {
        List<Card> cards = new ArrayList<>();
        if(deck.getCard1Id() != null && !deck.getCard1Id().isEmpty()) {
            Card c = cardDAO.getCardById(deck.getCard1Id());
            if(c != null) cards.add(c);
        }
        if(deck.getCard2Id() != null && !deck.getCard2Id().isEmpty()) {
            Card c = cardDAO.getCardById(deck.getCard2Id());
            if(c != null) cards.add(c);
        }
        if(deck.getCard3Id() != null && !deck.getCard3Id().isEmpty()) {
            Card c = cardDAO.getCardById(deck.getCard3Id());
            if(c != null) cards.add(c);
        }
        if(deck.getCard4Id() != null && !deck.getCard4Id().isEmpty()) {
            Card c = cardDAO.getCardById(deck.getCard4Id());
            if(c != null) cards.add(c);
        }
        return cards;
    }

    // Returns the elo adjustment for p1
    public static int calculateEloChange(User p1, User p2, boolean firstPlayerWon) {
        int totalGames1 = p1.getWins() + p1.getLosses();
        int K = (totalGames1 < 30) ? 32 : 16;

        // Standard expected score for p1.
        double expected1 = 1.0 / (1.0 + Math.pow(10, (p2.getElo() - p1.getElo()) / 400.0));
        double actual = firstPlayerWon ? 1.0 : 0.0;

        // Raw change without additional scaling.
        double rawChange = K * (actual - expected1);

        // If p1 loses, further scale the loss based on Elo difference.
        // If p1 is higher-rated than p2, the loss should be larger.
        // If p1 is lower-rated, the loss is less severe.
        if (!firstPlayerWon) {
            double scalingFactor = 1.0 + (p1.getElo() - p2.getElo()) / 400.0;
            rawChange *= scalingFactor;
        }

        int change = (int) Math.round(rawChange);

        // Clamp p1's new Elo to be at least 0.
        if (p1.getElo() + change < 0) {
            change = -p1.getElo();
        }

        return change;
    }
}
