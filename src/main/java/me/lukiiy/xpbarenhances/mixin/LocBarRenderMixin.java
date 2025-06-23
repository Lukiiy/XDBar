package me.lukiiy.xpbarenhances.mixin;

import me.lukiiy.xpbarenhances.XPBarEnhances;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.contextualbar.ExperienceBarRenderer;
import net.minecraft.client.gui.contextualbar.LocatorBarRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocatorBarRenderer.class)
public class LocBarRenderMixin {
    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void renderBg(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        ci.cancel();

        if (XPBarEnhances.locatorBarDoesntDisableXPBar) {
            ExperienceBarRenderer xpRender = XPBarEnhances.xpBarRenderer;
            if (xpRender != null) xpRender.renderBackground(guiGraphics, deltaTracker);
        }
    }
}
