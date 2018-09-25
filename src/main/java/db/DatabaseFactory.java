package db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import config.Config;

public class DatabaseFactory {

    // These are the default connection pool settings for Hikari
    private static final String CACHE_PREP_STMTS = "true";
    private static final String PREP_STMT_CACHE_SIZE = "250";
    private static final String PREP_STMT_CACHE_SQL_LIMIT = "2048";

    public static HikariDataSource create(Config config) {
        HikariConfig dbConfig = new HikariConfig();
        dbConfig.setJdbcUrl(config.getDb().getDriver());
        dbConfig.setUsername(config.getDb().getUser());
        dbConfig.setPassword(config.getDb().getPassword());
        dbConfig.addDataSourceProperty("cachePrepStmts", CACHE_PREP_STMTS);
        dbConfig.addDataSourceProperty("prepStmtCacheSize", PREP_STMT_CACHE_SIZE);
        dbConfig.addDataSourceProperty("prepStmtCacheSqlLimit", PREP_STMT_CACHE_SQL_LIMIT);
        return new HikariDataSource(dbConfig);
    }
}
