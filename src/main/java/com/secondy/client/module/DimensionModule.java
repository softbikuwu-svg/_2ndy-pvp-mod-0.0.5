package com.secondy.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class DimensionModule extends HudModule {
    public DimensionModule() {
        super("Dimension", "В каком измерении ты находишься", Category.HUD, 170, 150);
    }

    @Override
    public void draw(DrawContext ctx, MinecraftClient mc) {
        if (mc.world == null) return;
        String id = mc.world.getRegistryKey().getValue().getPath();
        String name = switch (id) {
            case "overworld" -> "Верхний мир";
            case "the_nether" -> "Незер";
            case "the_end" -> "Энд";
            default -> id;
        };
        label(ctx, mc, "Мир: " + name);
    }
}
