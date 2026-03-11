package org.alku.catalyst.fabric.client;

import org.alku.catalyst.client.ClientEventHandler;
import net.fabricmc.api.ClientModInitializer;

public final class CatalystFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientEventHandler.init();
    }
}
