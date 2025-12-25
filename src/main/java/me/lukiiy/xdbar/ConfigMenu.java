package me.lukiiy.xdbar;

import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;

@Environment(EnvType.CLIENT)
public class ConfigMenu extends Screen {
    private static final Component TITLE = Component.translatable("xdbar.config.title");
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 33, 33);
    private ConfigList list;
    private final Screen before;

    public ConfigMenu(Screen before) {
        super(TITLE);

        this.before = before;
    }

    @Override
    protected void init() {
        layout.addToHeader(LinearLayout.vertical().spacing(8)).addChild(new StringWidget(TITLE, font), LayoutSettings::alignHorizontallyCenter);

        list = new ConfigList();

        layout.addToContents(list);
        layout.addToFooter(LinearLayout.horizontal().spacing(8)).addChild(Button.builder(CommonComponents.GUI_DONE, b -> onClose()).width(100).build());
        layout.visitWidgets(this::addRenderableWidget);
        repositionElements();
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();

        if (list != null) list.updateSize(width, layout);
    }

    @Override
    public boolean isPauseScreen() {
        return before != null && before.isPauseScreen();
    }

    @Override
    public void onClose() {
        XDBar.loadConfig();

        if (before != null) minecraft.setScreen(before);
    }

    private class ConfigList extends ContainerObjectSelectionList<ConfigList.Entry> {
        public ConfigList() {
            super(ConfigMenu.this.minecraft, ConfigMenu.this.width, layout.getContentHeight(), layout.getHeaderHeight(), 24);
            loadStuff();
        }

        private void loadStuff() {
            addEntry(new CategoryEntry("locator"));
            addEntry(new BooleanEntry("locatorBar.pins"));
            addEntry(new BooleanEntry("locatorBar.background"));
            addEntry(new BooleanEntry("locatorBar.arrows"));

            addEntry(new CategoryEntry("level"));
            addEntry(new BooleanEntry("level.outline"));
            addEntry(new BooleanEntry("level.shadow"));
            addEntry(new ColorEntry("level.color"));
            addEntry(new IntEntry("level.offsetY", -10000, 10000));
        }

        @Override
        public int getRowWidth() {
            return 310;
        }

        abstract static class Entry extends ContainerObjectSelectionList.Entry<Entry> {
            protected final List<AbstractWidget> children = Lists.newArrayList();

            @Override
            public List<? extends GuiEventListener> children() {
                return children;
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return children;
            }
        }

        class CategoryEntry extends Entry {
            private final Component text;

            public CategoryEntry(String label) {
                this.text = Component.translatable("xdbar.config.category." + label).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD);
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int y, int x, int width, int height, int mx, int my, boolean hovered, float delta) {
                guiGraphics.drawCenteredString(font, text, x + width / 2, y + 6, -1);
            }
        }

        class BooleanEntry extends Entry {
            private final Checkbox checkbox;
            private final Component label;

            public BooleanEntry(String key) {
                this.label = Component.translatable("xdbar.setting." + key);
                boolean value = XDBar.config.getBoolean(key);

                checkbox = Checkbox.builder(Component.empty(), font).selected(value).onValueChange((box, val) -> XDBar.config.set(key, String.valueOf(val))).build();
                children.add(checkbox);
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int y, int x, int width, int height, int mx, int my, boolean hovered, float delta) {
                guiGraphics.drawString(font, label, x, y + 6, -1);

                checkbox.setX(x + width - Checkbox.getBoxSize(font));
                checkbox.setY(y + 2);
                checkbox.render(guiGraphics, mx, my, delta);
            }
        }

        class IntEntry extends Entry {
            private final EditBox box;
            private final Component label;

            public IntEntry(String key, int min, int max) {
                this.label = Component.translatable("xdbar.setting." + key);
                String value = XDBar.config.get(key);

                box = new EditBox(font, 0, 0, 60, 20, Component.literal(key));
                box.setValue(value != null ? value : "");
                box.setFilter(s -> s.isEmpty() || s.matches("\\d+"));
                box.setResponder(s -> {
                    if (s.isEmpty()) {
                        XDBar.config.set(key, "0");
                        return;
                    }

                    XDBar.config.set(key, String.valueOf(Math.clamp(Integer.parseInt(s), min, max)));
                });
                box.setTooltip(Tooltip.create(Component.translatable("xdbar.config.intbox", min, max)));

                children.add(box);
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int y, int x, int width, int height, int mx, int my, boolean hovered, float delta) {
                guiGraphics.drawString(font, label, x, y + 6, -1);

                box.setX(x + width - box.getWidth());
                box.setY(y);
                box.render(guiGraphics, mx, my, delta);
            }
        }

        class ColorEntry extends Entry {
            private final EditBox box;
            private final Component label;

            public ColorEntry(String key) {
                this.label = Component.translatable("xdbar.setting." + key);

                String value = XDBar.config.get(key);
                String initHex = "";

                if (value != null) {
                    try { initHex = Integer.toHexString(Integer.parseInt(value)); } catch (NumberFormatException ignored) {}
                }

                box = new EditBox(font, 0, 0, 60, 20, Component.literal(key));
                box.setValue(initHex);
                box.setMaxLength(8);
                box.setFilter(s -> s.matches("^[0-9A-Fa-f]{0,8}$"));
                box.setResponder(s -> XDBar.config.set(key, String.valueOf(XDBar.hexToInt(s))));
                box.setTooltip(Tooltip.create(Component.translatable("xdbar.config.colortip")));

                children.add(box);
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int y, int x, int width, int height, int mx, int my, boolean hovered, float delta) {
                guiGraphics.drawString(font, label, x, y + 6, -1);
                box.setX(x + width - box.getWidth());
                box.setY(y);
                box.render(guiGraphics, mx, my, delta);

                int parsed = XDBar.hexToInt(box.getValue());
                if (parsed != 0) {
                    int size = 10;
                    int gap = 5;
                    int px = box.getX() - gap - size;
                    int py = box.getY() + (box.getHeight() - size) / 2;

                    guiGraphics.fill(px, py, px + size, py + size, parsed);
                    guiGraphics.renderOutline(px, py, size, size, 0xFF000000);
                }
            }
        }
    }
}