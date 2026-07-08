package com.farmmanager.config;

import com.farmmanager.FarmManagerMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FarmConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("farmrealm.json");
    private static FarmConfig INSTANCE;

    private String dimension_access = "open";

    public static FarmConfig get() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    public static void reload() {
        INSTANCE = load();
    }

    private static FarmConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String content = Files.readString(CONFIG_PATH);
                FarmConfig config = GSON.fromJson(content, FarmConfig.class);
                if (config == null) {
                    config = new FarmConfig();
                }
                return config;
            } catch (Exception e) {
                FarmManagerMod.LOGGER.error("Failed to load config, using defaults", e);
            }
        }
        FarmConfig config = new FarmConfig();
        config.save();
        return config;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (IOException e) {
            FarmManagerMod.LOGGER.error("Failed to save config", e);
        }
    }

    public boolean isAccessLocked() {
        return "locked".equals(dimension_access);
    }

    public void setAccess(String access) {
        this.dimension_access = access;
        save();
    }

    public String getAccess() {
        return dimension_access;
    }
}
