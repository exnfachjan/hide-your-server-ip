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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

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
        BiFunction<String, Integer, FormattedCharSequence> fmt;
        if (hysi$ipVisible) {
            fmt = (text, pos) -> FormattedCharSequence.forward(text, Style.EMPTY);
        } else {
            fmt = (text, pos) -> {
                if (text.isEmpty()) return FormattedCharSequence.EMPTY;
                return FormattedCharSequence.forward("•".repeat(text.length()), Style.EMPTY);
            };
        }
        hysi$setFormatterViaReflection(hysi$ipBox, fmt);
    }

    /**
     * Sets the formatter field on EditBox via reflection.
     * This is version-agnostic: we search all declared fields for one
     * whose type is BiFunction and set it directly, bypassing the
     * removed setFormatter() method and avoiding @Accessor remapping issues.
     */
    @Unique
    private static void hysi$setFormatterViaReflection(
            EditBox box, BiFunction<String, Integer, FormattedCharSequence> formatter) {
        Class<?> clazz = box.getClass();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getType() == BiFunction.class) {
                    field.setAccessible(true);
                    try {
                        field.set(box, formatter);
                        return;
                    } catch (IllegalAccessException e) {
                        // continue searching
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    @Unique private Component hysi$buttonLabel() {
        return hysi$ipVisible
                ? Component.literal("[O]").withStyle(ChatFormatting.GREEN)
                : Component.literal("[*]").withStyle(ChatFormatting.RED);
    }
}