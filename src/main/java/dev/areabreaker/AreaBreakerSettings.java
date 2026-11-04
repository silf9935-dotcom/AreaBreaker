package dev.areabreaker;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class AreaBreakerSettings {
    public enum Mode { AREA, VEIN }
    public enum Shape { SQUARE, WALL, STRIP, TUNNEL }

    private static final Path CONFIG = FabricLoader.getInstance().getConfigDir().resolve("areabreaker.properties");
    private static int areaSize = 3;
    private static Mode mode = Mode.AREA;
    private static Shape shape = Shape.SQUARE;
    private static boolean durabilityProtection = true;
    private static boolean autoPickup = true;

    private AreaBreakerSettings() {}

    public static void load() {
        if (!Files.exists(CONFIG)) return;
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(CONFIG)) {
            properties.load(input);
            int savedSize = Integer.parseInt(properties.getProperty("areaSize", "3"));
            if (savedSize == 1 || savedSize == 2 || savedSize == 3 || savedSize == 4 || savedSize == 5 || savedSize == 10) {
                areaSize = savedSize;
            }
            mode = Mode.valueOf(properties.getProperty("mode", "AREA"));
            shape = Shape.valueOf(properties.getProperty("shape", "SQUARE"));
            durabilityProtection = Boolean.parseBoolean(properties.getProperty("durabilityProtection", "true"));
            autoPickup = Boolean.parseBoolean(properties.getProperty("autoPickup", "true"));
        } catch (IOException | IllegalArgumentException ignored) {
            // Keep safe defaults if the config is missing or malformed.
        }
    }

    private static void save() {
        Properties properties = new Properties();
        properties.setProperty("areaSize", Integer.toString(areaSize));
        properties.setProperty("mode", mode.name());
        properties.setProperty("shape", shape.name());
        properties.setProperty("durabilityProtection", Boolean.toString(durabilityProtection));
        properties.setProperty("autoPickup", Boolean.toString(autoPickup));
        try {
            Files.createDirectories(CONFIG.getParent());
            try (OutputStream output = Files.newOutputStream(CONFIG)) {
                properties.store(output, "AreaBreaker client settings");
            }
        } catch (IOException ignored) {
            // The mod remains usable even if the config cannot be written.
        }
    }

    public static int getAreaSize() { return areaSize; }

    public static void setAreaSize(int size) {
        if (size == 1 || size == 2 || size == 3 || size == 4 || size == 5 || size == 10) {
            areaSize = size;
            save();
        }
    }

    public static Mode getMode() { return mode; }
    public static void cycleMode() {
        mode = mode == Mode.AREA ? Mode.VEIN : Mode.AREA;
        save();
    }

    public static Shape getShape() { return shape; }
    public static void cycleShape() {
        shape = Shape.values()[(shape.ordinal() + 1) % Shape.values().length];
        save();
    }

    public static boolean isDurabilityProtectionEnabled() { return durabilityProtection; }
    public static void toggleDurabilityProtection() {
        durabilityProtection = !durabilityProtection;
        save();
    }

    public static boolean isAutoPickupEnabled() { return autoPickup; }
    public static void toggleAutoPickup() {
        autoPickup = !autoPickup;
        save();
    }
}
