package me.lukiiy.xdbar;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XDBar implements ClientModInitializer {
    public static final String MOD_ID = "xdbar";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static Config CONFIG = new Config("xdBar", "XDBar");

    public static final int DEF_COLOR = -8323296; // vanilla level color
    public static final int DEF_OFFSET = 35; // vanilla offset

    public static boolean shadow;
    public static int color;
    public static int offsetY;
    public static boolean pins;
    public static LocatorBgVisibility backgroundVisibility;
    public static boolean outline;
    public static boolean arrows;
    public static boolean distanceDisplay;
    public static boolean coloredArrows;

    @Override
    public void onInitializeClient() {
        loadConfig();
    }

    public static void loadConfig() {
        // Default preset
        CONFIG.setIfAbsent("level.shadow", "false");
        CONFIG.setIfAbsent("level.outline", "true");
        CONFIG.setIfAbsent("level.color", String.valueOf(DEF_COLOR));
        CONFIG.setIfAbsent("level.offsetY", String.valueOf(DEF_OFFSET));

        CONFIG.setIfAbsent("locatorBar.pins", "true");
        CONFIG.setIfAbsent("locatorBar.background", LocatorBgVisibility.DISABLED.name());
        CONFIG.setIfAbsent("locatorBar.arrows", "true");
        CONFIG.setIfAbsent("locatorBar.distance", "false");
        CONFIG.setIfAbsent("locatorBar.coloredArrows", "false");

        // Conversion
        convertBgVisibility();

        // Load
        shadow = CONFIG.getBoolean("level.shadow");
        outline = CONFIG.getBoolean("level.outline");
        pins = CONFIG.getBoolean("locatorBar.pins");
        backgroundVisibility = LocatorBgVisibility.valueOf(CONFIG.get("locatorBar.background"));
        arrows = CONFIG.getBoolean("locatorBar.arrows");
        distanceDisplay = CONFIG.getBoolean("locatorBar.distance");
        coloredArrows = CONFIG.getBoolean("locatorBar.coloredArrows");

        String processed = CONFIG.getOrDefault("level.color", "0");
        color = processed.equalsIgnoreCase("0") ? DEF_COLOR : Integer.parseInt(processed);

        try {
            offsetY = Integer.parseInt(CONFIG.get("level.offsetY"));
        } catch (NumberFormatException e) {
            offsetY = DEF_OFFSET;
        }

        CONFIG.save();
    }

    private static void convertBgVisibility() {
        String value = String.valueOf(CONFIG.get("locatorBar.background"));
        LocatorBgVisibility set = LocatorBgVisibility.ENABLED;

        try {
            set = "true".equalsIgnoreCase(value) ? LocatorBgVisibility.ENABLED : "false".equalsIgnoreCase(value) ? LocatorBgVisibility.DISABLED : LocatorBgVisibility.valueOf(value);
        } catch (Exception ignored) {}

        CONFIG.set("locatorBar.background", set.name());
    }

    public enum LocatorBgVisibility {
        ENABLED,
        DISABLED,
        CLEVER
    }

    public static boolean renderBackground(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;

        return switch (XDBar.backgroundVisibility) {
            case ENABLED -> true;
            case DISABLED -> false;
            case CLEVER -> {
                if (player == null || player.isCreative()) yield false;
                if (player.isSpectator()) yield true;

                yield player.experienceProgress == 0f;
            }
        };
    }

    public static void textOutline(GuiGraphics instance, Font font, Component value, int x, int y, int color) {
        instance.drawString(font, value, x + 1, y, color, false);
        instance.drawString(font, value, x - 1, y, color, false);
        instance.drawString(font, value, x, y + 1, color, false);
        instance.drawString(font, value, x, y - 1, color, false);
    }
}
