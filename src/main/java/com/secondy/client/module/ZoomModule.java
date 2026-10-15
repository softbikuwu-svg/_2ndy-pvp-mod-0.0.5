package com.secondy.client.module;

import com.secondy.client.util.Input;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class ZoomModule extends Module {
    private boolean zooming = false;
    private int saved = 70;

    public ZoomModule() {
        super("Zoom", "Приближение: зажми клавишу C", Category.UTILITY);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        boolean want = mc.currentScreen == null && Input.key(GLFW.GLFW_KEY_C);
        if (want && !zooming) {
            saved = mc.options.getFov().getValue();
            mc.options.getFov().setValue(30);
            zooming = true;
        } else if (!want && zooming) {
            reset(mc);
        }
    }

    @Override
    public void onDisable() {
        if (zooming) reset(MinecraftClient.getInstance());
    }

    private void reset(MinecraftClient mc) {
        mc.options.getFov().setValue(saved);
        zooming = false;
    }
}
