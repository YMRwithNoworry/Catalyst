package org.alku.catalyst.forge;

import org.alku.catalyst.Catalyst;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Catalyst.MOD_ID)
public final class CatalystForge {
    public CatalystForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(Catalyst.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        Catalyst.init();
    }
}
