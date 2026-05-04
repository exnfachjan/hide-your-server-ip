package dev.exnfachjan.hysi.mixin;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.BiFunction;

/**
 * Accessor for EditBox's internal formatter field.
 *
 * In Minecraft 26.1 the public setFormatter() method was removed.
 * The field itself (named "formatter" in Mojang's unobfuscated code)
 * still exists — we write to it directly via this accessor.
 */
@Mixin(EditBox.class)
public interface EditBoxFormatterAccessor {

    @Accessor("formatter")
    void hysi$setFormatter(BiFunction<String, Integer, FormattedCharSequence> formatter);
}