package dev.areabreaker;

public final class AreaBreakerSettings {
    public enum Mode { AREA, VEIN }
    public enum Shape { SQUARE, WALL, STRIP, TUNNEL }

    private static int areaSize = 3;
    private static Mode mode = Mode.AREA;
    private static Shape shape = Shape.SQUARE;
    private static boolean durabilityProtection = true;
    private static boolean autoPickup = true;

    private AreaBreakerSettings() {}

    public static int getAreaSize() { return areaSize; }
    public static void setAreaSize(int size) {
        if (size == 1 || size == 2 || size == 3 || size == 4 || size == 5 || size == 10) areaSize = size;
    }

    public static Mode getMode() { return mode; }
    public static void cycleMode() {
        mode = mode == Mode.AREA ? Mode.VEIN : Mode.AREA;
    }

    public static Shape getShape() { return shape; }
    public static void cycleShape() {
        shape = Shape.values()[(shape.ordinal() + 1) % Shape.values().length];
    }

    public static boolean isDurabilityProtectionEnabled() { return durabilityProtection; }
    public static void toggleDurabilityProtection() { durabilityProtection = !durabilityProtection; }

    public static boolean isAutoPickupEnabled() { return autoPickup; }
    public static void toggleAutoPickup() { autoPickup = !autoPickup; }
}