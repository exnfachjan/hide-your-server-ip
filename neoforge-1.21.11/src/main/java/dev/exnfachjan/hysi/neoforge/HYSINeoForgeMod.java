package dev.exnfachjan.hysi.neoforge;

import dev.exnfachjan.hysi.HYSIMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = HYSIMod.MOD_ID, dist = Dist.CLIENT)
public class HYSINeoForgeMod {
    public HYSINeoForgeMod() {
        HYSIMod.init();
    }
}