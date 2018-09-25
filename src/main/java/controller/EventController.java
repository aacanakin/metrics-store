package controller;

import model.EventRequest;
import org.eclipse.jetty.http.HttpStatus;
import repository.EventRepository;
import spark.Request;
import spark.Response;
import spark.Route;
import util.Attributes;
import util.Errors;

import java.sql.SQLException;

import static spark.Spark.halt;

public class EventController {
    public static final Route create = (Request request, Response response) -> {

        response.type("application/json");

        try {
            EventRepository eventRepository = request.attribute(Attributes.EVENT_REPOSITORY);
            EventRequest event = request.attribute(Attributes.EVENT_REQUEST);
            eventRepository.createEvent(event);
            response.status(HttpStatus.CREATED_201);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            halt(HttpStatus.INTERNAL_SERVER_ERROR_500, Errors.CREATE_EVENT);
        }

        return "";
    };
}
