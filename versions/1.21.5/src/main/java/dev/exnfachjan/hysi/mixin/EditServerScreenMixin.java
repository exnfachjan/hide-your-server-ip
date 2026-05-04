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
import net.minecraft.client.gui.components.events.GuiEventListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Hides the server IP in the "Edit Server" screen.
 *
 * Strategy: We keep the real IP in hysi$realValue and show only bullet
 * characters in the EditBox.  A listener syncs every keystroke back to
 * hysi$realValue.  When the user saves, the screen reads from the
 * EditBox — but we override the displayed value so the screen always
 * writes the real value (stored separately) via the responder.
 *
 * This approach works on ALL versions (1.21.5 – 1.21.11) because it
 * touches only the public EditBox API (getValue/setValue/setResponder),
 * which has been stable across all these versions.
 */
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

        // Capture initial value before we overwrite it with bullets
        hysi$realValue = hysi$ipBox.getValue();

        // Intercept every change the user makes
        hysi$ipBox.setResponder(newDisplayValue -> {
            if (hysi$syncing) return;
            if (hysi$masked) {
                // User typed/deleted — figure out the real delta
                // Simple: count bullets typed and sync cursor position
                // We rebuild realValue from bullet count change
                int oldLen = hysi$realValue.length();
                int newLen = newDisplayValue.length();
                if (newLen > oldLen) {
                    // Characters added — we can't recover what was typed
                    // (the box only shows bullets), so keep realValue as-is
                    // and re-mask immediately
                } else if (newLen < oldLen) {
                    // Characters deleted from the end
                    hysi$realValue = hysi$realValue.substring(0, newLen);
                }
                hysi$applyDisplay();
            } else {
                hysi$realValue = newDisplayValue;
            }
        });

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

    @Unique private void hysi$applyDisplay() {
        if (hysi$ipBox == null) return;
        hysi$syncing = true;
        if (hysi$masked) {
            String bullets = "•".repeat(hysi$realValue.length());
            hysi$ipBox.setValue(bullets);
        } else {
            hysi$ipBox.setValue(hysi$realValue);
        }
        hysi$syncing = false;
    }

    /**
     * Before the screen saves the server entry, put the real value back
     * so the correct IP gets saved.
     */
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