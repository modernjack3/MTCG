package at.fhtw.mtg.dal;

import at.fhtw.mtg.model.Card;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CardDAO {

    // Create a new card
    public void createCard(Card card) throws SQLException {
        String sql = "INSERT INTO cards (card_id, name, damage, element_type, spell, owner_id, in_deck, locked) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, card.getCardId());
            ps.setString(2, card.getName());
            ps.setInt(3, card.getDamage());
            ps.setString(4, card.getElementType());
            ps.setBoolean(5, card.isSpell());
            ps.setString(6, card.getOwnerId());
            ps.setBoolean(7, card.isInDeck());
            ps.setBoolean(8, card.isLocked());
            ps.executeUpdate();
        }
    }

    // Retrieve a card by its ID
    public Card getCardById(String cardId) throws SQLException {
        String sql = "SELECT * FROM cards WHERE card_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cardId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Card card = new Card();
                    card.setCardId(rs.getString("card_id"));
                    card.setName(rs.getString("name"));
                    card.setDamage(rs.getInt("damage"));
                    card.setElementType(rs.getString("element_type"));
                    card.setSpell(rs.getBoolean("spell"));
                    card.setOwnerId(rs.getString("owner_id"));
                    card.setInDeck(rs.getBoolean("in_deck"));
                    card.setLocked(rs.getBoolean("locked"));
                    return card;
                }
            }
        }
        return null;
    }

    // Update an existing card
    public void updateCard(Card card) throws SQLException {
        String sql = "UPDATE cards SET name = ?, damage = ?, element_type = ?, spell = ?, owner_id = ?, in_deck = ?, locked = ? "
                + "WHERE card_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, card.getName());
            ps.setInt(2, card.getDamage());
            ps.setString(3, card.getElementType());
            ps.setBoolean(4, card.isSpell());
            ps.setString(5, card.getOwnerId());
            ps.setBoolean(6, card.isInDeck());
            ps.setBoolean(7, card.isLocked());
            ps.setString(8, card.getCardId());
            ps.executeUpdate();
        }
    }

    // Delete a card
    public void deleteCard(String cardId) throws SQLException {
        String sql = "DELETE FROM cards WHERE card_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cardId);
            ps.executeUpdate();
        }
    }

    // Get all cards for a specific user
    public List<Card> getCardsByUserId(String userId) throws SQLException {
        String sql = "SELECT * FROM cards WHERE owner_id = ?";
        List<Card> cards = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Card card = new Card();
                    card.setCardId(rs.getString("card_id"));
                    card.setName(rs.getString("name"));
                    card.setDamage(rs.getInt("damage"));
                    card.setElementType(rs.getString("element_type"));
                    card.setSpell(rs.getBoolean("spell"));
                    card.setOwnerId(rs.getString("owner_id"));
                    card.setInDeck(rs.getBoolean("in_deck"));
                    card.setLocked(rs.getBoolean("locked"));
                    cards.add(card);
                }
            }
        }
        return cards;
    }
}
