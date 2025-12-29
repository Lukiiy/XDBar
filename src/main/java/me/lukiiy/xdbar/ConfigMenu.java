package me.lukiiy.xdbar;

import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
        public final Font font;

        public ConfigList() {
            super(ConfigMenu.this.minecraft, ConfigMenu.this.width, layout.getContentHeight(), layout.getHeaderHeight(), 24);

            font = ConfigMenu.this.font;
            loadStuff();
        }

        private void loadStuff() {
            addEntry(new CategoryEntry("locator"));
            addEntry(new BooleanEntry("locatorBar.pins"));
            addEntry(new EnumEntry<>("locatorBar.background", "xdbar.config.stylecycle", XDBar.LocatorBgVisibility.class));
            addEntry(new BooleanEntry("locatorBar.arrows"));
            addEntry(new BooleanEntry("locatorBar.coloredArrows"));
            addEntry(new BooleanEntry("locatorBar.distance"));

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

        private EditBox createEditBox(String key, String defaultValue, int width) {
            EditBox box = new EditBox(font, 0, 0, width, 20, Component.literal(key));

            box.setValue(XDBar.CONFIG.getOrDefault(key, defaultValue));
            return box;
        }

        private record StaticNarration(Component text) implements NarratableEntry {
            @Override
            public NarrationPriority narrationPriority() {
                return NarrationPriority.HOVERED;
            }

            @Override
            public void updateNarration(NarrationElementOutput output) {
                output.add(NarratedElementType.TITLE, text);
            }
        }

        abstract class Entry extends ContainerObjectSelectionList.Entry<Entry> {
            protected final List<AbstractWidget> children = Lists.newArrayList();
            protected final Component label;
            protected final StaticNarration labelNarration;
            protected AbstractWidget widget;

            protected Entry(Component label, AbstractWidget widget) {
                this.label = label;
                this.labelNarration = label == null ? null : new StaticNarration(label);
                setWidget(widget);
            }

            protected void setWidget(AbstractWidget widget) {
                this.widget = widget;

                if (widget != null) {
                    if (!(widget instanceof CycleButton<?>)) widget.setMessage(label);
                    children.add(widget);
                }
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return children;
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return widget != null ? children : (labelNarration != null ? List.of(labelNarration) : List.of());
            }

            @Override
            public void render(GuiGraphics instance, int index, int y, int x, int width, int height, int mx, int my, boolean hovered, float delta) {
                if (label != null) instance.drawString(ConfigList.this.font, label, x, y + 6, -1);

                if (widget != null) {
                    widget.setX(x + width - widget.getWidth());
                    widget.setY(y);
                    widget.render(instance, mx, my, delta);
                }
            }
        }

        class CategoryEntry extends Entry {
            public CategoryEntry(String key) {
                super(Component.translatable("xdbar.config.category." + key).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD), null);
            }

            @Override
            public void render(GuiGraphics instance, int index, int y, int x, int width, int h, int mx, int my, boolean hovered, float delta) {
                instance.drawCenteredString(font, label, x + width / 2, y + 6, -1);
            }
        }

        class BooleanEntry extends Entry {
            public BooleanEntry(String key) {
                super(Component.translatable("xdbar.setting." + key), Checkbox.builder(Component.empty(), font).selected(XDBar.CONFIG.getBoolean(key)).onValueChange((b, v) -> XDBar.CONFIG.set(key, String.valueOf(v))).build());
            }
        }

        class IntEntry extends Entry {
            public IntEntry(String key, int min, int max) {
                super(Component.translatable("xdbar.setting." + key), createEditBox(key, "0", 60));
                EditBox box = (EditBox) widget;

                box.setFilter(s -> s.isEmpty() || s.matches("\\d+"));
                box.setResponder(s -> XDBar.CONFIG.set(key, String.valueOf(Math.clamp(s.isEmpty() ? 0 : Integer.parseInt(s), min, max))));
            }
        }

        class ColorEntry extends Entry {
            public ColorEntry(String key) {
                super(Component.translatable("xdbar.setting." + key), createEditBox(key, "", 60));
                EditBox box = (EditBox) widget;

                box.setMaxLength(8);
                box.setFilter(s -> s.matches("^[0-9A-Fa-f]{0,8}$"));
                box.setValue(Integer.toHexString(Integer.parseInt(XDBar.CONFIG.getOrDefault(key, "0"))));
                box.setResponder(s -> XDBar.CONFIG.set(key, String.valueOf(hexToInt(s))));
                box.setTooltip(Tooltip.create(Component.translatable("xdbar.config.colortip")));
            }

            @Override
            public void render(GuiGraphics instance, int index, int y, int x, int width, int h, int mx, int my, boolean hovered, float delta) {
                super.render(instance, index, y, x, width, h, mx, my, hovered, delta);

                int color = hexToInt(((EditBox) widget).getValue());
                if (color != 0) {
                    int size = 10;
                    int px = widget.getX() + widget.getWidth() - size / 2;
                    int py = widget.getY() - size / 2;

                    instance.fill(px, py, px + size, py + size, 0xFF000000 | color);
                    instance.renderOutline(px, py, size, size, 0xFF000000);
                }
            }

            private static int hexToInt(String hex) {
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

        class EnumEntry<T extends Enum<T>> extends Entry {
            public EnumEntry(String key, String cycleLabel, Class<T> enumClass) {
                super(Component.translatable("xdbar.setting." + key), null);

                T[] values = enumClass.getEnumConstants();
                T current = Optional.ofNullable(XDBar.CONFIG.get(key)).map(v -> Enum.valueOf(enumClass, v)).orElse(values[0]);

                int width = font.width(Component.translatable(cycleLabel)) + font.width(": ") + Arrays.stream(values).mapToInt(v -> font.width(v.name())).max().orElse(0) + 10;
                setWidget(CycleButton.<T>builder(v -> Component.literal(v.name())).withValues(values).withInitialValue(current).create(0, 0, width, 20, Component.translatable(cycleLabel), (btn, val) -> XDBar.CONFIG.set(key, val.name())));
            }
        }
    }
}