package at.fhtw.mtg.dal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    // Thread-safe map to store token -> userId mappings.
    private static final Map<String, String> tokenUserMap = new ConcurrentHashMap<>();

    /**
     * Adds a login session.
     * @param token The generated token.
     * @param userId The user's unique ID.
     */
    public static void addLogin(String token, String userId) {
        tokenUserMap.put(token, userId);
    }

    /**
     * Checks if a token is valid (i.e., exists in the map).
     * @param token The token to check.
     * @return true if valid, false otherwise.
     */
    public static boolean isTokenValid(String token) {
        return tokenUserMap.containsKey(token);
    }

    /**
     * Retrieves the user ID associated with a token.
     * @param token The token.
     * @return The user ID, or null if token not found.
     */
    public static String getUserIdForToken(String token) {
        return tokenUserMap.get(token);
    }

    /**
     * Removes a token, e.g. on logout.
     * @param token The token to remove.
     */
    public static void removeToken(String token) {
        tokenUserMap.remove(token);
    }
}
