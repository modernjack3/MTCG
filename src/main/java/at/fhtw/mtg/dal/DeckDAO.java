package at.fhtw.mtg.dal;

import at.fhtw.mtg.model.Deck;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DeckDAO {

    // Retrieve the deck for a specific user
    public Deck getDeckByUserId(String userId) throws SQLException {
        String sql = "SELECT * FROM decks WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Deck deck = new Deck();
                    deck.setUserId(rs.getString("user_id"));
                    deck.setCard1Id(rs.getString("card1_id"));
                    deck.setCard2Id(rs.getString("card2_id"));
                    deck.setCard3Id(rs.getString("card3_id"));
                    deck.setCard4Id(rs.getString("card4_id"));
                    return deck;
                }
            }
        }
        return null;
    }

    // Update or create an existing deck
    public void upsertDeck(Deck deck) throws SQLException {

        //Upsert (Insert or Update if Deck already exists)
        String sql = "INSERT INTO decks (user_id, card1_id, card2_id, card3_id, card4_id) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON CONFLICT (user_id) DO UPDATE SET " +
                "card1_id = EXCLUDED.card1_id, " +
                "card2_id = EXCLUDED.card2_id, " +
                "card3_id = EXCLUDED.card3_id, " +
                "card4_id = EXCLUDED.card4_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, deck.getUserId());
            ps.setString(2, deck.getCard1Id());
            ps.setString(3, deck.getCard2Id());
            ps.setString(4, deck.getCard3Id());
            ps.setString(5, deck.getCard4Id());

            ps.executeUpdate();
        }
    }
}
