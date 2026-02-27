package org.alku.catalyst;

import org.alku.catalyst.config.CatalystConfig;

public final class Catalyst {
    public static final String MOD_ID = "catalyst";

    public static void init() {
        CatalystConfig.getInstance();
    }
}
