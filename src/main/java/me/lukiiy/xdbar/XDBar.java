package me.lukiiy.xdbar;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XDBar implements ClientModInitializer {
    public static final String MOD_ID = "xdBar";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static Config config = new Config(MOD_ID, "XDBar");

    public static final int DEF_COLOR = -8323296; // vanilla level color
    public static final int DEF_OFFSET = 35; // vanilla offset

    public static boolean shadow;
    public static int color;
    public static int offsetY;
    public static boolean pins;
    public static boolean prioritizeOthers;
    public static boolean outline;
    public static boolean arrows;

    @Override
    public void onInitializeClient() {
        loadConfig();
    }

    public static void loadConfig() {
        // Default preset
        config.setIfAbsent("level.shadow", "false");
        config.setIfAbsent("level.outline", "true");
        config.setIfAbsent("level.color", "80FF20");
        config.setIfAbsent("level.offsetY", String.valueOf(DEF_OFFSET));

        config.setIfAbsent("locatorBar.pins", "true");
        config.setIfAbsent("locatorBar.background", "false");
        config.setIfAbsent("locatorBar.arrows", "true");

        // Load
        shadow = config.getBoolean("level.shadow");
        outline = config.getBoolean("level.outline");
        pins = config.getBoolean("locatorBar.pins");
        prioritizeOthers = config.getBoolean("locatorBar.background");
        arrows = config.getBoolean("locatorBar.arrows");

        String processed = config.getOrDefault("level.color", "default");
        color = processed.equalsIgnoreCase("default") ? DEF_COLOR : Integer.parseInt(processed);

        try {
            offsetY = Integer.parseInt(config.get("level.offsetY"));
        } catch (NumberFormatException e) {
            offsetY = DEF_OFFSET;
        }

        config.save();
    }

    public static int hexToInt(String hex) {
        if (hex == null || hex.isEmpty()) return 0;

        hex = hex.replace("#", "");
        if (hex.length() == 6) hex = "FF" + hex;

        try {
            return (int) (Long.parseLong(hex, 16) & 0xFFFFFFFFL);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
