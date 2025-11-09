package me.cutebow.snappytappy.input;

import me.cutebow.snappytappy.config.STConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class Keybinds {
    private static KeyBinding toggle;

    public static void register() {
        toggle = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.snappy_tappy.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_GRAVE_ACCENT,
                "key.categories.misc"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(Keybinds::tick);
    }

    private static void tick(MinecraftClient client) {
        if (client == null) return;
        while (toggle.wasPressed()) {
            STConfig.INSTANCE.enabled = !STConfig.INSTANCE.enabled;
            STConfig.save();
            InGameHud hud = client.inGameHud;
            if (hud != null) {
                hud.setOverlayMessage(Text.literal("Snappy Tappy: " + (STConfig.INSTANCE.enabled ? "ON" : "OFF")), false);
            }
        }
    }
}
