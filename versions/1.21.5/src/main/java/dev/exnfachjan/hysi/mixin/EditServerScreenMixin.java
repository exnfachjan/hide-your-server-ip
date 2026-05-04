package dev.exnfachjan.hysi.mixin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.DirectJoinServerScreen;
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
import java.lang.reflect.Method;
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
        hysi$setFormatter(hysi$ipBox, fmt);
    }

    /**
     * Version-agnostic formatter injection.
     * 1. Try setFormatter() method (exists in 1.21.1–1.21.5).
     * 2. Fall back to setting all BiFunction fields directly via reflection
     *    (needed for 1.21.6–1.21.11 where setFormatter() was removed).
     */
    @Unique
    private static void hysi$setFormatter(
            EditBox box, BiFunction<String, Integer, FormattedCharSequence> fmt) {
        // Try public method first (1.21.5 and older)
        try {
            Method m = EditBox.class.getMethod("setFormatter", BiFunction.class);
            m.invoke(box, fmt);
            return;
        } catch (NoSuchMethodException ignored) {
            // method removed — fall through to field reflection
        } catch (Exception e) {
            return;
        }
        // Reflection fallback: set every BiFunction field in the hierarchy
        Class<?> clazz = box.getClass();
        while (clazz != null) {
            for (Field f : clazz.getDeclaredFields()) {
                if (f.getType() == BiFunction.class) {
                    f.setAccessible(true);
                    try { f.set(box, fmt); } catch (IllegalAccessException ignored) {}
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