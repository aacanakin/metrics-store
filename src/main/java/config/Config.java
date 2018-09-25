package config;

import java.util.Map;

public class Config {

    private Http http;
    private Database db;
    private Map<String, String> apps;
    private Prometheus prometheus;

    public Http getHttp() {
        return http;
    }

    public Prometheus getPrometheus() {
        return prometheus;
    }

    public Database getDb() {
        return db;
    }

    public Map<String, String> getApps() {
        return apps;
    }

    // Inner class definitions for config
    public class Database {
        private String driver;
        private String user;
        private String password;

        public String getDriver() {
            return driver;
        }

        public String getUser() {
            return user;
        }

        public String getPassword() {
            return password;
        }
    }

    public class Http {
        private String host;
        private int port;

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }
    }

    public class Prometheus {
        private int port;

        public int getPort() {
            return port;
        }
    }
}
