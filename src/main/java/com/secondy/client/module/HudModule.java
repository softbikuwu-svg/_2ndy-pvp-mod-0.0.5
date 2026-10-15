package com.secondy.client.module;

import com.secondy.client.util.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/** Модуль, который рисуется на экране и который можно перетаскивать. */
public abstract class HudModule extends Module {
    public int x, y;
    /** Рисовать тёмную плашку-фон под модулем. */
    public boolean showBackground = true;
    protected int width = 60, height = 14;

    protected HudModule(String name, String description, Category category, int x, int y) {
        super(name, description, category);
        this.x = x;
        this.y = y;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public boolean contains(int mx, int my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }

    public abstract void draw(DrawContext ctx, MinecraftClient mc);

    protected void background(DrawContext ctx) {
        if (!showBackground) return;
        ctx.fill(x, y, x + width, y + height, 0x90000000);
    }

    /** Плашка с несколькими строками текста. */
    protected void lines(DrawContext ctx, MinecraftClient mc, java.util.List<String> text) {
        int w = 0;
        for (String s : text) w = Math.max(w, mc.textRenderer.getWidth(s));
        width = w + 8;
        height = text.size() * 10 + 4;
        background(ctx);
        for (int i = 0; i < text.size(); i++) {
            ctx.drawText(mc.textRenderer, text.get(i), x + 4, y + 3 + i * 10, Theme.color(i * 0.08f), true);
        }
    }

    /** Простая плашка с одной строкой текста. */
    protected void label(DrawContext ctx, MinecraftClient mc, String text) {
        width = mc.textRenderer.getWidth(text) + 8;
        height = 14;
        background(ctx);
        ctx.drawText(mc.textRenderer, text, x + 4, y + 3, Theme.color(0f), true);
    }
}
