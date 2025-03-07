package at.fhtw.mtg.service;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.mtg.dal.BattleDAO;
import at.fhtw.mtg.dal.BattleResultDAO;
import at.fhtw.mtg.dal.SessionManager;
import at.fhtw.mtg.model.BattleHistory;
import at.fhtw.mtg.util.MtcgUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public class HistoryService extends BaseService {

    private BattleResultDAO battleResultDAO = new BattleResultDAO();
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * GET /history
     * Returns up to 10 most recent battles (from most recent to least recent) in which the authenticated user participated.
     * Each entry contains:
     *   - battleId
     *   - outcome ("Win" or "Loss")
     *   - opponentUserId (the other player)
     *   - eloChange (for the authenticated user)
     *   - createdAt timestamp.
     */
    @Override
    protected Response handleGet(Request request) {
        String auth = MtcgUtils.getAuthFromRequest(request);

        Response invalid = MtcgUtils.invalidAuth(auth);
        if(invalid != null) {return invalid;}

        String userId = SessionManager.getUserIdForToken(MtcgUtils.extractTokenFromAuth(auth));
        if(userId == null) {return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"Invalid credentials\"}");}

        try {
            List<BattleHistory> history = battleResultDAO.getBattleHistoryForUser(userId);
            if(history == null || history.isEmpty()) {
                return new Response(HttpStatus.NO_CONTENT, ContentType.JSON, "[]");
            }
            String json = objectMapper.writeValueAsString(history);
            return new Response(HttpStatus.OK, ContentType.JSON, json);
        } catch(Exception e) {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
