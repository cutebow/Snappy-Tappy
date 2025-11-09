package me.cutebow.snappytappy;

import me.cutebow.snappytappy.config.STConfig;
import me.cutebow.snappytappy.input.InputInterceptor;
import me.cutebow.snappytappy.input.Keybinds;
import net.fabricmc.api.ClientModInitializer;

public final class SnappyTappyClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        STConfig.load();
        InputInterceptor.register();
        Keybinds.register();
    }
}
