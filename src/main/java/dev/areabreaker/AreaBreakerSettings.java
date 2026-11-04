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
    private static boolean safeMode = true;
    private static boolean showHud = true;
    private static long blocksMined;
    private static long oresMined;
    private static long miningXp;
    private static long itemsCollected;
    private static long longestVein;
    private static long sessionBlocks;
    private static long sessionOres;
    private static int veinRank;
    private static int magnetRank;
    private static int reinforcedRank;
    private static int geologistRank;
    private static int tunnelerRank;
    private static int reachRank;
    private static final Set<String> filterBlocks = new LinkedHashSet<>();

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
            safeMode = Boolean.parseBoolean(p.getProperty("safeMode", "true"));
            showHud = Boolean.parseBoolean(p.getProperty("showHud", "true"));
            blocksMined = Long.parseLong(p.getProperty("blocksMined", "0"));
            oresMined = Long.parseLong(p.getProperty("oresMined", "0"));
            miningXp = Long.parseLong(p.getProperty("miningXp", "0"));
            itemsCollected = Long.parseLong(p.getProperty("itemsCollected", "0"));
            longestVein = Long.parseLong(p.getProperty("longestVein", "0"));
            veinRank = bounded(p, "upgrade.vein", 0, 5);
            magnetRank = bounded(p, "upgrade.magnet", 0, 5);
            reinforcedRank = bounded(p, "upgrade.reinforced", 0, 5);
            geologistRank = bounded(p, "upgrade.geologist", 0, 5);
            tunnelerRank = bounded(p, "upgrade.tunneler", 0, 3);
            reachRank = bounded(p, "upgrade.reach", 0, 4);
            String list = p.getProperty("filterBlocks", "");
            filterBlocks.clear();
            for (String id : list.split(",")) if (!id.isBlank()) filterBlocks.add(id.trim());
        } catch (IOException | IllegalArgumentException ignored) {
            // Keep safe defaults if the config is missing or malformed.
        }
    }

    private static int bounded(Properties p, String key, int min, int max) {
        return Math.max(min, Math.min(max, Integer.parseInt(p.getProperty(key, "0"))));
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
        p.setProperty("safeMode", Boolean.toString(safeMode));
        p.setProperty("showHud", Boolean.toString(showHud));
        p.setProperty("blocksMined", Long.toString(blocksMined));
        p.setProperty("oresMined", Long.toString(oresMined));
        p.setProperty("miningXp", Long.toString(miningXp));
        p.setProperty("itemsCollected", Long.toString(itemsCollected));
        p.setProperty("longestVein", Long.toString(longestVein));
        p.setProperty("upgrade.vein", Integer.toString(veinRank));
        p.setProperty("upgrade.magnet", Integer.toString(magnetRank));
        p.setProperty("upgrade.reinforced", Integer.toString(reinforcedRank));
        p.setProperty("upgrade.geologist", Integer.toString(geologistRank));
        p.setProperty("upgrade.tunneler", Integer.toString(tunnelerRank));
        p.setProperty("upgrade.reach", Integer.toString(reachRank));
        p.setProperty("filterBlocks", filterBlocks.stream().collect(Collectors.joining(",")));
        try {
            Files.createDirectories(CONFIG.getParent());
            try (OutputStream out = Files.newOutputStream(CONFIG)) { p.store(out, "AreaBreaker miner profile"); }
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
    public static boolean isSafeModeEnabled() { return safeMode; }
    public static void toggleSafeMode() { safeMode = !safeMode; save(); }
    public static boolean isHudEnabled() { return showHud; }
    public static void toggleHud() { showHud = !showHud; save(); }
    public static FilterMode getFilterMode() { return filterMode; }
    public static void cycleFilterMode() { filterMode = FilterMode.values()[(filterMode.ordinal() + 1) % FilterMode.values().length]; save(); }

    public static boolean addFilterBlock(String id) {
        if (id == null || id.isBlank()) return false;
        boolean added;
        if (filterBlocks.contains(id)) { filterBlocks.remove(id); added = false; }
        else { filterBlocks.add(id); added = true; }
        save();
        return added;
    }

    public static boolean isBlockAllowed(Block block) {
        String path = Registries.BLOCK.getId(block).getPath();
        if (safeMode && (path.equals("bedrock") || path.equals("barrier") || path.contains("command_block")
                || path.equals("structure_block") || path.equals("jigsaw") || path.equals("end_portal_frame")
                || path.equals("end_portal") || path.equals("nether_portal"))) return false;
        if (filterMode == FilterMode.OFF) return true;
        boolean listed = filterBlocks.contains(Registries.BLOCK.getId(block).toString());
        return filterMode == FilterMode.BLACKLIST ? !listed : listed;
    }

    public static void recordMined(Block block) {
        blocksMined++;
        sessionBlocks++;
        String path = Registries.BLOCK.getId(block).getPath();
        boolean ore = path.contains("ore");
        if (ore) { oresMined++; sessionOres++; }
        int baseXp = ore ? 3 : 1;
        miningXp += baseXp + (baseXp * geologistRank / 10);
        if (blocksMined % 25 == 0) save();
    }

    public static void recordVeinSize(long size) {
        if (size > longestVein) { longestVein = size; save(); }
    }

    public static void recordItemsCollected(long count) {
        if (count > 0) { itemsCollected += count; }
    }

    public static long getBlocksMined() { return blocksMined; }
    public static long getOresMined() { return oresMined; }
    public static long getMiningXp() { return miningXp; }
    public static long getItemsCollected() { return itemsCollected; }
    public static long getLongestVein() { return longestVein; }
    public static long getSessionBlocks() { return sessionBlocks; }
    public static long getSessionOres() { return sessionOres; }
    public static int getMiningLevel() { return (int)Math.min(100, 1 + miningXp / 250); }
    public static long getXpToNextLevel() { return 250 - miningXp % 250; }
    public static int getVeinLimit() { return Math.min(128, 16 + getMiningLevel() * 8 + veinRank * 16); }
    public static int getTunnelDepth() { return Math.min(9, 3 + (getMiningLevel() - 1) / 3 + tunnelerRank); }
    public static int getMagnetRadius() { return 3 + magnetRank * 2; }
    public static int getDurabilityReserve() { return 1 + reinforcedRank * 2; }
    public static double getMiningReach() { return 6.0 + reachRank * 0.5; }
    public static int getFilterBlockCount() { return filterBlocks.size(); }

    public static int getAvailableUpgradePoints() {
        return Math.max(0, (getMiningLevel() - 1) / 5 - veinRank - magnetRank - reinforcedRank - geologistRank - tunnelerRank - reachRank);
    }

    public static int getUpgradeRank(String id) {
        return switch (id) {
            case "vein" -> veinRank;
            case "magnet" -> magnetRank;
            case "reinforced" -> reinforcedRank;
            case "geologist" -> geologistRank;
            case "tunneler" -> tunnelerRank;
            case "reach" -> reachRank;
            default -> 0;
        };
    }

    public static int getUpgradeMax(String id) { return id.equals("tunneler") ? 3 : id.equals("reach") ? 4 : 5; }

    public static boolean purchaseUpgrade(String id) {
        if (getAvailableUpgradePoints() <= 0 || getUpgradeRank(id) >= getUpgradeMax(id)) return false;
        switch (id) {
            case "vein" -> veinRank++;
            case "magnet" -> magnetRank++;
            case "reinforced" -> reinforcedRank++;
            case "geologist" -> geologistRank++;
            case "tunneler" -> tunnelerRank++;
            case "reach" -> reachRank++;
            default -> { return false; }
        }
        save();
        return true;
    }

    public static void saveNow() { save(); }
}
