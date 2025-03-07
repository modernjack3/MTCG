package at.fhtw.mtg.service;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.mtg.dal.SessionManager;
import at.fhtw.mtg.dal.UserDAO;
import at.fhtw.mtg.model.Card;
import at.fhtw.mtg.model.CardPackage;
import at.fhtw.mtg.dal.CardPackageDAO;
import at.fhtw.mtg.dal.CardDAO;
import at.fhtw.mtg.model.User;
import at.fhtw.mtg.util.MtcgUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;

public class PackageCreationService extends BaseService {
    private ObjectMapper objectMapper = new ObjectMapper();
    private CardPackageDAO packageDAO = new CardPackageDAO();
    private CardDAO cardDAO = new CardDAO();
    private UserDAO userDAO = new UserDAO();

    @Override
    protected Response handlePost(Request request) {
        String auth = MtcgUtils.getAuthFromRequest(request);

        Response invalid = MtcgUtils.invalidAuth(auth);
        if(invalid != null) {return invalid;}

        String userId = SessionManager.getUserIdForToken(MtcgUtils.extractTokenFromAuth(auth));
        if(userId == null) {return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"Invalid credentials\"}");}

        try {
            // Check for admin authorization
            User admin = userDAO.getUserById(userId);
            if(!admin.getUsername().equals("admin")){return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"Invalid credentials\"}");}

            List<Card> cards = objectMapper.readValue(request.getBody(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Card.class));
            if (cards.size() != 5) {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"Exactly 5 cards required\"}");
            }

            // Insert into the cards table
            for (Card card : cards) {
                cardDAO.createCard(card);
            }

            // Create a new package
            String packageId = UUID.randomUUID().toString();
            CardPackage cardPackage = new CardPackage();
            cardPackage.setPackageId(packageId);
            cardPackage.setSold(false);
            cardPackage.setBuyerId(null);
            cardPackage.setCards(cards);
            packageDAO.createCardPackage(cardPackage);

            return new Response(HttpStatus.CREATED, ContentType.JSON, "{\"message\":\"Package and cards successfully created\"}");

        } catch (Exception e) {
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}

