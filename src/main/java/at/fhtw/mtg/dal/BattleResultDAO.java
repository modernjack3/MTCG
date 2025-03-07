package at.fhtw.mtg.dal;

import at.fhtw.mtg.model.BattleHistory;
import at.fhtw.mtg.model.BattleResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BattleResultDAO {

    /**
     * Inserts a BattleResult into the battles table.
     *
     * @param battleResult The BattleResult object to insert.
     * @throws SQLException If a database access error occurs.
     */
    public void createBattleResult(BattleResult battleResult) throws SQLException {
        String sql = "INSERT INTO battles (battle_id, waiting_user_id, opponent_user_id, winner_user_id, " +
                "battle_log, waiting_elo_change, opponent_elo_change, polled) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, battleResult.getBattleId());
            ps.setString(2, battleResult.getWaitingUserId());
            ps.setString(3, battleResult.getOpponentUserId());
            ps.setString(4, battleResult.getWinnerUserId());
            ps.setString(5, battleResult.getBattleLog());
            ps.setInt(6, battleResult.getWaitingEloChange());
            ps.setInt(7, battleResult.getOpponentEloChange());
            // Always insert false initially for polled.
            ps.setBoolean(8, false);

            ps.executeUpdate();
        }
    }

    public BattleResult getUnpolledBattleResult(String waitingUserId) throws SQLException {
        String sql = "SELECT battle_id, waiting_user_id, opponent_user_id, winner_user_id, " +
                "battle_log, waiting_elo_change, opponent_elo_change, created_at " +
                "FROM battles " +
                "WHERE waiting_user_id = ? AND polled = FALSE " +
                "ORDER BY created_at DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, waitingUserId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BattleResult result = new BattleResult();
                    result.setBattleId(rs.getString("battle_id"));
                    result.setWaitingUserId(rs.getString("waiting_user_id"));
                    result.setOpponentUserId(rs.getString("opponent_user_id"));
                    result.setWinnerUserId(rs.getString("winner_user_id"));
                    result.setBattleLog(rs.getString("battle_log"));
                    result.setWaitingEloChange(rs.getInt("waiting_elo_change"));
                    result.setOpponentEloChange(rs.getInt("opponent_elo_change"));
                    result.setCreatedAt(rs.getTimestamp("created_at").toString());
                    return result;
                }
            }
        }
        return null;
    }

    public void markBattleResultAsPolled(String battleId) throws SQLException {
        String sql = "UPDATE battles SET polled = TRUE WHERE battle_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, battleId);
            ps.executeUpdate();
        }
    }

    public List<BattleHistory> getBattleHistoryForUser(String userId) throws SQLException {
        List<BattleHistory> history = new ArrayList<>();
        String sql = "SELECT * FROM battles WHERE waiting_user_id = ? OR opponent_user_id = ? ORDER BY created_at DESC LIMIT 10";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BattleHistory dto = new BattleHistory();
                    dto.setBattleId(rs.getString("battle_id"));
                    String waitingUserId = rs.getString("waiting_user_id");
                    String opponentUserId = rs.getString("opponent_user_id");
                    int waitingEloChange = rs.getInt("waiting_elo_change");
                    int opponentEloChange = rs.getInt("opponent_elo_change");

                    // Determine outcome from the perspective of the given user.
                    String winnerUserId = rs.getString("winner_user_id");
                    if (userId.equals(winnerUserId)) {
                        dto.setOutcome("Win");
                        // Determine Elo change depending on role.
                        if(userId.equals(waitingUserId)) {
                            dto.setEloChange(waitingEloChange);
                            dto.setOpponentUserId(opponentUserId);
                        } else {
                            dto.setEloChange(opponentEloChange);
                            dto.setOpponentUserId(waitingUserId);
                        }
                    } else {
                        dto.setOutcome("Loss");
                        if(userId.equals(waitingUserId)) {
                            dto.setEloChange(waitingEloChange);
                            dto.setOpponentUserId(opponentUserId);
                        } else {
                            dto.setEloChange(opponentEloChange);
                            dto.setOpponentUserId(waitingUserId);
                        }
                    }
                    dto.setCreatedAt(rs.getTimestamp("created_at").toString());
                    history.add(dto);
                }
            }
        }
        return history;
    }
}
