package me.lukiiy.xdbar;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.contextualbar.ExperienceBarRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XDBar implements ClientModInitializer {
    public static final String MOD_ID = "xdBar";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Config config;
    public static ExperienceBarRenderer xpBarRenderer;
    public static final int DEF_COLOR = -8323296; // vanilla level color
    public static final int DEF_OFFSET = 35;

    public static boolean shadow;
    public static int color;
    public static int offsetY;
    public static boolean keepXPBarWithLocator;
    public static boolean outline;

    @Override
    public void onInitializeClient() {
        config = new Config(FabricLoader.getInstance().getConfigDir(), LOGGER, MOD_ID, "XDBar");

        config.setIfAbsent("level.shadow", "false");
        config.setIfAbsent("level.outline", "true");
        config.setIfAbsent("level.color", "80FF20");
        config.setIfAbsent("level.offsetY", String.valueOf(DEF_OFFSET));
        config.setIfAbsent("keepXPBarWithLocator", "true");

        updateConfig();
    }

    public static void updateConfig() {
        shadow = config.getBoolean("level.shadow");
        outline = config.getBoolean("level.outline");
        keepXPBarWithLocator = config.getBoolean("keepXPBarWithLocator");

        String cTemp = config.get("level.color");
        color = cTemp.startsWith("default") ? DEF_COLOR : hexToInt(cTemp);

        try {
            offsetY = Integer.parseInt(config.get("level.offsetY"));
        } catch (NumberFormatException e) {
            offsetY = DEF_OFFSET;
        }

        config.save();
    }

    public static int hexToInt(String hex) {
        hex = hex.replace("#", "");
        if (hex.length() == 6) hex = "FF" + hex;

        try {
            return (int) Long.parseLong(hex, 16);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
