package at.fhtw.mtg.dal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

public class SessionManagerTest {

    @BeforeEach
    void clearSessions() throws Exception {
        // Clear the static tokenUserMap so each test starts empty
        Field field = SessionManager.class.getDeclaredField("tokenUserMap");
        field.setAccessible(true);
        Map<String, String> tokenUserMap = (Map<String, String>) field.get(null);
        tokenUserMap.clear();
    }

    @Test
    void testAddLoginAndValidity() {
        String token = "testToken-mtcgToken";
        String userId = "user-123";

        // Initially, the token should not be valid.
        assertFalse(SessionManager.isTokenValid(token), "Token should not be valid before adding.");

        // Add the login session.
        SessionManager.addLogin(token, userId);

        // The token should now be valid and return the proper user ID.
        assertTrue(SessionManager.isTokenValid(token), "Token should be valid after adding.");
        assertEquals(userId, SessionManager.getUserIdForToken(token), "User ID should match the one added.");
    }

    @Test
    void testRemoveToken() {
        String token = "sampleToken-mtcgToken";
        String userId = "user-456";

        // Add a session and verify it exists.
        SessionManager.addLogin(token, userId);
        assertTrue(SessionManager.isTokenValid(token));

        // Remove the token.
        SessionManager.removeToken(token);

        // The token should no longer be valid or retrievable.
        assertFalse(SessionManager.isTokenValid(token), "Token should be invalid after removal.");
        assertNull(SessionManager.getUserIdForToken(token), "Token lookup should return null after removal.");
    }

    @Test
    void testMultipleLogins() {
        String token1 = "token1-mtcgToken";
        String token2 = "token2-mtcgToken";
        String userId1 = "user1";
        String userId2 = "user2";

        // Add two sessions.
        SessionManager.addLogin(token1, userId1);
        SessionManager.addLogin(token2, userId2);

        // Verify that both tokens are valid and return the correct user IDs.
        assertTrue(SessionManager.isTokenValid(token1));
        assertTrue(SessionManager.isTokenValid(token2));
        assertEquals(userId1, SessionManager.getUserIdForToken(token1));
        assertEquals(userId2, SessionManager.getUserIdForToken(token2));
    }
}
