package me.lukiiy.xpbarenhances.mixin;

import me.lukiiy.xpbarenhances.XPBarEnhances;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import net.minecraft.client.gui.contextualbar.ExperienceBarRenderer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.Supplier;

@Mixin(Gui.class)
public class GuiMixin {
    @Shadow @Final private Map<?, Supplier<ContextualBarRenderer>> contextualInfoBarRenderers;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void xpBarEnhancer$getRenderer(Minecraft minecraft, CallbackInfo ci) {
        contextualInfoBarRenderers.values().stream()
                .map(Supplier::get)
                .filter(ExperienceBarRenderer.class::isInstance) // ooh!
                .map(ExperienceBarRenderer.class::cast)
                .findFirst()
                .ifPresent(render -> XPBarEnhances.xpBarRenderer = render);
    }

    @Redirect(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;renderExperienceLevel(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;I)V"))
    private void xpBarEnhancer$displayLevel(GuiGraphics guiGraphics, Font font, int level) {
        if (XPBarEnhances.shadow) {
            Component component = Component.translatable("gui.experience.level", level);
            int i = (guiGraphics.guiWidth() - font.width(component)) / 2;

            guiGraphics.drawString(font, component, i, guiGraphics.guiHeight() - XPBarEnhances.offsetY, XPBarEnhances.color, true);
        } else {
            ContextualBarRenderer.renderExperienceLevel(guiGraphics, font, level);
        }
    }
}
