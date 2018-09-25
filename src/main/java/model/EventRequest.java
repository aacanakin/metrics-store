package model;

import com.fasterxml.jackson.databind.ObjectMapper;
import config.Config;
import util.Errors;
import util.RequestKeys;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Map;

public class EventRequest implements Validator {

    private String table;
    private final String apiKey;
    private final BigDecimal userId;
    private final Timestamp timestamp;
    private String data;

    public EventRequest(final Config config, final Map<String, Object> body) throws Exception {

        this.validate(config, body);

        this.apiKey = body.get(RequestKeys.API_KEY).toString();
        this.table = config.getApps().get(this.apiKey);

        this.userId = new BigDecimal(body.get(RequestKeys.USER_ID).toString());

        int rawTimestamp = Integer.parseInt(body.get(RequestKeys.TIMESTAMP).toString());
        this.timestamp = new Timestamp((long) rawTimestamp * 1000);

        // Clean predefined keys from body to save as data field
        body.remove(RequestKeys.API_KEY);
        body.remove(RequestKeys.USER_ID);
        body.remove(RequestKeys.TIMESTAMP);

        ObjectMapper mapper = new ObjectMapper();
        this.data = mapper.writeValueAsString(body);
    }

    public String getTable() { return table; }

    public String getApiKey() {
        return apiKey;
    }

    public BigDecimal getUserId() {
        return userId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getData() {
        return data;
    }

    @Override
    public void validate(final Config config, final Map<String, Object> body) throws Exception {

        // Check required fields
        if (!body.containsKey(RequestKeys.API_KEY)) {
            throw new Exception(Errors.REQUIRED_API_KEY);
        }

        if (!body.containsKey(RequestKeys.USER_ID)) {
            throw new Exception(Errors.REQUIRED_USER_ID);
        }

        if (!body.containsKey(RequestKeys.TIMESTAMP)) {
            throw new Exception(Errors.REQUIRED_TIMESTAMP);
        }

        // Validate API Key
        if (!config.getApps().containsKey(body.get(RequestKeys.API_KEY).toString())) {
            throw new Exception(Errors.INVALID_API_KEY);
        }
    }
}
