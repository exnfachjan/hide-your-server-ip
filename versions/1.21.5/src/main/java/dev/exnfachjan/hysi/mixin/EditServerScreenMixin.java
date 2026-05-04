package dev.exnfachjan.hysi.mixin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
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
import java.util.ArrayList;
import java.util.List;

@Mixin(targets = "net.minecraft.client.gui.screens.EditServerScreen")
public abstract class EditServerScreenMixin extends Screen {

    @Unique private EditBox hysi$ipBox;
    @Unique private String  hysi$realValue = "";
    @Unique private boolean hysi$masked = true;
    @Unique private boolean hysi$syncing = false;
    @Unique private Button  hysi$toggleButton;

    protected EditServerScreenMixin(Component title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void hysi$afterInit(CallbackInfo ci) {
        hysi$ipBox = null;
        List<EditBox> boxes = new ArrayList<>();
        for (GuiEventListener child : this.children()) {
            if (child instanceof EditBox box) boxes.add(box);
        }
        if (boxes.isEmpty()) return;
        boxes.sort((a, b) -> Integer.compare(b.getY(), a.getY()));
        hysi$ipBox = boxes.get(0);
        hysi$realValue = hysi$ipBox.getValue();
        hysi$ipBox.setWidth(hysi$ipBox.getWidth() - 26);
        hysi$applyDisplay();
        hysi$toggleButton = this.addRenderableWidget(
                Button.builder(hysi$buttonLabel(), btn -> {
                            hysi$masked = !hysi$masked;
                            hysi$applyDisplay();
                            // Move cursor to end so user can type immediately
                            if (!hysi$masked) hysi$ipBox.moveCursorToEnd(false);
                            hysi$toggleButton.setMessage(hysi$buttonLabel());
                        })
                        .bounds(hysi$ipBox.getX() + hysi$ipBox.getWidth() + 2,
                                hysi$ipBox.getY(), 24, 20)
                        .build()
        );
    }

    /** Intercept character input when masked — write to realValue, show bullets. */
    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void hysi$charTyped(char codePoint, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (!hysi$masked || hysi$ipBox == null || !hysi$ipBox.isFocused()) return;
        hysi$realValue += codePoint;
        hysi$applyDisplay();
        cir.setReturnValue(true);
    }

    /** Intercept backspace/delete when masked. */
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void hysi$keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (!hysi$masked || hysi$ipBox == null || !hysi$ipBox.isFocused()) return;
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !hysi$realValue.isEmpty()) {
            hysi$realValue = hysi$realValue.substring(0, hysi$realValue.length() - 1);
            hysi$applyDisplay();
            cir.setReturnValue(true);
        } else if (keyCode == GLFW.GLFW_KEY_DELETE) {
            // Delete key — clear whole field for simplicity
            hysi$realValue = "";
            hysi$applyDisplay();
            cir.setReturnValue(true);
        }
        // All other keys (arrows, tab, enter) pass through normally
    }

    @Unique private void hysi$applyDisplay() {
        if (hysi$ipBox == null) return;
        hysi$syncing = true;
        hysi$ipBox.setValue(hysi$masked ? "•".repeat(hysi$realValue.length()) : hysi$realValue);
        hysi$syncing = false;
    }

    @Inject(method = "onClose", at = @At("HEAD"))
    private void hysi$restoreBeforeSave(CallbackInfo ci) {
        if (hysi$ipBox == null) return;
        hysi$syncing = true;
        hysi$ipBox.setValue(hysi$realValue);
        hysi$syncing = false;
    }

    @Unique private Component hysi$buttonLabel() {
        return hysi$masked
                ? Component.literal("[*]").withStyle(ChatFormatting.RED)
                : Component.literal("[O]").withStyle(ChatFormatting.GREEN);
    }
}