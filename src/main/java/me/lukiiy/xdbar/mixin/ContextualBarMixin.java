package me.lukiiy.xdbar.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.lukiiy.xdbar.XDBar;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.contextualbar.ContextualBar;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(ContextualBar.class)
public interface ContextualBarMixin {
    @ModifyExpressionValue(method = "extractExperienceLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;guiHeight()I"))
    private static int xdbar$offset(int original) {
        if (XDBar.offsetY == XDBar.DEF_OFFSET) return original;

        return original + (XDBar.DEF_OFFSET - XDBar.offsetY);
    }

    @ModifyArg(method = "extractExperienceLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V", ordinal = 4), index = 4)
    private static int xdbar$indicatorColor(int x) {
        return XDBar.color == XDBar.DEF_COLOR ? x : XDBar.color;
    }

    @ModifyArg(method = "extractExperienceLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V", ordinal = 4), index = 5)
    private static boolean xdbar$shadow(boolean original) {
        return original || XDBar.shadow;
    }

    @WrapOperation(method = "extractExperienceLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V"))
    private static void xdbar$outline(GuiGraphicsExtractor instance, Font font, Component str, int x, int y, int color, boolean dropShadow, Operation<Void> original) {
        if (color == XDBar.NEUTRAL && XDBar.color != XDBar.NEUTRAL && !XDBar.outline) return; // targets outline

        original.call(instance, font, str, x, y, color, dropShadow);
    }
}