package me.cutebow.snappytappy.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class STConfig {
    public static final STConfig INSTANCE = new STConfig();
    public boolean enabled = true;
    public boolean snappyEnabled = true;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static transient boolean stopHookRegistered = false;

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("snappy_tappy.json");
    }

    public static void load() {
        Path p = path();
        if (Files.exists(p)) {
            try {
                STConfig loaded = GSON.fromJson(Files.readString(p), STConfig.class);
                if (loaded != null) {
                    INSTANCE.enabled = loaded.enabled;
                    INSTANCE.snappyEnabled = loaded.snappyEnabled;
                }
            } catch (Exception ignored) {}
        } else {
            save();
        }
        registerStopSaveOnce();
    }

    public static void save() {
        try {
            Files.createDirectories(path().getParent());
            Files.writeString(path(), GSON.toJson(INSTANCE));
        } catch (IOException ignored) {}
    }

    private static void registerStopSaveOnce() {
        if (stopHookRegistered) return;
        stopHookRegistered = true;
        try {
            ClientLifecycleEvents.CLIENT_STOPPING.register(client -> STConfig.save());
        } catch (Throwable t) {
            try {
                Runtime.getRuntime().addShutdownHook(new Thread(STConfig::save, "snappy_tappy_save"));
            } catch (Throwable ignored) {}
        }
    }
}
