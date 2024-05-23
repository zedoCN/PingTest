package top.zedo.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Config {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Config config;

    static {
        try {
            config = GSON.fromJson(Files.newBufferedReader(Path.of("./config.json")), Config.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> host;
    public int length;
    public int timeout;
    public float pingRate;
}
