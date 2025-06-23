package me.lukiiy.xpbarenhances;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.contextualbar.ExperienceBarRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XPBarEnhances implements ClientModInitializer {
    public static final String MOD_ID = "xpBarEnhancer";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Config config;
    public static ExperienceBarRenderer xpBarRenderer;

    public static boolean shadow;
    public static int color;
    public static int offsetY;
    public static boolean locatorBarDoesntDisableXPBar;

    @Override
    public void onInitializeClient() {
        config = new Config(FabricLoader.getInstance(), LOGGER, MOD_ID, "XPBarEnhancer");

        config.setIfAbsent("level.shadow", "true");
        config.setIfAbsent("level.color", "default # Put a hex color here if you want");
        config.setIfAbsent("level.offsetY", "38");
        config.setIfAbsent("locatorBarDoesntDisableXPBar", "true");

        updateConfig();
    }

    public static void updateConfig() {
        shadow = config.getBoolean("level.shadow");
        locatorBarDoesntDisableXPBar = config.getBoolean("locatorBarDoesntDisableXPBar");

        String cTemp = config.get("level.color");
        if (cTemp.startsWith("default")) color = -8323296;
        else color = hexToMC(cTemp);

        offsetY = Integer.getInteger(config.get("level.offsetY"), 38);
    }

    private static int hexToMC(String hex) {
        hex = hex.replace("#", "");
        if (hex.length() == 6) hex = "FF" + hex;

        return (int) Long.parseLong(hex, 16);
    }
}
