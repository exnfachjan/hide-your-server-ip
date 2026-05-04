package dev.exnfachjan.hysi.mixin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.DirectJoinServerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.lwjgl.glfw.GLFW;

@Mixin(DirectJoinServerScreen.class)
public abstract class DirectJoinServerScreenMixin extends Screen {

    @Unique private EditBox hysi$ipBox;
    @Unique private String  hysi$realValue = "";
    @Unique private boolean hysi$masked = true;
    @Unique private Button  hysi$toggleButton;

    protected DirectJoinServerScreenMixin(Component title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void hysi$afterInit(CallbackInfo ci) {
        hysi$ipBox = null;
        for (GuiEventListener child : this.children()) {
            if (child instanceof EditBox box) { hysi$ipBox = box; break; }
        }
        if (hysi$ipBox == null) return;
        hysi$realValue = hysi$ipBox.getValue();
        hysi$ipBox.setWidth(hysi$ipBox.getWidth() - 26);
        hysi$applyDisplay();
        hysi$toggleButton = this.addRenderableWidget(
                Button.builder(hysi$buttonLabel(), btn -> {
                            hysi$masked = !hysi$masked;
                            hysi$applyDisplay();
                            hysi$toggleButton.setMessage(hysi$buttonLabel());
                        })
                        .bounds(hysi$ipBox.getX() + hysi$ipBox.getWidth() + 2,
                                hysi$ipBox.getY(), 24, 20)
                        .build()
        );
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void hysi$charTyped(char codePoint, int modifiers,
                                CallbackInfoReturnable<Boolean> cir) {
        if (!hysi$masked || hysi$ipBox == null || !hysi$ipBox.isFocused()) return;
        hysi$realValue += codePoint;
        hysi$applyDisplay();
        cir.setReturnValue(true);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void hysi$keyPressed(int keyCode, int scanCode, int modifiers,
                                 CallbackInfoReturnable<Boolean> cir) {
        if (!hysi$masked || hysi$ipBox == null || !hysi$ipBox.isFocused()) return;
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!hysi$realValue.isEmpty()) {
                hysi$realValue = hysi$realValue.substring(0, hysi$realValue.length() - 1);
                hysi$applyDisplay();
            }
            cir.setReturnValue(true);
        } else if (keyCode == GLFW.GLFW_KEY_DELETE) {
            hysi$realValue = "";
            hysi$applyDisplay();
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void hysi$onRemoved(CallbackInfo ci) {
        if (hysi$ipBox == null) return;
        hysi$ipBox.setValue(hysi$realValue);
    }

    @Unique private void hysi$applyDisplay() {
        if (hysi$ipBox == null) return;
        hysi$ipBox.setValue(hysi$masked
                ? "•".repeat(hysi$realValue.length())
                : hysi$realValue);
    }

    @Unique private Component hysi$buttonLabel() {
        return hysi$masked
                ? Component.literal("[*]").withStyle(ChatFormatting.RED)
                : Component.literal("[O]").withStyle(ChatFormatting.GREEN);
    }
}