package com.secondy.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ClockModule extends HudModule {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ClockModule() {
        super("Clock", "Реальное время", Category.HUD, 6, 138);
    }

    @Override
    public void draw(DrawContext ctx, MinecraftClient mc) {
        label(ctx, mc, "Время: " + LocalTime.now().format(FMT));
    }
}
