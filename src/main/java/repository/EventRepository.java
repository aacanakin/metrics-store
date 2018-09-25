package repository;

import com.zaxxer.hikari.HikariDataSource;
import model.EventRequest;
import org.postgresql.util.PGobject;

import java.sql.*;
import java.util.UUID;

public class EventRepository {

    private final HikariDataSource db;

    public EventRepository(HikariDataSource db) {
        this.db = db;
    }

    public void createEvent(EventRequest eventRequest) throws SQLException {

        final String[] clauses = new String[]{
                String.format(
                        "INSERT INTO %s(api_key, timestamp, user_id, data)", eventRequest.getTable()
                ),
                "VALUES(?, ?, ?, ?)"
        };

        final String sql = String.join("\n", clauses);

        // This is done for custom json field
        final PGobject extra = new PGobject();
        extra.setType("json");
        extra.setValue(eventRequest.getData());

        Connection connection = this.db.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setObject(1, UUID.fromString(eventRequest.getApiKey()));
        statement.setTimestamp(2, new Timestamp(eventRequest.getTimestamp().getTime()));
        statement.setBigDecimal(3, eventRequest.getUserId());
        statement.setObject(4, extra);
        statement.executeUpdate();

        connection.close();
    }
}
