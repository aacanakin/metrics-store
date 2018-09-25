import config.Config;
import config.ConfigFactory;
import spark.utils.IOUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

class Migrate {
    public static void main(String[] args) {
        Config config;
        try {
            String configContent = IOUtils.toString(Application.class.getResourceAsStream("config.json"));
            config = ConfigFactory.create(configContent);
        } catch (IOException e) {
            System.err.println(e.toString());
            System.err.println("Could not read config file");
            return;
        }

        Connection db;
        try {
            db = DriverManager.getConnection(
                    config.getDb().getDriver(),
                    config.getDb().getUser(),
                    config.getDb().getPassword()
            );
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.err.println("Could not connect to db");
            return;
        }

        for (String key : config.getApps().keySet()) {
            // Create table if not exists
            final String table = config.getApps().get(key);
            final String[] dimensions = new String[]{
                    "\tid UUID DEFAULT uuid_generate_v4() NOT NULL",
                    "\tapi_key UUID NOT NULL",
                    "\ttimestamp TIMESTAMP NOT NULL",
                    "\tuser_id BIGINT NOT NULL",
                    "\tdata JSON",
                    "\tcreated_at TIMESTAMP DEFAULT timezone('utc', now()) NOT NULL",
            };

            final String[] clauses = new String[]{
                    String.format("CREATE TABLE IF NOT EXISTS %s (", table),
                    String.join(", \n", dimensions),
                    ");"
            };

            final String createTableSql = String.join("\n", clauses);
            final String createHyperTableSql = String.format(
                    "SELECT create_hypertable('%s', 'created_at', chunk_time_interval => interval '5 minutes', if_not_exists => TRUE);",
                    table
            );

            System.out.printf("Migrating %s...\n", table);

            System.out.println(createTableSql);
            System.out.println(createHyperTableSql);

            try {
                db.setAutoCommit(false);
                PreparedStatement createTableStatement = db.prepareStatement(createTableSql);
                PreparedStatement createHyperTableStatement = db.prepareStatement(createHyperTableSql);
                createTableStatement.executeUpdate();
                createHyperTableStatement.executeQuery();
                db.commit();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                System.err.println("Could not create table");
            }
        }
    }
}
