package dev.exnfachjan.hysi.mixin;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.BiFunction;

/**
 * Accessor for EditBox's internal formatter field.
 *
 * setFormatter() was removed somewhere between 1.21.5 and 1.21.11.
 * Writing directly to the field works across all versions in this range.
 */
@Mixin(EditBox.class)
public interface EditBoxFormatterAccessor {

    @Accessor("formatter")
    void hysi$setFormatter(BiFunction<String, Integer, FormattedCharSequence> formatter);
}