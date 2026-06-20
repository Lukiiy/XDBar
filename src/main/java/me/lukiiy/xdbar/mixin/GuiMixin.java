package me.lukiiy.xdbar.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.lukiiy.xdbar.XDBar;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import net.minecraft.client.gui.contextualbar.LocatorBarRenderer;
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
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private Map<?, Supplier<ContextualBarRenderer>> contextualInfoBarRenderers;

    @Unique private LocatorBarRenderer locatorRenderer;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void xdBar$getRenderer(Minecraft minecraft, CallbackInfo ci) {
        contextualInfoBarRenderers.values().stream().map(Supplier::get)
                .filter(LocatorBarRenderer.class::isInstance) // ooh!
                .map(LocatorBarRenderer.class::cast)
                .findFirst()
                .ifPresent(render -> locatorRenderer = render);
    }

    @Inject(method = "renderHotbarAndDecorations", at = @At("TAIL"))
    private void xdBar$renderLocator(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (minecraft.player != null && minecraft.player.connection.getWaypointManager().hasWaypoints()) locatorRenderer.render(guiGraphics, deltaTracker);
    }

    @Inject(method = "willPrioritizeExperienceInfo", at = @At("HEAD"), cancellable = true)
    private void xdBar$prioritizeXP(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(!XDBar.renderBackground(minecraft));
    }

    @ModifyExpressionValue(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasExperience()Z"))
    private boolean xdbar$creative(boolean original) {
        if (minecraft.gameMode == null) return original;

        return original || (XDBar.creativeLevel && !minecraft.gameMode.isSpectator());
    }
}
