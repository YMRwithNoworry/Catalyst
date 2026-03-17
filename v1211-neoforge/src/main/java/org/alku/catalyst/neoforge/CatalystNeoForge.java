package org.alku.catalyst.neoforge;

import org.alku.catalyst.Catalyst;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Catalyst.MOD_ID)
public final class CatalystNeoForge {
    public CatalystNeoForge(IEventBus modEventBus) {
        Catalyst.init();
    }
}
