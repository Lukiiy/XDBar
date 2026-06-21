package me.lukiiy.xdbar.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.lukiiy.xdbar.XDBar;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.contextualbar.ContextualBar;
import net.minecraft.client.gui.contextualbar.LocatorBar;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
@Mixin(Hud.class)
public class HudMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private Map<?, Supplier<ContextualBar>> contextualInfoBars;

    @Unique private LocatorBar locatorRenderer;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void xdBar$getRenderer(Minecraft minecraft, CallbackInfo ci) {
        contextualInfoBars.values().stream().map(Supplier::get)
                .filter(LocatorBar.class::isInstance) // ooh!
                .map(LocatorBar.class::cast)
                .findFirst()
                .ifPresent(render -> locatorRenderer = render);
    }

    @Inject(method = "extractHotbarAndDecorations", at = @At("TAIL"))
    private void xdBar$renderLocator(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (minecraft.player != null && minecraft.player.connection.getWaypointManager().hasWaypoints()) locatorRenderer.extractRenderState(graphics, deltaTracker);
    }

    @Inject(method = "willPrioritizeExperienceInfo", at = @At("HEAD"), cancellable = true)
    private void xdBar$prioritizeXP(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(!XDBar.renderBackground(minecraft));
    }

    @ModifyExpressionValue(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasExperience()Z"))
    private boolean xdbar$creative(boolean original) {
        if (minecraft.gameMode == null) return original;

        return original || (XDBar.creativeLevel && !minecraft.gameMode.isSpectator());
    }
}