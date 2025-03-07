package at.fhtw.mtg.dal;

import at.fhtw.mtg.model.Card;
import at.fhtw.mtg.model.CardPackage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CardPackageDAO {

    // Existing method: createCardPackage(CardPackage cardPackage)
    public void createCardPackage(CardPackage cardPackage) throws SQLException {
        String sqlPackage = "INSERT INTO packages (package_id, buyer_id, is_sold) VALUES (?, ?, ?)";
        String sqlPackageCards = "INSERT INTO package_cards (package_id, card_id) VALUES (?, ?)";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            // Begin transaction.
            conn.setAutoCommit(false);

            // Insert the package.
            try (PreparedStatement psPackage = conn.prepareStatement(sqlPackage)) {
                psPackage.setString(1, cardPackage.getPackageId());
                psPackage.setString(2, cardPackage.getBuyerId()); // likely null when created.
                psPackage.setBoolean(3, cardPackage.isSold());
                psPackage.executeUpdate();
            }

            // Insert all the card references.
            try (PreparedStatement psPackageCards = conn.prepareStatement(sqlPackageCards)) {
                for (Card card : cardPackage.getCards()) {
                    psPackageCards.setString(1, cardPackage.getPackageId());
                    psPackageCards.setString(2, card.getCardId());
                    psPackageCards.addBatch();
                }
                psPackageCards.executeBatch();
            }

            // Commit the transaction if all operations succeed.
            conn.commit();
        } catch (SQLException ex) {
            // Rollback if any operation fails.
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e) {
                    // Log or handle the rollback error as needed.
                    e.printStackTrace();
                }
            }
            throw ex;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Retrieves the next available (unsold) package along with its cards.
     * Returns null if no package is available.
     */
    public CardPackage getNextAvailablePackage() throws SQLException {
        String sql = "SELECT * FROM packages WHERE is_sold = FALSE ORDER BY created_at ASC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CardPackage cardPackage = new CardPackage();
                    cardPackage.setPackageId(rs.getString("package_id"));
                    cardPackage.setBuyerId(rs.getString("buyer_id")); // likely null
                    cardPackage.setSold(rs.getBoolean("is_sold"));

                    // Retrieve associated cards
                    List<Card> cards = getCardsForPackage(cardPackage.getPackageId(), conn);
                    cardPackage.setCards(cards);

                    return cardPackage;
                }
            }
        }
        return null;
    }

    /**
     * Updates the package information (e.g., marking it as sold and setting buyer_id).
     */
    public void updateCardPackage(CardPackage cardPackage) throws SQLException {
        String sql = "UPDATE packages SET buyer_id = ?, is_sold = ? WHERE package_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cardPackage.getBuyerId());
            ps.setBoolean(2, cardPackage.isSold());
            ps.setString(3, cardPackage.getPackageId());
            ps.executeUpdate();
        }
    }

    /**
     * Helper method to retrieve all cards associated with a package.
     */
    private List<Card> getCardsForPackage(String packageId, Connection conn) throws SQLException {
        List<Card> cards = new ArrayList<>();
        String sql = "SELECT card_id FROM package_cards WHERE package_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, packageId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String cardId = rs.getString("card_id");
                    // Retrieve full card details from the cards table
                    Card card = getCardById(cardId, conn);
                    if (card != null) {
                        cards.add(card);
                    }
                }
            }
        }
        return cards;
    }

    /**
     * Helper method to retrieve a Card by its ID using the provided connection.
     */
    private Card getCardById(String cardId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM cards WHERE card_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
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
}
