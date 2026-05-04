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
 * Masks the server IP in EditServerScreen.
 *
 * DESIGN — only "init" and "removed" are injected.  Both are declared on
 * Screen (the superclass) and are therefore always found in the RefMap even
 * when targeting an obfuscated subclass via targets="...".
 *
 * Input handling while masked works via setResponder:
 *   - lock=true  →  we set the value ourselves, ignore responder
 *   - lock=false →  user changed the value; sync hysi$real from the delta
 */
@Mixin(targets = "net.minecraft.client.gui.screens.multiplayer.EditServerScreen")
public abstract class EditServerScreenMixin extends Screen {

    @Unique private EditBox hysi$box;
    @Unique private String  hysi$real = "";
    @Unique private boolean hysi$masked = true;
    @Unique private boolean hysi$lock = false;
    @Unique private Button  hysi$btn;

    protected EditServerScreenMixin(Component title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void hysi$init(CallbackInfo ci) {
        hysi$setupIfNeeded();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void hysi$tick(CallbackInfo ci) {
        hysi$setupIfNeeded();
    }

    @Unique
    private void hysi$setupIfNeeded() {
        if (hysi$btn != null) return;
        if (hysi$box == null) {
            List<EditBox> boxes = new ArrayList<>();
            for (GuiEventListener c : this.children())
                if (c instanceof EditBox b) boxes.add(b);
            if (boxes.isEmpty()) return;
            boxes.sort((a, b) -> Integer.compare(b.getY(), a.getY()));
            hysi$box  = boxes.get(0);
            hysi$real = hysi$box.getValue();

            hysi$box.setResponder(val -> {
                if (hysi$lock) return;          // our own setValue — ignore
                if (!hysi$masked) {
                    hysi$real = val;            // unmasked: take value as-is
                    return;
                }
                // Masked: user typed or deleted.
                // val contains whatever EditBox now holds (mix of bullets + new chars).
                // We compute the change relative to our known bullet string.
                int prevLen = hysi$real.length();
                int newLen  = val.length();
                if (newLen > prevLen) {
                    // Characters were added.  We can read the newly added chars from
                    // the end of val because EditBox stores them before our override.
                    String added = val.substring(prevLen);
                    hysi$real = hysi$real + added;
                } else if (newLen < prevLen) {
                    hysi$real = hysi$real.substring(0, newLen);
                }
                // Re-apply bullets
                hysi$lock = true;
                hysi$box.setValue("•".repeat(hysi$real.length()));
                hysi$lock = false;
            });

            hysi$box.setWidth(hysi$box.getWidth() - 26);
            hysi$lock = true;
            hysi$box.setValue("•".repeat(hysi$real.length()));
            hysi$lock = false;
        }

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