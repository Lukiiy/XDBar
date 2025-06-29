package me.lukiiy.xdbar.mixin;

import me.lukiiy.xdbar.XDBar;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
@Mixin(Gui.class)
public class GuiMixin {
    @Shadow @Final private Map<?, Supplier<ContextualBarRenderer>> contextualInfoBarRenderers;
    @Unique private static final int TEXT_OUTLINE = -16777216;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void xdBar$getRenderer(Minecraft minecraft, CallbackInfo ci) {
        contextualInfoBarRenderers.values().stream()
                .map(Supplier::get)
                .filter(ExperienceBarRenderer.class::isInstance) // ooh!
                .map(ExperienceBarRenderer.class::cast)
                .findFirst()
                .ifPresent(render -> XDBar.xpBarRenderer = render);
    }

    // overwriting
    @Redirect(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;renderExperienceLevel(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;I)V"))
    private void xdBar$displayLevel(GuiGraphics guiGraphics, Font font, int level) {
        if (!XDBar.shadow && XDBar.outline && XDBar.color == XDBar.DEF_COLOR && XDBar.offsetY == XDBar.DEF_OFFSET) {
            ContextualBarRenderer.renderExperienceLevel(guiGraphics, font, level);
        } else {
            Component value = Component.translatable("gui.experience.level", level);
            int x = (guiGraphics.guiWidth() - font.width(value)) / 2;
            int y = guiGraphics.guiHeight() - XDBar.offsetY;

            if (XDBar.outline) {
                guiGraphics.drawString(font, value, x + 1, y, TEXT_OUTLINE, false);
                guiGraphics.drawString(font, value, x - 1, y, TEXT_OUTLINE, false);
                guiGraphics.drawString(font, value, x, y + 1, TEXT_OUTLINE, false);
                guiGraphics.drawString(font, value, x, y - 1, TEXT_OUTLINE, false);
            }
            guiGraphics.drawString(font, value, x, y, XDBar.color, XDBar.shadow);
        }
    }

    @Inject(method = "willPrioritizeExperienceInfo", at = @At("HEAD"), cancellable = true)
    private void xdBar$prioritizeLocator(CallbackInfoReturnable<Boolean> cir) {
        if (XDBar.keepXPBarWithLocator) cir.setReturnValue(false);
    }
}
