package dev.areabreaker;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public final class AreaBreakerSettings {
    public enum Mode { AREA, VEIN }
    public enum Shape { SQUARE, WALL, STRIP, TUNNEL }
    public enum FilterMode { OFF, BLACKLIST, WHITELIST }

    private static final Path CONFIG = FabricLoader.getInstance().getConfigDir().resolve("areabreaker.properties");
    private static int areaSize = 3;
    private static Mode mode = Mode.AREA;
    private static Shape shape = Shape.SQUARE;
    private static FilterMode filterMode = FilterMode.OFF;
    private static boolean enabled = true;
    private static boolean durabilityProtection = true;
    private static boolean autoPickup = true;
    private static boolean autoSmelt;
    private static long blocksMined;
    private static long oresMined;
    private static long miningXp;
    private static final Set<String> filterBlocks = new LinkedHashSet<>();
    private static long lastStatsSave;

    private AreaBreakerSettings() {}

    public static void load() {
        if (!Files.exists(CONFIG)) return;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(CONFIG)) {
            p.load(in);
            int savedSize = Integer.parseInt(p.getProperty("areaSize", "3"));
            if (savedSize == 1 || savedSize == 2 || savedSize == 3 || savedSize == 4 || savedSize == 5 || savedSize == 10) areaSize = savedSize;
            mode = Mode.valueOf(p.getProperty("mode", "AREA"));
            shape = Shape.valueOf(p.getProperty("shape", "SQUARE"));
            filterMode = FilterMode.valueOf(p.getProperty("filterMode", "OFF"));
            enabled = Boolean.parseBoolean(p.getProperty("enabled", "true"));
            durabilityProtection = Boolean.parseBoolean(p.getProperty("durabilityProtection", "true"));
            autoPickup = Boolean.parseBoolean(p.getProperty("autoPickup", "true"));
            autoSmelt = Boolean.parseBoolean(p.getProperty("autoSmelt", "false"));
            blocksMined = Long.parseLong(p.getProperty("blocksMined", "0"));
            oresMined = Long.parseLong(p.getProperty("oresMined", "0"));
            miningXp = Long.parseLong(p.getProperty("miningXp", "0"));
            String list = p.getProperty("filterBlocks", "");
            filterBlocks.clear();
            for (String id : list.split(",")) if (!id.isBlank()) filterBlocks.add(id.trim());
        } catch (IOException | IllegalArgumentException ignored) {
            // Keep safe defaults if the config is missing or malformed.
        }
    }

    private static void save() {
        Properties p = new Properties();
        p.setProperty("areaSize", Integer.toString(areaSize));
        p.setProperty("mode", mode.name());
        p.setProperty("shape", shape.name());
        p.setProperty("filterMode", filterMode.name());
        p.setProperty("enabled", Boolean.toString(enabled));
        p.setProperty("durabilityProtection", Boolean.toString(durabilityProtection));
        p.setProperty("autoPickup", Boolean.toString(autoPickup));
        p.setProperty("autoSmelt", Boolean.toString(autoSmelt));
        p.setProperty("blocksMined", Long.toString(blocksMined));
        p.setProperty("oresMined", Long.toString(oresMined));
        p.setProperty("miningXp", Long.toString(miningXp));
        p.setProperty("filterBlocks", filterBlocks.stream().collect(Collectors.joining(",")));
        try {
            Files.createDirectories(CONFIG.getParent());
            try (OutputStream out = Files.newOutputStream(CONFIG)) { p.store(out, "AreaBreaker 1.1.0 settings and miner profile"); }
        } catch (IOException ignored) {
            // The mod remains usable if the config cannot be written.
        }
    }

    public static int getAreaSize() { return areaSize; }
    public static void setAreaSize(int size) {
        if (size == 1 || size == 2 || size == 3 || size == 4 || size == 5 || size == 10) { areaSize = size; save(); }
    }
    public static Mode getMode() { return mode; }
    public static void cycleMode() { mode = mode == Mode.AREA ? Mode.VEIN : Mode.AREA; save(); }
    public static Shape getShape() { return shape; }
    public static void cycleShape() { shape = Shape.values()[(shape.ordinal() + 1) % Shape.values().length]; save(); }
    public static boolean isEnabled() { return enabled; }
    public static void toggleEnabled() { enabled = !enabled; save(); }
    public static boolean isDurabilityProtectionEnabled() { return durabilityProtection; }
    public static void toggleDurabilityProtection() { durabilityProtection = !durabilityProtection; save(); }
    public static boolean isAutoPickupEnabled() { return autoPickup; }
    public static void toggleAutoPickup() { autoPickup = !autoPickup; save(); }
    public static boolean isAutoSmeltEnabled() { return autoSmelt; }
    public static void toggleAutoSmelt() { autoSmelt = !autoSmelt; save(); }
    public static FilterMode getFilterMode() { return filterMode; }
    public static void cycleFilterMode() { filterMode = FilterMode.values()[(filterMode.ordinal() + 1) % FilterMode.values().length]; save(); }

    public static boolean addFilterBlock(String id) {
        if (id == null || id.isBlank()) return false;
        boolean added = filterBlocks.add(id);
        if (added) save();
        return added;
    }

    public static boolean isBlockAllowed(Block block) {
        if (filterMode == FilterMode.OFF) return true;
        String id = Registries.BLOCK.getId(block).toString();
        boolean listed = filterBlocks.contains(id);
        return filterMode == FilterMode.BLACKLIST ? !listed : listed;
    }

    public static void recordMined(Block block) {
        blocksMined++;
        String path = Registries.BLOCK.getId(block).getPath();
        boolean ore = path.contains("ore");
        if (ore) oresMined++;
        miningXp += ore ? 3 : 1;
        if (blocksMined % 25 == 0) save();
    }

    public static long getBlocksMined() { return blocksMined; }
    public static long getOresMined() { return oresMined; }
    public static long getMiningXp() { return miningXp; }
    public static int getMiningLevel() { return (int)Math.min(100, 1 + miningXp / 250); }
    public static long getXpToNextLevel() { return 250 - miningXp % 250; }
    public static int getFilterBlockCount() { return filterBlocks.size(); }
    public static void saveNow() { save(); }
}
