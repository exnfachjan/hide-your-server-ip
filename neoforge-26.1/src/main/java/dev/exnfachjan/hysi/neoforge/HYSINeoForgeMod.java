package dev.exnfachjan.hysi.neoforge;

import dev.exnfachjan.hysi.HYSIMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

// dist = Dist.CLIENT ensures this only runs on the client.
// No runtime check needed — NeoForge 26.1 removed FMLEnvironment.dist.
@Mod(value = HYSIMod.MOD_ID, dist = Dist.CLIENT)
public class HYSINeoForgeMod {
    public HYSINeoForgeMod() {
        HYSIMod.init();
    }
}