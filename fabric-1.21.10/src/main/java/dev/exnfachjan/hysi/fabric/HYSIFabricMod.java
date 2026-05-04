package dev.exnfachjan.hysi.fabric;

import dev.exnfachjan.hysi.HYSIMod;
import net.fabricmc.api.ClientModInitializer;

public class HYSIFabricMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HYSIMod.init();
    }
}
