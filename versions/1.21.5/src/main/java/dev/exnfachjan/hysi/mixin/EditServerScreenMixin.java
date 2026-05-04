package dev.exnfachjan.hysi.mixin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.gui.components.events.GuiEventListener;
import java.util.ArrayList;
import java.util.List;

// String-based target avoids compile-time import issues across MC versions
@Mixin(targets = "net.minecraft.client.gui.screens.EditServerScreen")
public abstract class EditServerScreenMixin extends Screen {

    @Unique private EditBox hysi$ipBox;
    @Unique private boolean hysi$ipVisible = false;
    @Unique private Button hysi$toggleButton;

    protected EditServerScreenMixin(Component title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void hysi$afterInit(CallbackInfo ci) {
        hysi$ipBox = null;
        List<EditBox> boxes = new ArrayList<>();
        for (GuiEventListener child : this.children()) {
            if (child instanceof EditBox box) boxes.add(box);
        }
        if (boxes.isEmpty()) return;
        // The IP box is the one positioned lowest on screen
        boxes.sort((a, b) -> Integer.compare(b.getY(), a.getY()));
        hysi$ipBox = boxes.get(0);
        hysi$ipBox.setWidth(hysi$ipBox.getWidth() - 26);
        hysi$applyFormatter();
        hysi$toggleButton = this.addRenderableWidget(
            Button.builder(hysi$buttonLabel(), btn -> hysi$toggleVisibility())
                  .bounds(hysi$ipBox.getX() + hysi$ipBox.getWidth() + 2,
                          hysi$ipBox.getY(), 24, 20)
                  .build()
        );
    }

    @Unique private void hysi$toggleVisibility() {
        hysi$ipVisible = !hysi$ipVisible;
        hysi$applyFormatter();
        if (hysi$toggleButton != null)
            hysi$toggleButton.setMessage(hysi$buttonLabel());
    }

    @Unique private void hysi$applyFormatter() {
        if (hysi$ipBox == null) return;
        if (hysi$ipVisible) {
            hysi$ipBox.setFormatter((text, pos) -> FormattedCharSequence.forward(text, Style.EMPTY));
        } else {
            hysi$ipBox.setFormatter((text, pos) -> {
                if (text.isEmpty()) return FormattedCharSequence.EMPTY;
                return FormattedCharSequence.forward("•".repeat(text.length()), Style.EMPTY);
            });
        }
    }

    @Unique private Component hysi$buttonLabel() {
        return hysi$ipVisible
            ? Component.literal("[O]").withStyle(ChatFormatting.GREEN)
            : Component.literal("[*]").withStyle(ChatFormatting.RED);
    }
}
