package me.lukiiy.xdbar;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class ConfigMenu extends Screen {
    private static final Component TITLE = Component.translatable("xdbar.config.title");
    private final Screen parent;
    private EditBox colorInput;
    private EditBox offsetYInput;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    public ConfigMenu(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.layout.addTitleHeader(TITLE, this.font);
        LinearLayout content = this.layout.addToContents(LinearLayout.vertical()).spacing(8);
        content.defaultCellSetting().alignHorizontallyCenter();

        // Keep XP bar with locator bar
        addToggle(content, "xdbar.config.keepXPBarWithLocator", XDBar.keepXPBarWithLocator, val -> XDBar.keepXPBarWithLocator = val, "keepXPBarWithLocator");

        // Level indicator
        content.addChild(new StringWidget(Component.translatable("xdbar.config.levelIndicator"), font));

        // Shadow
        addToggle(content, "xdbar.config.levelShadow", XDBar.shadow, val -> XDBar.shadow = val, "level.shadow");

        // Color
        GridLayout colorGrid = new GridLayout().rowSpacing(2);
        colorGrid.defaultCellSetting().alignVerticallyMiddle().paddingHorizontal(2);
        GridLayout.RowHelper colorRow = colorGrid.createRowHelper(2);

        int colorPreview = ThreadLocalRandom.current().nextInt(90) + 10;
        MutableComponent colorLabel = Component.translatable("xdbar.config.levelColor").append(" (").append(Component.literal(String.valueOf(colorPreview)).withColor(XDBar.color)).append("):");

        colorRow.addChild(new StringWidget(colorLabel, font));
        colorInput = new EditBox(font, 0, 0, 50, 20, Component.empty());
        colorInput.setValue(String.format("#%06X", XDBar.color & 0xFFFFFF));
        colorRow.addChild(colorInput);
        colorInput.setResponder(v -> colorLabel.getSiblings().set(1, Component.literal(String.valueOf(colorPreview)).withColor(XDBar.hexToMC(v))));

        content.addChild(colorGrid);

        // OffsetY input
        GridLayout offsetGrid = new GridLayout().rowSpacing(2);
        offsetGrid.defaultCellSetting().alignVerticallyMiddle().paddingHorizontal(2);

        GridLayout.RowHelper offsetRow = offsetGrid.createRowHelper(2);

        offsetRow.addChild(new StringWidget(Component.translatable("xdbar.config.offsetY"), font));
        offsetYInput = new EditBox(font, 0, 0, 50, 20, Component.empty());
        offsetYInput.setFilter(s -> s.isEmpty() || s.matches("-?\\d{0,3}"));
        offsetYInput.setValue(String.valueOf(XDBar.offsetY));
        offsetRow.addChild(offsetYInput);

        content.addChild(offsetGrid);

        // Done
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, b -> onClose()).width(200).build());

        repositionElements();
        this.layout.visitWidgets(this::addRenderableWidget);
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
    }

    private void addToggle(LinearLayout layout, String key, boolean current, Consumer<Boolean> setter, String configKey) {
        Button toggleButton = Button.builder(toggleComponent(key, current), b -> {
            boolean currentValue = XDBar.config.getBoolean(configKey);
            boolean newValue = !currentValue;

            setter.accept(newValue);
            XDBar.config.set(configKey, String.valueOf(newValue));
            b.setMessage(toggleComponent(key, newValue));
        }).width(180).build();

        layout.addChild(toggleButton);
    }

    private Component toggleComponent(String key, boolean current) {
        return Component.translatable(key).append(": ").append(current ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }

    @Override
    public void onClose() {
        int color = XDBar.hexToMC(colorInput.getValue());

        XDBar.color = color;
        XDBar.config.set("level.color", Integer.toHexString(color & 0xFFFFFF).toUpperCase());

        try {
            int offset = Math.clamp(Integer.parseInt(offsetYInput.getValue()), -100, 100);

            XDBar.offsetY = offset;
            XDBar.config.set("level.offsetY", String.valueOf(offset));
        } catch (NumberFormatException ignored) {}

        XDBar.updateConfig();
        if (minecraft != null) minecraft.setScreen(parent);
    }
}