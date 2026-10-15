package com.secondy.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class DirectionModule extends HudModule {
    public DirectionModule() {
        super("Direction", "Куда ты смотришь (сторона света)", Category.HUD, 6, 192);
    }

    @Override
    public void draw(DrawContext ctx, MinecraftClient mc) {
        var p = mc.player;
        if (p == null) return;
        Direction d = p.getHorizontalFacing();
        String name = switch (d) {
            case NORTH -> "Север (-Z)";
            case SOUTH -> "Юг (+Z)";
            case EAST -> "Восток (+X)";
            case WEST -> "Запад (-X)";
            default -> d.asString();
        };
        label(ctx, mc, name + "  " + Math.round(MathHelper.wrapDegrees(p.getYaw())) + "°");
    }
}
