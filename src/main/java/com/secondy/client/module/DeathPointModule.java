package com.secondy.client.module;

import com.secondy.client.settings.ActionSetting;
import com.secondy.client.settings.BoolSetting;
import com.secondy.client.settings.ColorSetting;
import com.secondy.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Запоминает место последней смерти: координаты, дистанция, сторона света. */
public class DeathPointModule extends HudModule {
    private static final String[] SIDES = { "В", "ЮВ", "Ю", "ЮЗ", "З", "СЗ", "С", "СВ" };

    private final BoolSetting showDist = add(new BoolSetting("Дистанция до точки", "show_dist", true));
    private final BoolSetting showSide = add(new BoolSetting("Сторона света", "show_side", true));
    private final BoolSetting showDim = add(new BoolSetting("Измерение", "show_dim", true));
    private final BoolSetting chatMsg = add(new BoolSetting("Писать координаты в чат при смерти", "chat", true));
    private final BoolSetting autoClear = add(new BoolSetting("Убрать, когда подошёл к точке", "auto_clear", false));
    private final NumberSetting clearRadius = add(new NumberSetting("Радиус автоматического сброса", "clear_radius", 5, 2, 30, 1, " м"));
    private final ColorSetting titleColor = add(new ColorSetting("Цвет заголовка", "title_color", 0xFF453A));
    private final ColorSetting textColor = add(new ColorSetting("Цвет текста", "text_color", 0xFFFFFF));
    @SuppressWarnings("unused")
    private final ActionSetting clear = add(new ActionSetting("Удалить точку", () -> this.has = false));

    private boolean has;
    private double dx, dy, dz;
    private String dim = "";
    private boolean wasDead;

    public DeathPointModule() {
        super("Death Point", "Координаты и направление к месту последней смерти", Category.HUD, 200, 180);
    }

    private static String dimName(String path) {
        return switch (path) {
            case "overworld" -> "Верхний мир";
            case "the_nether" -> "Незер";
            case "the_end" -> "Энд";
            default -> path;
        };
    }

    @Override
    public void onTick(MinecraftClient mc) {
        var p = mc.player;
        if (p == null || mc.world == null) return;

        boolean dead = p.isDead();
        if (dead && !wasDead) {
            has = true;
            dx = p.getX();
            dy = p.getY();
            dz = p.getZ();
            dim = mc.world.getRegistryKey().getValue().getPath();
            if (chatMsg.get()) {
                mc.inGameHud.getChatHud().addMessage(Text.literal(String.format(Locale.ROOT,
                        "Точка смерти: %.0f %.0f %.0f (%s)", dx, dy, dz, dimName(dim))));
            }
        }
        wasDead = dead;

        if (has && autoClear.get() && !dead && dim.equals(mc.world.getRegistryKey().getValue().getPath())) {
            double ddx = p.getX() - dx, ddy = p.getY() - dy, ddz = p.getZ() - dz;
            if (Math.sqrt(ddx * ddx + ddy * ddy + ddz * ddz) <= clearRadius.get()) has = false;
        }
    }

    @Override
    public void draw(DrawContext ctx, MinecraftClient mc) {
        var p = mc.player;
        if (p == null || mc.world == null) return;
        boolean preview = mc.currentScreen instanceof ChatScreen;
        if (!has && !preview) return;

        List<String> lines = new ArrayList<>();
        lines.add("Точка смерти");
        if (!has) {
            lines.add("пока нет");
        } else {
            lines.add(String.format(Locale.ROOT, "%.0f / %.0f / %.0f", dx, dy, dz));
            String now = mc.world.getRegistryKey().getValue().getPath();
            boolean same = now.equals(dim);
            if (showDist.get() || showSide.get()) {
                if (same) {
                    double ddx = dx - p.getX(), ddz = dz - p.getZ();
                    StringBuilder sb = new StringBuilder();
                    if (showDist.get()) {
                        double ddy = dy - p.getY();
                        sb.append(String.format(Locale.ROOT, "%.0f м", Math.sqrt(ddx * ddx + ddy * ddy + ddz * ddz)));
                    }
                    if (showSide.get()) {
                        double ang = Math.toDegrees(Math.atan2(ddz, ddx));
                        int idx = ((int) Math.round(ang / 45.0) % 8 + 8) % 8;
                        if (sb.length() > 0) sb.append("  ");
                        sb.append(SIDES[idx]);
                    }
                    lines.add(sb.toString());
                } else {
                    lines.add("в другом измерении");
                }
            }
            if (showDim.get()) lines.add(dimName(dim));
        }

        var tr = mc.textRenderer;
        int w = 0;
        for (String s : lines) w = Math.max(w, tr.getWidth(s));
        width = w + 8;
        height = lines.size() * 10 + 4;
        background(ctx);
        for (int i = 0; i < lines.size(); i++) {
            ctx.drawText(tr, lines.get(i), x + 4, y + 3 + i * 10, i == 0 ? titleColor.get() : textColor.get(), true);
        }
    }
}
