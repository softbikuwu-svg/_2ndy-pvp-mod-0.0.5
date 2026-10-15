package com.secondy.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import net.minecraft.item.ItemStack;

public class ItemCounterModule extends HudModule {
    public ItemCounterModule() {
        super("Item Counter", "Сколько таких предметов, как в руке, всего в инвентаре", Category.HUD, 6, 264);
    }

    @Override
    public void draw(DrawContext ctx, MinecraftClient mc) {
        var p = mc.player;
        if (p == null) return;
        ItemStack held = p.getMainHandStack();
        if (held.isEmpty()) {
            label(ctx, mc, "Предмет: —");
            return;
        }
        int total = 0;
        var inv = p.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack s = inv.getStack(i);
            if (s.isOf(held.getItem())) total += s.getCount();
        }
        label(ctx, mc, held.getName().getString() + ": " + total);
    }
}
