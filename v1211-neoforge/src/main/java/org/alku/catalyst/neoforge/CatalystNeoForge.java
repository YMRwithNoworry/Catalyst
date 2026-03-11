package org.alku.catalyst.neoforge;

import org.alku.catalyst.Catalyst;
import dev.architectury.platform.neoforge.EventBuses;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Catalyst.MOD_ID)
public final class CatalystNeoForge {
    public CatalystNeoForge(IEventBus modEventBus) {
        EventBuses.registerModEventBus(Catalyst.MOD_ID, modEventBus);
        Catalyst.init();
    }
}
