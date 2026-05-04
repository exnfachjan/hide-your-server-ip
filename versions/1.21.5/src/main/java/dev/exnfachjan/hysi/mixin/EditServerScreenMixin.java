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

        // Capture initial value (existing server IP when editing)
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

    /**
     * Intercept character input when masked.
     * We consume the event and manually append to hysi$realValue,
     * then show the correct number of bullets.
     */
    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void hysi$charTyped(char codePoint, int modifiers,
                                CallbackInfoReturnable<Boolean> cir) {
        if (!hysi$masked || hysi$ipBox == null || !hysi$ipBox.isFocused()) return;
        hysi$realValue += codePoint;
        hysi$applyDisplay();
        cir.setReturnValue(true);
    }

    /**
     * Intercept backspace/delete when masked.
     * All other keys (Enter, Tab, arrows…) are NOT cancelled so screen
     * navigation and saving still work normally.
     */
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void hysi$keyPressed(int keyCode, int scanCode, int modifiers,
                                 CallbackInfoReturnable<Boolean> cir) {
        if (!hysi$masked || hysi$ipBox == null || !hysi$ipBox.isFocused()) return;
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!hysi$realValue.isEmpty()) {
                hysi$realValue = hysi$realValue.substring(0, hysi$realValue.length() - 1);
                hysi$applyDisplay();
            }
            cir.setReturnValue(true); // consume only backspace
        } else if (keyCode == GLFW.GLFW_KEY_DELETE) {
            hysi$realValue = "";
            hysi$applyDisplay();
            cir.setReturnValue(true); // consume only delete
        }
        // Everything else (Enter, Tab, Escape, Ctrl+A etc.) passes through
    }

    /**
     * Before the screen saves, restore the real value so Minecraft
     * writes the actual IP — not the bullet placeholder.
     * We hook "addButton" which is called right before the save action
     * in vanilla's addServer / editServer logic, but the safest hook is
     * the save button's press which calls the parent onClose flow.
     * Instead we inject at the top of every render tick: if the box
     * no longer has our bullets (e.g. Minecraft read getValue() for
     * saving) we don't need to do anything — so we just restore once
     * right before the screen closes via removed().
     */
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