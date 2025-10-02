package me.lukiiy.xdbar.mixin;

import me.lukiiy.xdbar.XDBar;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.contextualbar.LocatorBarRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LocatorBarRenderer.class)
public class LocBarRenderMixin {
    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void renderBg(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!XDBar.prioritizeOthers) ci.cancel();
    }
}
