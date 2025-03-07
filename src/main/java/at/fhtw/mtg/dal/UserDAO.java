package at.fhtw.mtg.dal;

import at.fhtw.mtg.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // Create a new user in the database
    public void createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (user_id, username, password, coins, elo, games_played, wins, losses, name, bio, image) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getUserId());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPassword());
            ps.setInt(4, user.getCoins());
            ps.setInt(5, user.getElo());
            ps.setInt(6, user.getGamesPlayed());
            ps.setInt(7, user.getWins());
            ps.setInt(8, user.getLosses());
            ps.setString(9, user.getName());
            ps.setString(10, user.getBio());
            ps.setString(11, user.getImage());
            ps.executeUpdate();
        }
    }

    public User getUserByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getString("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setCoins(rs.getInt("coins"));
                    user.setElo(rs.getInt("elo"));
                    user.setGamesPlayed(rs.getInt("games_played"));
                    user.setWins(rs.getInt("wins"));
                    user.setLosses(rs.getInt("losses"));
                    user.setName(rs.getString("name"));
                    user.setBio(rs.getString("bio"));
                    user.setImage(rs.getString("image"));
                    return user;
                }
            }
        }
        return null;
    }

    // Retrieve a user by ID
    public User getUserById(String userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getString("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setCoins(rs.getInt("coins"));
                    user.setElo(rs.getInt("elo"));
                    user.setGamesPlayed(rs.getInt("games_played"));
                    user.setWins(rs.getInt("wins"));
                    user.setLosses(rs.getInt("losses"));
                    user.setName(rs.getString("name"));
                    user.setBio(rs.getString("bio"));
                    user.setImage(rs.getString("image"));
                    return user;
                }
            }
        }
        return null;
    }

    // Update an existing user
    public void updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET coins = ?, elo = ?, games_played = ?, wins = ?, losses = ?, name = ?, bio = ?, image = ? "
                + "WHERE user_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, user.getCoins());
            ps.setInt(2, user.getElo());
            ps.setInt(3, user.getGamesPlayed());
            ps.setInt(4, user.getWins());
            ps.setInt(5, user.getLosses());
            ps.setString(6, user.getName());
            ps.setString(7, user.getBio());
            ps.setString(8, user.getImage());
            ps.setString(9, user.getUserId());
            ps.executeUpdate();
        }
    }

    public List<User> getAllUsersSortedByElo() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY elo DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getString("user_id"));
                user.setUsername(rs.getString("username"));
                user.setName(rs.getString("name"));
                user.setBio(rs.getString("bio"));
                user.setImage(rs.getString("image"));
                user.setElo(rs.getInt("elo"));
                user.setWins(rs.getInt("wins"));
                user.setLosses(rs.getInt("losses"));
                users.add(user);
            }
        }
        return users;
    }


}
