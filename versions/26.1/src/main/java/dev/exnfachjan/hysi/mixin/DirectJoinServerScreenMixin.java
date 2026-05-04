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

    @Unique private EditBox hysi$box;
    @Unique private String  hysi$real = "";
    @Unique private boolean hysi$masked = true;
    @Unique private boolean hysi$lock = false;
    @Unique private Button  hysi$btn;

    protected DirectJoinServerScreenMixin(Component title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void hysi$init(CallbackInfo ci) {
        for (GuiEventListener c : this.children())
            if (c instanceof EditBox b) { hysi$box = b; break; }
        if (hysi$box == null) return;
        hysi$real = hysi$box.getValue();

        hysi$box.setResponder(val -> {
            if (hysi$lock) return;
            if (!hysi$masked) {
                hysi$real = val;
                return;
            }
            int prevLen = hysi$real.length();
            int newLen  = val.length();
            if (newLen > prevLen) {
                String added = val.substring(prevLen);
                hysi$real = hysi$real + added;
            } else if (newLen < prevLen) {
                hysi$real = hysi$real.substring(0, newLen);
            }
            hysi$lock = true;
            hysi$box.setValue("•".repeat(hysi$real.length()));
            hysi$lock = false;
        });

        hysi$box.setWidth(hysi$box.getWidth() - 26);
        hysi$lock = true;
        hysi$box.setValue("•".repeat(hysi$real.length()));
        hysi$lock = false;

        hysi$btn = this.addRenderableWidget(
                Button.builder(hysi$label(), b -> {
                            hysi$masked = !hysi$masked;
                            hysi$lock = true;
                            hysi$box.setValue(hysi$masked ? "•".repeat(hysi$real.length()) : hysi$real);
                            hysi$lock = false;
                            hysi$btn.setMessage(hysi$label());
                        })
                        .bounds(hysi$box.getX() + hysi$box.getWidth() + 2,
                                hysi$box.getY(), 24, 20)
                        .build()
        );
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void hysi$removed(CallbackInfo ci) {
        if (hysi$box == null) return;
        hysi$lock = true;
        hysi$box.setValue(hysi$real);
        hysi$lock = false;
    }

    @Unique private Component hysi$label() {
        return hysi$masked
                ? Component.literal("[*]").withStyle(ChatFormatting.RED)
                : Component.literal("[O]").withStyle(ChatFormatting.GREEN);
    }
}