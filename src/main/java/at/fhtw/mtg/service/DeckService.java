package at.fhtw.mtg.service;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.mtg.dal.DeckDAO;
import at.fhtw.mtg.dal.CardDAO;
import at.fhtw.mtg.dal.SessionManager;
import at.fhtw.mtg.dal.UserDAO;
import at.fhtw.mtg.model.Deck;
import at.fhtw.mtg.model.Card;
import at.fhtw.mtg.model.User;
import at.fhtw.mtg.util.MtcgUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

public class DeckService extends BaseService {

    private DeckDAO deckDAO = new DeckDAO();
    private CardDAO cardDAO = new CardDAO();
    private UserDAO userDAO = new UserDAO();
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * GET /deck
     * If a query parameter "format=plain" is provided, returns a plain text representation.
     * Otherwise, returns JSON with full card details.
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
            if(user == null) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"User not found\"}");
            }
            Deck deck = deckDAO.getDeckByUserId(user.getUserId());
            if(deck == null) {
                return new Response(HttpStatus.NO_CONTENT, ContentType.JSON, "[]");
            }
            // Retrieve full card details using CardDAO.
            List<Card> cards = new ArrayList<>();
            if(deck.getCard1Id() != null) {
                Card c = cardDAO.getCardById(deck.getCard1Id());
                if(c != null) cards.add(c);
            }
            if(deck.getCard2Id() != null) {
                Card c = cardDAO.getCardById(deck.getCard2Id());
                if(c != null) cards.add(c);
            }
            if(deck.getCard3Id() != null) {
                Card c = cardDAO.getCardById(deck.getCard3Id());
                if(c != null) cards.add(c);
            }
            if(deck.getCard4Id() != null) {
                Card c = cardDAO.getCardById(deck.getCard4Id());
                if(c != null) cards.add(c);
            }

            // Check query parameter for format.
            String params = request.getParams(); // e.g. "format=plain"
            if(params != null && params.toLowerCase().contains("format=plain")) {
                // Build a plain text representation.
                StringBuilder plainText = new StringBuilder();
                for(Card card : cards) {
                    plainText.append(card.toString() + "\n");
                }

                return new Response(HttpStatus.OK, ContentType.PLAIN_TEXT, plainText.toString());
            } else {
                String json = objectMapper.writeValueAsString(cards);
                return new Response(HttpStatus.OK, ContentType.JSON, json);
            }
        } catch(Exception e) {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * PUT /deck
     * Expects a JSON array of exactly 4 unique card IDs.
     * Updates the deck for the authenticated user.
     */
    @Override
    protected Response handlePut(Request request) {
        String auth = MtcgUtils.getAuthFromRequest(request);

        Response invalid = MtcgUtils.invalidAuth(auth);
        if(invalid != null) {return invalid;}

        String userId = SessionManager.getUserIdForToken(MtcgUtils.extractTokenFromAuth(auth));
        if(userId == null) {return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"Invalid credentials\"}");}

        try {
            // Parse JSON array of card IDs.
            String[] cardIds = objectMapper.readValue(request.getBody(), String[].class);
            if(cardIds.length != 4) {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"Exactly 4 card IDs required\"}");
            }
            // Check for uniqueness.
            if(cardIds[0].equals(cardIds[1]) || cardIds[0].equals(cardIds[2]) || cardIds[0].equals(cardIds[3]) ||
                    cardIds[1].equals(cardIds[2]) || cardIds[1].equals(cardIds[3]) || cardIds[2].equals(cardIds[3])) {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"Card IDs must be unique\"}");
            }

            // Validate that each card belongs to the user and is not locked from trading
            User user = userDAO.getUserById(userId);
            if(user == null) {return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"User not found\"}");}

            for (String cardId : cardIds) {
                Card card = cardDAO.getCardById(cardId);
                if (card == null) {
                    return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"Card with ID " + cardId + " does not exist\"}");
                }
                if (!card.getOwnerId().equals(user.getUserId())) {
                    return new Response(HttpStatus.FORBIDDEN, ContentType.JSON, "{\"error\":\"Card with ID " + cardId + " does not belong to the user\"}");
                }
                if (card.isLocked()) {
                    return new Response(HttpStatus.FORBIDDEN, ContentType.JSON, "{\"error\":\"Card with ID " + cardId + " is locked from trading and cannot be used in the deck\"}");
                }
            }

            // Remove old cards from Deck so they become tradeable
            Deck remDeck = deckDAO.getDeckByUserId(userId);
            if(remDeck != null) {
                List<String> remCards = new ArrayList<>();
                remCards.add(remDeck.getCard1Id());
                remCards.add(remDeck.getCard2Id());
                remCards.add(remDeck.getCard3Id());
                remCards.add(remDeck.getCard4Id());
                for (String cardId : remCards) {
                    Card card = cardDAO.getCardById(cardId);
                    card.setInDeck(false);
                    cardDAO.updateCard(card);
                }
            }

            Deck deck = new Deck(user.getUserId(), cardIds[0], cardIds[1], cardIds[2], cardIds[3]);
            deckDAO.upsertDeck(deck);

            // Set the cards to inDeck to block them from trading
            for (String cardId : cardIds) {
                Card card = cardDAO.getCardById(cardId);
                card.setInDeck(true);
                cardDAO.updateCard(card);
            }
            return new Response(HttpStatus.OK, ContentType.JSON, "{\"message\":\"Deck successfully updated\"}");
        } catch(Exception e) {
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
