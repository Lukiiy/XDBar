package me.lukiiy.xdbar.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.lukiiy.xdbar.XDBar;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.contextualbar.ContextualBar;
import net.minecraft.client.gui.contextualbar.LocatorBar;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
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
@Mixin(Hud.class)
public class GuiMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private Map<?, Supplier<ContextualBar>> contextualInfoBars;

    @Unique private LocatorBar locatorRenderer;
    @Unique private static final int TEXT_OUTLINE = 0xFF000000;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void xdBar$getRenderer(Minecraft minecraft, CallbackInfo ci) {
        contextualInfoBars.values().stream().map(Supplier::get)
                .filter(LocatorBar.class::isInstance) // ooh!
                .map(LocatorBar.class::cast)
                .findFirst()
                .ifPresent(render -> locatorRenderer = render);
    }

    @Redirect(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractExperienceLevel(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;I)V"))
    private void xdBar$displayLevel(GuiGraphicsExtractor graphics, Font font, int experienceLevel) {
        if (!XDBar.shadow && XDBar.outline && XDBar.color == XDBar.DEF_COLOR && XDBar.offsetY == XDBar.DEF_OFFSET) ContextualBar.extractExperienceLevel(graphics, font, experienceLevel);
        else {
            Component value = Component.translatable("gui.experience.level", experienceLevel);
            int x = (graphics.guiWidth() - font.width(value)) / 2;
            int y = graphics.guiHeight() - XDBar.offsetY;

            if (XDBar.outline) XDBar.textOutline(graphics, font, value, x, y, TEXT_OUTLINE);

            graphics.text(font, value, x, y, XDBar.color, XDBar.shadow);
        }
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