package at.fhtw.mtg.service;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.mtg.dal.SessionManager;
import at.fhtw.mtg.dal.UserDAO;
import at.fhtw.mtg.model.User;
import at.fhtw.mtg.model.UserCredentials;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SessionService extends BaseService {

    private UserDAO userDAO = new UserDAO();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected Response handlePost(Request request) {
        try {
            UserCredentials credentials = objectMapper.readValue(request.getBody(), UserCredentials.class);
            User user = userDAO.getUserByUsername(credentials.getUsername());
            if (user == null) {
                return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{\"error\":\"User not found\"}");
            }
            if (!user.getPassword().equals(credentials.getPassword())) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"Invalid credentials\"}");
            }
            String token = credentials.getUsername() + "-mtcgToken";
            // Store Login in Memory
            SessionManager.addLogin(token, user.getUserId());
            return new Response(HttpStatus.OK, ContentType.JSON, "{\"token\":\"" + token + "\"}");
        } catch (Exception e) {
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
