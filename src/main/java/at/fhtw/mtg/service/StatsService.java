package at.fhtw.mtg.service;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.mtg.dal.SessionManager;
import at.fhtw.mtg.dal.UserDAO;
import at.fhtw.mtg.model.User;
import at.fhtw.mtg.util.MtcgUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class StatsService extends BaseService {

    private UserDAO userDAO = new UserDAO();
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * GET /stats
     * Retrieves the stats for the requesting user.
     * Expected to return a JSON object with the user's Name, Elo, Wins, and Losses.
     */
    @Override
    protected Response handleGet(Request request) {
        // Validate Authorization header.
        String auth = MtcgUtils.getAuthFromRequest(request);

        Response invalid = MtcgUtils.invalidAuth(auth);
        if(invalid != null) {return invalid;}

        String userId = SessionManager.getUserIdForToken(MtcgUtils.extractTokenFromAuth(auth));
        if(userId == null) {return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"Invalid credentials\"}");}


        try {
            // Retrieve user by username.
            User user = userDAO.getUserById(userId);
            if (user == null) {
                return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{\"error\":\"User not found\"}");
            }
            // Build a stats object.
            Map<String, Object> stats = new HashMap<>();
            stats.put("Name", user.getUsername());
            stats.put("Elo", user.getElo());
            stats.put("Wins", user.getWins());
            stats.put("Losses", user.getLosses());

            String json = objectMapper.writeValueAsString(stats);
            return new Response(HttpStatus.OK, ContentType.JSON, json);
        } catch (Exception e) {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
