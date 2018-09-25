package config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;


public class ConfigFactory {

    public static Config create(String content) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(content, Config.class);
    }
}
