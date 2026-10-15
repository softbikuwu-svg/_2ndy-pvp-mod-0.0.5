package com.secondy.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class ServerModule extends HudModule {
    public ServerModule() {
        super("Server Address", "Адрес сервера, на котором ты играешь", Category.HUD, 6, 228);
    }

    @Override
    public void draw(DrawContext ctx, MinecraftClient mc) {
        var info = mc.getCurrentServerEntry();
        label(ctx, mc, info != null ? info.address : "Одиночная игра");
    }
}
