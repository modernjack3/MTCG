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

import java.util.UUID;

public class UserService extends BaseService {

    private UserDAO userDAO = new UserDAO();
    private ObjectMapper objectMapper = new ObjectMapper();

    // POST /users (registration) remains open.
    @Override
    protected Response handlePost(Request request) {
        if (request.getPathParts().size() == 1) { // route: /users
            try {
                // Parse a full User object from request body and generate a new UUID for the user.
                User user = objectMapper.readValue(request.getBody(), User.class);
                user.setUserId(UUID.randomUUID().toString());

                if (userDAO.getUserByUsername(user.getUsername()) != null) {
                    return new Response(HttpStatus.CONFLICT, ContentType.JSON, "{\"error\":\"User already exists\"}");
                }
                userDAO.createUser(user);
                return new Response(HttpStatus.CREATED, ContentType.JSON, "{\"message\":\"User successfully created\"}");
            } catch (Exception e) {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
        return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"Invalid route for POST /users\"}");
    }

    // GET /users/{username} - only allow authenticated users to view their own data.
    @Override
    protected Response handleGet(Request request) {
        if (request.getPathParts().size() == 2) { // route: /users/{username}
            String auth = MtcgUtils.getAuthFromRequest(request);

            Response invalid = MtcgUtils.invalidAuth(auth);
            if(invalid != null) {return invalid;}

            String userId = SessionManager.getUserIdForToken(MtcgUtils.extractTokenFromAuth(auth));
            if(userId == null) {return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"Invalid credentials\"}");}

            try {
                User user = userDAO.getUserById(userId);
                if (user == null) {
                    return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{\"error\":\"User not found\"}");
                }
                if(!user.getUsername().equals(request.getPathParts().get(1))) {return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"Invalid credentials\"}");}
                String json = objectMapper.writeValueAsString(user);
                return new Response(HttpStatus.OK, ContentType.JSON, json);
            } catch (Exception e) {
                return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
        return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"Invalid route for GET /users\"}");
    }

    // PUT /users/{username} - only allow authenticated users to update their own data.
    @Override
    protected Response handlePut(Request request) {
        if (request.getPathParts().size() == 2) { // route: /users/{username}
            String auth = MtcgUtils.getAuthFromRequest(request);

            Response invalid = MtcgUtils.invalidAuth(auth);
            if(invalid != null) {return invalid;}

            String userId = SessionManager.getUserIdForToken(MtcgUtils.extractTokenFromAuth(auth));
            if(userId == null) {return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"Invalid credentials\"}");}

            try {
                // Expect only the fields to update
                User userUpdate = objectMapper.readValue(request.getBody(), User.class);
                User existing = userDAO.getUserById(userId);
                if (existing == null) {
                    return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{\"error\":\"User not found\"}");
                }

                if(!existing.getUsername().equals(request.getPathParts().get(1))) {return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"Invalid credentials\"}");}
                existing.setName(userUpdate.getName());
                existing.setBio(userUpdate.getBio());
                existing.setImage(userUpdate.getImage());
                userDAO.updateUser(existing);
                return new Response(HttpStatus.OK, ContentType.JSON, "{\"message\":\"User successfully updated\"}");
            } catch (Exception e) {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
        return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"Invalid route for PUT /users\"}");
    }
}
