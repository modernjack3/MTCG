package at.fhtw.mtg.service;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.http.Method;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.httpserver.server.Service;

public abstract class BaseService implements Service {

    @Override
    public Response handleRequest(Request request) {
        Method method = request.getMethod();
        switch (method) {
            case GET:
                return handleGet(request);
            case POST:
                return handlePost(request);
            case PUT:
                return handlePut(request);
            case DELETE:
                return handleDelete(request);
            default:
                return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"Unsupported method\"}");
        }
    }

    protected Response handleGet(Request request) {
        return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"GET not supported\"}");
    }

    protected Response handlePost(Request request) {
        return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"POST not supported\"}");
    }

    protected Response handlePut(Request request) {
        return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"PUT not supported\"}");
    }

    protected Response handleDelete(Request request) {
        return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{\"error\":\"DELETE not supported\"}");
    }
}
