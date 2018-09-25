package config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigFactory {

    private static final String PATH = "config.json";

    public static Config create() throws IOException, URISyntaxException {
        Path configPath = Paths.get(ClassLoader.getSystemResource(PATH).toURI());
        String content = new String(Files.readAllBytes(configPath));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(content, Config.class);
    }
}
