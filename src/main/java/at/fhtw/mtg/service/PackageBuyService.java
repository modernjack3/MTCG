package at.fhtw.mtg.service;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.mtg.dal.SessionManager;
import at.fhtw.mtg.dal.UserDAO;
import at.fhtw.mtg.dal.CardPackageDAO;
import at.fhtw.mtg.dal.CardDAO;
import at.fhtw.mtg.model.User;
import at.fhtw.mtg.model.CardPackage;
import at.fhtw.mtg.model.Card;
import at.fhtw.mtg.util.MtcgUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public class PackageBuyService extends BaseService {

    private ObjectMapper objectMapper = new ObjectMapper();
    private UserDAO userDAO = new UserDAO();
    private CardPackageDAO packageDAO = new CardPackageDAO();
    private CardDAO cardDAO = new CardDAO();

    @Override
    protected Response handlePost(Request request) {

        // Check that the URL path contains "packages"
        if (request.getPathParts().size() < 2 ||
                !request.getPathParts().get(1).equalsIgnoreCase("packages")) {
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"Invalid route for package purchase\"}");
        }

        String auth = MtcgUtils.getAuthFromRequest(request);

        Response invalid = MtcgUtils.invalidAuth(auth);
        if(invalid != null) {return invalid;}

        String userId = SessionManager.getUserIdForToken(MtcgUtils.extractTokenFromAuth(auth));
        if(userId == null) {return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"Invalid credentials\"}");}

        try {
            // Retrieve the user.
            User user = userDAO.getUserById(userId);
            if (user == null) {
                return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{\"error\":\"User not found\"}");
            }
            // Check if the user has at least 5 coins.
            if (user.getCoins() < 5) {
                return new Response(HttpStatus.FORBIDDEN, ContentType.JSON, "{\"error\":\"Not enough coins to buy a package\"}");
            }
            // Retrieve the next available (unsold) package.
            CardPackage cardPackage = packageDAO.getNextAvailablePackage();
            if (cardPackage == null) {
                return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{\"error\":\"No card package available\"}");
            }

            // Begin Transaction
            cardPackage.setBuyerId(user.getUserId());
            cardPackage.setSold(true);
            packageDAO.updateCardPackage(cardPackage);

            // Update ownership for each card in the package.
            List<Card> packageCards = cardPackage.getCards();
            if (packageCards == null || packageCards.isEmpty()) {
                return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"error\":\"Package has no cards\"}");
            }
            for (Card card : packageCards) {
                card.setOwnerId(user.getUserId());
                cardDAO.updateCard(card);
            }

            // Deduct 5 coins from the user.
            user.setCoins(user.getCoins() - 5);
            userDAO.updateUser(user);

            // Return the list of acquired cards.
            String json = objectMapper.writeValueAsString(packageCards);
            return new Response(HttpStatus.OK, ContentType.JSON, json);

        } catch (Exception e) {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
