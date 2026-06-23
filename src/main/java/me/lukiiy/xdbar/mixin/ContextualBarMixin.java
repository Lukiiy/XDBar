package me.lukiiy.xdbar.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import me.lukiiy.xdbar.XDBar;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ContextualBarRenderer.class)
public interface ContextualBarMixin {
    @ModifyExpressionValue(method = "renderExperienceLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;guiHeight()I"))
    private static int xdbar$offset(int original) {
        if (XDBar.offsetY == XDBar.DEF_OFFSET) return original;

        return original + (XDBar.DEF_OFFSET - XDBar.offsetY);
    }

    @ModifyArg(method = "renderExperienceLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V", ordinal = 4), index = 4)
    private static int xdbar$indicatorColor(int i) {
        return XDBar.color == XDBar.DEF_COLOR ? i : XDBar.color;
    }

    @ModifyArg(method = "renderExperienceLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V", ordinal = 4), index = 5)
    private static boolean xdbar$shadow(boolean bl) {
        return bl || XDBar.shadow;
    }

    @WrapWithCondition(method = "renderExperienceLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V", ordinal = 0))
    private static boolean xdbar$outline(GuiGraphics instance, Font font, Component component, int i, int j, int k, boolean bl) {
        return XDBar.outline;
    }

    @WrapWithCondition(method = "renderExperienceLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V", ordinal = 1))
    private static boolean xdbar$outline1(GuiGraphics instance, Font font, Component component, int i, int j, int k, boolean bl) {
        return XDBar.outline;
    }

    @WrapWithCondition(method = "renderExperienceLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V", ordinal = 2))
    private static boolean xdbar$outline2(GuiGraphics instance, Font font, Component component, int i, int j, int k, boolean bl) {
        return XDBar.outline;
    }

    @WrapWithCondition(method = "renderExperienceLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V", ordinal = 3))
    private static boolean xdbar$outline3(GuiGraphics instance, Font font, Component component, int i, int j, int k, boolean bl) {
        return XDBar.outline;
    }
}