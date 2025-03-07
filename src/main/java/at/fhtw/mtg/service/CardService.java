package at.fhtw.mtg.service;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.mtg.dal.CardDAO;
import at.fhtw.mtg.dal.SessionManager;
import at.fhtw.mtg.dal.UserDAO;
import at.fhtw.mtg.model.Card;
import at.fhtw.mtg.model.User;
import at.fhtw.mtg.util.MtcgUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public class CardService extends BaseService {

    private CardDAO cardDAO = new CardDAO();
    private UserDAO userDAO = new UserDAO();
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * GET /cards
     * Returns all cards for the authenticated user.
     */
    @Override
    protected Response handleGet(Request request) {
        String auth = MtcgUtils.getAuthFromRequest(request);

        Response invalid = MtcgUtils.invalidAuth(auth);
        if(invalid != null) {return invalid;}

        String userId = SessionManager.getUserIdForToken(MtcgUtils.extractTokenFromAuth(auth));
        if(userId == null) {return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"Invalid credentials\"}");}
        try {
            User user = userDAO.getUserById(userId);
            if (user == null) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"Missing or invalid auth token\"}");
            }
            List<Card> cards = cardDAO.getCardsByUserId(user.getUserId());
            if (cards == null || cards.isEmpty()) {
                return new Response(HttpStatus.NO_CONTENT, ContentType.JSON, "[]");
            }
            String json = objectMapper.writeValueAsString(cards);
            return new Response(HttpStatus.OK, ContentType.JSON, json);
        } catch (Exception e) {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

}
