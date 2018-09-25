import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import config.Config;
import config.ConfigFactory;
import controller.EventController;
import db.DatabaseFactory;
import io.prometheus.client.Histogram;
import io.prometheus.client.exporter.HTTPServer;
import model.ErrorResponse;
import model.EventRequest;
import org.eclipse.jetty.http.HttpStatus;
import repository.EventRepository;
import util.Attributes;
import util.Paths;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import static spark.Spark.*;

class Application {

    private static final int MAX_THREADS = 32;

    // response time buckets in seconds
    private static final double[] BUCKETS = new double[]{
            0.001, 0.005, 0.01, 0.02, 0.05, 0.1
    };

    private static final String HISTOGRAM_NAME = "requests_latency_seconds";
    private static final String HISTOGRAM_HELP = "requests_latency_seconds";
    private static final Histogram requestLatencyHistogram = Histogram
            .build()
            .buckets(BUCKETS)
            .name(HISTOGRAM_NAME)
            .help(HISTOGRAM_HELP)
            .register();

    public static void main(String[] args) {

        Config config;
        try {
            config = ConfigFactory.create();
        } catch (IOException | URISyntaxException e) {
            System.err.println(e.getMessage());
            System.err.println("Could not read config file");
            return;
        }

        // Create data source
        HikariDataSource db = DatabaseFactory.create(config);

        // Create event repository
        EventRepository eventRepository = new EventRepository(db);

        // Spin up prometheus server
        try {
            new HTTPServer(config.getPrometheus().getPort());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        // Configure spark
        port(config.getHttp().getPort());
        threadPool(MAX_THREADS);

        // Setup filters for request validation
        before(Paths.EVENTS, (request, response) -> {

            // Track response time
            Histogram.Timer requestTimer = requestLatencyHistogram.startTimer();
            request.attribute(Attributes.REQUEST_TIMER, requestTimer);

            // Convert request body to map
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> body = mapper.readValue(
                    request.body(),
                    new TypeReference<Map<String, Object>>() {
                    }
            );

            // Send proper error based on event request validation
            EventRequest eventRequest;
            try {
                eventRequest = new EventRequest(config, body);
            } catch (Exception e) {
                ErrorResponse error = new ErrorResponse(e.getMessage());
                halt(HttpStatus.BAD_REQUEST_400, mapper.writeValueAsString(error));
                return;
            }

            // Bind attributes for controller to use
            request.attribute(Attributes.EVENT_REPOSITORY, eventRepository);
            request.attribute(Attributes.EVENT_REQUEST, eventRequest);
        });

        // Setup routes
        post(Paths.EVENTS, "application/json", EventController.create);

        // Setup after filter to track response time
        after(Paths.EVENTS, (request, response) -> {
            // Randomly sleep after each event request
            long randomSleepInMillis = (long) (Math.random() * (100L));
            Thread.sleep(randomSleepInMillis);

            Histogram.Timer requestTimer = request.attribute(Attributes.REQUEST_TIMER);
            requestTimer.observeDuration();
        });

        // Register not found route
        get("*", (req, res) -> {
            res.type("application/json");
            res.status(HttpStatus.NOT_FOUND_404);
            return "";
        });

        // Wait for jetty server to spin
        awaitInitialization();
    }
}
