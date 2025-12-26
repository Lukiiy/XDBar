package me.lukiiy.xdbar.mixin;

import me.lukiiy.xdbar.XDBar;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.contextualbar.LocatorBarRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.WaypointStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.waypoints.TrackedWaypoint;
import net.minecraft.world.waypoints.Waypoint;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Environment(EnvType.CLIENT)
@Mixin(LocatorBarRenderer.class)
public abstract class LocBarRenderMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void xdBar$renderBg(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!XDBar.renderBackground(minecraft)) ci.cancel();
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void xdBar$pins(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!XDBar.pins) ci.cancel();
    }

    @Inject(method = "method_70870", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIII)V"), cancellable = true)
    private void xdBar$arrows(Level level, GuiGraphics guiGraphics, int i, TrackedWaypoint trackedWaypoint, CallbackInfo ci) {
        if (!XDBar.arrows) ci.cancel();
    }

    @Inject(method = "method_70870", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIII)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void xdBar$colorArrows(Level level, GuiGraphics guiGraphics, int i, TrackedWaypoint trackedWaypoint, CallbackInfo ci, double d, int j, Waypoint.Icon icon, WaypointStyle waypointStyle, float f, ResourceLocation resourceLocation, int k, int l, TrackedWaypoint.PitchDirection pitchDirection, int m, ResourceLocation resourceLocation2) {
        if (!XDBar.coloredArrows || pitchDirection == TrackedWaypoint.PitchDirection.NONE) return;

        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation2, j + l + 1, i + m, 7, 5, k);
    }

    @Inject(method = "method_70870", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIIII)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void xdBar$deco(Level level, GuiGraphics guiGraphics, int i, TrackedWaypoint trackedWaypoint, CallbackInfo ci, double d, int j, Waypoint.Icon icon, WaypointStyle waypointStyle, float f, ResourceLocation resourceLocation, int k, int l) {
        int distOffset = 15;
        if (!XDBar.distanceDisplay || l < -distOffset || l > distOffset || resourceLocation.equals(waypointStyle.sprite(Float.MAX_VALUE))) return;

        String text = Mth.floor(f) + "";
        int x = j + l + 4 - minecraft.font.width(text) / 2 + 1;
        int y = i - 2;

        XDBar.textOutline(guiGraphics, minecraft.font, Component.literal(text), x, y, 0xFF000000);
        guiGraphics.drawString(minecraft.font, text, x, y, k, false);
    }
}
