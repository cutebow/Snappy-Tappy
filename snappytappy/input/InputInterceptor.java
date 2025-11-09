package me.cutebow.snappytappy.input;

import me.cutebow.snappytappy.config.STConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public final class InputInterceptor {
    private static final Map<Integer, Boolean> lastApplied = new HashMap<>();
    private static final Map<Integer, Long> pressTick = new HashMap<>();
    private static final Map<Integer, Boolean> prevRaw = new HashMap<>();
    private static long tick = 0;

    public static void init() {
        ClientTickEvents.START_CLIENT_TICK.register(InputInterceptor::onTick);
    }

    public static void register() {
        init();
    }

    private static void onTick(MinecraftClient client) {
        tick++;
        if (client == null) return;

        if (!STConfig.INSTANCE.enabled) {
            if (!lastApplied.isEmpty()) {
                reconcileToRaw(client);
                lastApplied.clear();
            }
            return;
        }

        if (client.currentScreen != null) {
            releaseMovement(client);
            return;
        }

        GameOptions o = client.options;

        int lc = safeCode(KeyBindingHelper.getBoundKeyOf(o.leftKey));
        int rc = safeCode(KeyBindingHelper.getBoundKeyOf(o.rightKey));
        int fc = safeCode(KeyBindingHelper.getBoundKeyOf(o.forwardKey));
        int bc = safeCode(KeyBindingHelper.getBoundKeyOf(o.backKey));
        int jc = safeCode(KeyBindingHelper.getBoundKeyOf(o.jumpKey));
        int sc = safeCode(KeyBindingHelper.getBoundKeyOf(o.sneakKey));
        int spc = safeCode(KeyBindingHelper.getBoundKeyOf(o.sprintKey));

        List<KeyBinding> movementKeys = Arrays.asList(
                o.leftKey, o.rightKey, o.forwardKey, o.backKey, o.jumpKey, o.sneakKey, o.sprintKey
        );

        Map<Integer, List<KeyBinding>> byCode = new HashMap<>();
        Map<Integer, InputUtil.Key> repr = new HashMap<>();
        for (KeyBinding kb : movementKeys) {
            InputUtil.Key bound = KeyBindingHelper.getBoundKeyOf(kb);
            if (bound == null) continue;
            int code = bound.getCode();
            if (code < 0) continue;
            byCode.computeIfAbsent(code, k -> new ArrayList<>()).add(kb);
            repr.putIfAbsent(code, bound);
        }

        long win = client.getWindow().getHandle();
        Map<Integer, Boolean> raw = new HashMap<>();
        for (int code : byCode.keySet()) {
            InputUtil.Key sample = repr.get(code);
            boolean mouse = sample != null && sample.getCategory() == InputUtil.Type.MOUSE;
            boolean p = mouse
                    ? GLFW.glfwGetMouseButton(win, sample.getCode()) == GLFW.GLFW_PRESS
                    : InputUtil.isKeyPressed(win, code);
            raw.put(code, p);
            boolean was = Boolean.TRUE.equals(prevRaw.get(code));
            if (p && !was) pressTick.put(code, tick);
        }

        Set<Integer> managed = new HashSet<>(byCode.keySet());

        Set<Integer> forcedTrue = new HashSet<>();
        Set<Integer> forcedFalse = new HashSet<>();
        if (STConfig.INSTANCE.snappyEnabled) {
            resolvePair(raw, pressTick, lc, rc, forcedTrue, forcedFalse);
            resolvePair(raw, pressTick, fc, bc, forcedTrue, forcedFalse);
        }

        for (int code : managed) {
            boolean val = raw.getOrDefault(code, false);
            if (forcedFalse.contains(code)) val = false;
            if (forcedTrue.contains(code)) val = true;
            List<KeyBinding> list = byCode.get(code);
            if (list == null) continue;
            for (KeyBinding kb : list) kb.setPressed(val);
            lastApplied.put(code, val);
        }

        prevRaw.clear();
        prevRaw.putAll(raw);
    }

    private static void reconcileToRaw(MinecraftClient client) {
        GameOptions o = client.options;
        List<KeyBinding> movementKeys = Arrays.asList(
                o.leftKey, o.rightKey, o.forwardKey, o.backKey, o.jumpKey, o.sneakKey, o.sprintKey
        );

        Map<Integer, List<KeyBinding>> byCode = new HashMap<>();
        Map<Integer, InputUtil.Key> repr = new HashMap<>();
        for (KeyBinding kb : movementKeys) {
            InputUtil.Key bound = KeyBindingHelper.getBoundKeyOf(kb);
            if (bound == null) continue;
            int code = bound.getCode();
            if (!lastApplied.containsKey(code) || code < 0) continue;
            byCode.computeIfAbsent(code, k -> new ArrayList<>()).add(kb);
            repr.putIfAbsent(code, bound);
        }

        long win = client.getWindow().getHandle();
        for (Map.Entry<Integer, List<KeyBinding>> e : byCode.entrySet()) {
            int code = e.getKey();
            InputUtil.Key sample = repr.get(code);
            boolean mouse = sample != null && sample.getCategory() == InputUtil.Type.MOUSE;
            boolean raw = mouse
                    ? GLFW.glfwGetMouseButton(win, sample.getCode()) == GLFW.GLFW_PRESS
                    : InputUtil.isKeyPressed(win, code);
            for (KeyBinding kb : e.getValue()) kb.setPressed(raw);
        }
    }

    private static void resolvePair(Map<Integer, Boolean> raw, Map<Integer, Long> times, int a, int b, Set<Integer> t, Set<Integer> f) {
        boolean pa = raw.getOrDefault(a, false);
        boolean pb = raw.getOrDefault(b, false);
        if (pa && pb) {
            long ta = times.getOrDefault(a, 0L);
            long tb = times.getOrDefault(b, 0L);
            if (ta >= tb) { t.add(a); f.add(b); } else { t.add(b); f.add(a); }
        }
    }

    private static int safeCode(InputUtil.Key k) {
        return k == null ? InputUtil.UNKNOWN_KEY.getCode() : k.getCode();
    }

    private static void releaseMovement(MinecraftClient client) {
        if (client == null) return;
        GameOptions o = client.options;
        List<KeyBinding> keys = Arrays.asList(o.leftKey, o.rightKey, o.forwardKey, o.backKey, o.jumpKey, o.sneakKey, o.sprintKey);
        for (KeyBinding kb : keys) kb.setPressed(false);
        for (KeyBinding kb : keys) {
            InputUtil.Key bound = KeyBindingHelper.getBoundKeyOf(kb);
            if (bound != null) lastApplied.put(bound.getCode(), false);
        }
    }
}
