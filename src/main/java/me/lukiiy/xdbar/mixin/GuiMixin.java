package me.lukiiy.xdbar.mixin;

import me.lukiiy.xdbar.XDBar;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import net.minecraft.client.gui.contextualbar.LocatorBarRenderer;
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
@Mixin(Gui.class)
public class GuiMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private Map<?, Supplier<ContextualBarRenderer>> contextualInfoBarRenderers;

    @Unique private LocatorBarRenderer locatorRenderer;
    @Unique private static final int TEXT_OUTLINE = -16777216;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void xdBar$getRenderer(Minecraft minecraft, CallbackInfo ci) {
        contextualInfoBarRenderers.values().stream().map(Supplier::get)
                .filter(LocatorBarRenderer.class::isInstance) // ooh!
                .map(LocatorBarRenderer.class::cast)
                .findFirst()
                .ifPresent(render -> locatorRenderer = render);
    }

    @Redirect(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;extractExperienceLevel(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;I)V"))
    private void xdBar$displayLevel(GuiGraphicsExtractor graphics, Font font, int experienceLevel) {
        if (!XDBar.shadow && XDBar.outline && XDBar.color == XDBar.DEF_COLOR && XDBar.offsetY == XDBar.DEF_OFFSET) ContextualBarRenderer.extractExperienceLevel(graphics, font, experienceLevel);
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

    @Redirect(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasExperience()Z"))
    private boolean xdbar$creative(MultiPlayerGameMode instance) {
        return XDBar.creativeLevel || instance.hasExperience();
    }
}
