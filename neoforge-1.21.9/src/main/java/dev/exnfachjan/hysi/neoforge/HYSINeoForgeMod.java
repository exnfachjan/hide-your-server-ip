package dev.exnfachjan.hysi.neoforge;

import dev.exnfachjan.hysi.HYSIMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(value = HYSIMod.MOD_ID, dist = Dist.CLIENT)
public class HYSINeoForgeMod {
    public HYSINeoForgeMod() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            HYSIMod.init();
        }
    }
}
