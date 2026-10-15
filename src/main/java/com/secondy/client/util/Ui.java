package com.secondy.client.util;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/** Мелкие помощники для рисования в стиле iOS: скруглённые прямоугольники, цвета с альфой, плавные переходы. */
public final class Ui {
    private Ui() {}

    /** Умножает альфу цвета на f (0..1). */
    public static int alpha(int color, float f) {
        int a = Math.round((color >>> 24) * Math.max(0f, Math.min(1f, f)));
        return (a << 24) | (color & 0xFFFFFF);
    }

    /** Линейный переход между двумя ARGB-цветами. */
    public static int lerp(int c1, int c2, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int a = (int) (((c1 >>> 24) & 255) + (((c2 >>> 24) & 255) - ((c1 >>> 24) & 255)) * t);
        int r = (int) (((c1 >> 16) & 255) + (((c2 >> 16) & 255) - ((c1 >> 16) & 255)) * t);
        int g = (int) (((c1 >> 8) & 255) + (((c2 >> 8) & 255) - ((c1 >> 8) & 255)) * t);
        int b = (int) ((c1 & 255) + ((c2 & 255) - (c1 & 255)) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int inset(int i, int h, int r) {
        if (r <= 0) return 0;
        double dy;
        if (i < r) dy = r - i - 0.5;
        else if (i >= h - r) dy = i - (h - r) + 0.5;
        else return 0;
        return (int) Math.ceil(r - Math.sqrt(Math.max(0, r * r - dy * dy)));
    }

    /** Скруглённый прямоугольник (каждый пиксель закрашивается один раз — полупрозрачность не «слипается»). */
    public static void round(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) return;
        r = Math.min(r, Math.min(w, h) / 2);
        for (int i = 0; i < h; i++) {
            int d = inset(i, h, r);
            ctx.fill(x + d, y + i, x + w - d, y + i + 1, color);
        }
    }

    /** Тонкая скруглённая обводка 1px. */
    public static void roundOutline(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
        if (w <= 2 || h <= 2) return;
        r = Math.min(r, Math.min(w, h) / 2);
        for (int i = 0; i < h; i++) {
            int d = inset(i, h, r);
            if (i == 0 || i == h - 1) {
                ctx.fill(x + d, y + i, x + w - d, y + i + 1, color);
            } else {
                int d2 = inset(i - 1, h - 2, Math.max(0, r - 1));
                int innerL = x + 1 + d2;
                int innerR = x + w - 1 - d2;
                if (innerL > x + d) ctx.fill(x + d, y + i, innerL, y + i + 1, color);
                if (x + w - d > innerR) ctx.fill(innerR, y + i, x + w - d, y + i + 1, color);
            }
        }
    }

    /** Текст; при почти нулевой альфе не рисуется (иначе Minecraft сделает его непрозрачным). */
    public static void text(DrawContext ctx, TextRenderer tr, String s, int x, int y, int color) {
        if ((color >>> 24) < 8) return;
        ctx.drawText(tr, s, x, y, color, false);
    }

    public static void text(DrawContext ctx, TextRenderer tr, Text t, int x, int y, int color) {
        if ((color >>> 24) < 8) return;
        ctx.drawText(tr, t, x, y, color, false);
    }

    /** Обрезает строку по ширине (без зависимости от версии API TextRenderer). */
    public static String trim(TextRenderer tr, String s, int maxWidth) {
        if (tr.getWidth(s) <= maxWidth) return s;
        int end = s.length();
        while (end > 0 && tr.getWidth(s.substring(0, end)) > maxWidth) end--;
        return s.substring(0, end);
    }

    public static float ease(float t) {
        t = Math.max(0f, Math.min(1f, t));
        return 1f - (1f - t) * (1f - t) * (1f - t);
    }
}
