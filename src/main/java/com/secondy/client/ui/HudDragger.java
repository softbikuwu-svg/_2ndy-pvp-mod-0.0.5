package com.secondy.client.ui;

import com.secondy.client.module.HudModule;
import com.secondy.client.module.ModuleManager;
import com.secondy.client.util.Config;
import com.secondy.client.util.Input;
import com.secondy.client.util.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

/** Перетаскивание HUD-элементов мышкой, пока открыт чат. */
public final class HudDragger {
    private static HudModule dragging;
    private static int offX, offY;
    private static boolean wasDown;

    private HudDragger() {}

    public static void update(DrawContext ctx, MinecraftClient mc, int mx, int my) {
        boolean down = Input.mouse(0);
        List<HudModule> huds = ModuleManager.huds();

        if (down && !wasDown) {
            for (int i = huds.size() - 1; i >= 0; i--) {
                HudModule h = huds.get(i);
                if (h.isEnabled() && h.contains(mx, my)) {
                    dragging = h;
                    offX = mx - h.x;
                    offY = my - h.y;
                    break;
                }
            }
        }
        if (!down && dragging != null) {
            dragging = null;
            Config.save();
        }
        if (dragging != null) {
            int maxX = Math.max(0, mc.getWindow().getScaledWidth() - dragging.getWidth());
            int maxY = Math.max(0, mc.getWindow().getScaledHeight() - dragging.getHeight());
            dragging.x = Math.max(0, Math.min(maxX, mx - offX));
            dragging.y = Math.max(0, Math.min(maxY, my - offY));
        }
        wasDown = down;

        for (HudModule h : huds) {
            if (!h.isEnabled()) continue;
            int color = h == dragging ? 0xFF55FF55 : (h.contains(mx, my) ? 0xFFFFFFFF : 0x80FFFFFF);
            Theme.outline(ctx, h.x - 1, h.y - 1, h.getWidth() + 2, h.getHeight() + 2, color);
        }
    }
}
