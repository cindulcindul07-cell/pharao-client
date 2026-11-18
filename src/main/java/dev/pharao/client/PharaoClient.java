package dev.pharao.client;

import dev.pharao.client.config.ClientConfig;
import dev.pharao.client.detection.DetectionService;
import dev.pharao.client.hud.PharaoHud;
import dev.pharao.client.screen.PharaoScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public final class PharaoClient implements ClientModInitializer {
    private static boolean rightShiftWasDown;

    @Override
    public void onInitializeClient() {
        ClientConfig.load();
        PharaoHud.register();
        ClientTickEvents.END_CLIENT_TICK.register(PharaoClient::onEndTick);
    }

    private static void onEndTick(MinecraftClient client) {
        if (client.getWindow() == null) return;
        long handle = client.getWindow().getHandle();
        boolean rightShift = GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;

        if (rightShift && !rightShiftWasDown && client.currentScreen == null) {
            client.setScreen(new PharaoScreen());
        }
        rightShiftWasDown = rightShift;

        if (client.currentScreen == null) {
            for (int i = 0; i < ClientConfig.ACTION_COUNT; i++) {
                int key = ClientConfig.actionKeys[i];
                boolean down = key > 0 && GLFW.glfwGetKey(handle, key) == GLFW.GLFW_PRESS;
                if (down && !actionWasDown[i]) {
                    if (i == 0 || i == 2 || i == 3) ClientConfig.toggleAction(i);
                    // Freecam remains unavailable until a camera-only implementation is added.
                }
                actionWasDown[i] = down;
            }
        } else {
            for (int i = 0; i < actionWasDown.length; i++) actionWasDown[i] = false;
        }
        DetectionService.tick(client);
    }
}
