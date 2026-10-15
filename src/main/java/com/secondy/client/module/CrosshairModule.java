package com.secondy.client.module;

import com.secondy.client.settings.BoolSetting;
import com.secondy.client.settings.ColorSetting;
import com.secondy.client.settings.ModeSetting;
import com.secondy.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

/** Свой прицел: стиль, размер, толщина, зазор, цвет, обводка, смена цвета на цели. */
public class CrosshairModule extends Module {
    private final ModeSetting style = add(new ModeSetting("Стиль", "style", 0,
            "Крест", "Точка", "Крест + точка", "Квадрат"));
    private final NumberSetting size = add(new NumberSetting("Длина линий", "size", 5, 1, 20, 1, " px"));
    private final NumberSetting thickness = add(new NumberSetting("Толщина", "thickness", 1, 1, 5, 1, " px"));
    private final NumberSetting gap = add(new NumberSetting("Зазор от центра", "gap", 3, 0, 15, 1, " px"));
    private final NumberSetting opacity = add(new NumberSetting("Прозрачность", "opacity", 100, 10, 100, 5, "%"));
    private final BoolSetting outline = add(new BoolSetting("Чёрная обводка", "outline", true));
    private final BoolSetting dynamic = add(new BoolSetting("Динамика (шире при беге и прыжке)", "dynamic", false));
    private final BoolSetting onTarget = add(new BoolSetting("Менять цвет на цели", "on_target", true));
    private final BoolSetting hideVanilla = add(new BoolSetting("Скрыть ванильный прицел", "hide_vanilla", true));
    private final ColorSetting color = add(new ColorSetting("Цвет", "color", 0xFFFFFF));
    private final ColorSetting targetColor = add(new ColorSetting("Цвет на цели", "target_color", 0xFF453A));

    private float dyn = 0f;

    public CrosshairModule() {
        super("Custom Crosshair", "Свой прицел с полной настройкой", Category.VISUAL);
    }

    /** Нужно ли скрыть ванильный прицел (вызывается из SecondyClient). */
    public boolean hidesVanilla() {
        return isEnabled() && hideVanilla.get();
    }

    public void draw(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null || !mc.options.getPerspective().isFirstPerson()) return;

        int cx = mc.getWindow().getScaledWidth() / 2;
        int cy = mc.getWindow().getScaledHeight() / 2;

        float want = 0f;
        if (dynamic.get()) {
            var p = mc.player;
            if (p.isSprinting()) want = 3f;
            else if (!p.isOnGround()) want = 4f;
            else if (p.getVelocity().horizontalLengthSquared() > 0.0004) want = 2f;
        }
        dyn += (want - dyn) * 0.2f;

        int a = Math.round(255f * opacity.getInt() / 100f);
        int rgb = (onTarget.get() && mc.targetedEntity != null) ? targetColor.current(0f) : color.current(0f);
        int fillCol = (a << 24) | rgb;
        int outCol = a << 24;

        int t = thickness.getInt();
        int h = t / 2;
        int g = gap.getInt() + Math.round(dyn);
        int len = size.getInt();

        List<int[]> rects = new ArrayList<>();
        int s = style.index();
        if (s == 0 || s == 2) {
            rects.add(new int[]{cx - h, cy - g - len, cx - h + t, cy - g});        // вверх
            rects.add(new int[]{cx - h, cy + g, cx - h + t, cy + g + len});        // вниз
            rects.add(new int[]{cx - g - len, cy - h, cx - g, cy - h + t});        // влево
            rects.add(new int[]{cx + g, cy - h, cx + g + len, cy - h + t});        // вправо
        }
        if (s == 1 || s == 2) {
            int d = t + 1;
            rects.add(new int[]{cx - d / 2, cy - d / 2, cx - d / 2 + d, cy - d / 2 + d});
        }
        if (s == 3) {
            int half = g + len;
            rects.add(new int[]{cx - half, cy - half, cx + half, cy - half + t});  // верх
            rects.add(new int[]{cx - half, cy + half - t, cx + half, cy + half});  // низ
            rects.add(new int[]{cx - half, cy - half + t, cx - half + t, cy + half - t}); // лево
            rects.add(new int[]{cx + half - t, cy - half + t, cx + half, cy + half - t}); // право
        }

        if (outline.get()) {
            for (int[] r : rects) ctx.fill(r[0] - 1, r[1] - 1, r[2] + 1, r[3] + 1, outCol);
        }
        for (int[] r : rects) ctx.fill(r[0], r[1], r[2], r[3], fillCol);
    }
}
