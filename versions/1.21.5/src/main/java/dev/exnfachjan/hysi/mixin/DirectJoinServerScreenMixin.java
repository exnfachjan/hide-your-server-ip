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
import net.minecraft.client.gui.components.events.GuiEventListener;

@Mixin(DirectJoinServerScreen.class)
public abstract class DirectJoinServerScreenMixin extends Screen {

    @Unique private EditBox hysi$ipBox;
    @Unique private String  hysi$realValue = "";
    @Unique private boolean hysi$masked = true;
    @Unique private boolean hysi$syncing = false;
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

        hysi$ipBox.setResponder(newDisplayValue -> {
            if (hysi$syncing) return;
            if (hysi$masked) {
                int oldLen = hysi$realValue.length();
                int newLen = newDisplayValue.length();
                if (newLen < oldLen) {
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