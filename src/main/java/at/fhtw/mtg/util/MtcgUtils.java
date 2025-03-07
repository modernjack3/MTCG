package at.fhtw.mtg.util;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;

public class MtcgUtils {

    // Extract username from token, e.g., "kienboec-mtcgToken" -> "kienboec"
    public static String extractTokenFromAuth(String auth) {
        return auth.substring("Bearer ".length()).trim();
    }

    // Checks if the Authentication is in a valid Format
    // Returns null if valid, returns Response to send if not
    public static Response invalidAuth(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{\"error\":\"Missing or invalid auth token\"}");
        }
        return null;
    }

    // Gets the Authorization Header Value
    public static String getAuthFromRequest(Request request) {
        return request.getHeaderMap().getHeader("Authorization");
    }
}
