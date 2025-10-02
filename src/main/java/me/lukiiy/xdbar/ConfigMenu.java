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

import java.util.function.Consumer;

public class ConfigMenu extends Screen {
    private static final Component TITLE = Component.translatable("xdbar.config.title");
    private final Screen parent;
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

        addToggle(content, "locators", XDBar.pins, val -> XDBar.pins = val, "locatorBar.pins");
        addToggle(content, "locatorBg", XDBar.prioritizeOthers, val -> XDBar.prioritizeOthers = val, "locatorBar.background");

        // Level indicator
        content.addChild(new StringWidget(Component.translatable("xdbar.config.levelIndicator"), font));

        // Outline
        addToggle(content, "levelOutline", XDBar.outline, val -> XDBar.outline = val, "level.outline");

        // Shadow
        addToggle(content, "levelShadow", XDBar.shadow, val -> XDBar.shadow = val, "level.shadow");

        // Color
        GridLayout colorDiv = new GridLayout().rowSpacing(2);
        colorDiv.defaultCellSetting().alignVerticallyMiddle().paddingHorizontal(2);

        GridLayout.RowHelper colorRow = colorDiv.createRowHelper(2);
        colorRow.addChild(new StringWidget(Component.translatable("xdbar.config.levelColor"), font));

        EditBox colorInput = new EditBox(font, 0, 0, 50, 20, Component.empty());
        colorInput.setMaxLength(7);
        colorInput.setValue(String.format("#%06X", XDBar.color & 0xFFFFFF));
        colorInput.setFilter(s -> s.isEmpty() || s.matches("#?[0-9a-fA-F]{0,6}"));

        colorRow.addChild(colorInput);

        content.addChild(colorDiv);

        // OffsetY
        GridLayout offsetDiv = new GridLayout().rowSpacing(2);
        offsetDiv.defaultCellSetting().alignVerticallyMiddle().paddingHorizontal(2);

        GridLayout.RowHelper offsetRow = offsetDiv.createRowHelper(2);
        offsetRow.addChild(new StringWidget(Component.translatable("xdbar.config.offsetY"), font));

        offsetYInput = new EditBox(font, 0, 0, 50, 20, Component.empty());
        offsetYInput.setValue(String.valueOf(XDBar.offsetY));
        offsetYInput.setFilter(s -> s.matches("-?\\d+"));

        offsetRow.addChild(offsetYInput);

        content.addChild(offsetDiv);

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
            boolean v = !XDBar.config.getBoolean(configKey);

            setter.accept(v);
            XDBar.config.set(configKey, String.valueOf(v));
            b.setMessage(toggleComponent(key, v));
        }).width(180).build();

        layout.addChild(toggleButton);
    }

    private Component toggleComponent(String key, boolean current) {
        return Component.translatable("xdbar.config." + key).append(": ").append(current ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }

    @Override
    public void onClose() {
        try {
            XDBar.offsetY = Integer.parseInt(offsetYInput.getValue());
        } catch (NumberFormatException ignored) {
            XDBar.LOGGER.info("({}) Couldn't parse the value as an integer.", Component.translatable("xdbar.config.offsetY").getString());
        }

        XDBar.config.set("level.offsetY", String.valueOf(XDBar.offsetY));

        XDBar.updateConfig();
        if (minecraft != null) minecraft.setScreen(parent);
    }
}