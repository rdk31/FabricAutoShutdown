package net.rdk31.autoshutdown;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigManager {
    private final static String configPath = "./config/fabric-auto-shutdown.json";

    private static ConfigInstance generateDefaultConfig() {
        ConfigInstance configInstance = new ConfigInstance();

        String gson = new GsonBuilder().setPrettyPrinting().create().toJson(configInstance);
        File file = new File(configPath);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try(FileWriter writer = new FileWriter(file)) {
            writer.write(gson);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return configInstance;
    }

    private static ConfigInstance readConfig() {
        ConfigInstance configInstance;

        try {
            configInstance = new Gson().fromJson(new FileReader(configPath), ConfigInstance.class);
        } catch (Exception e){
            e.printStackTrace();
            configInstance = new ConfigInstance();
        }

        return configInstance;
    }

    public static ConfigInstance loadConfig() {
        if (Files.exists(Paths.get(configPath))) {
            return readConfig();
        } else {
            return generateDefaultConfig();
        }
    }
}
