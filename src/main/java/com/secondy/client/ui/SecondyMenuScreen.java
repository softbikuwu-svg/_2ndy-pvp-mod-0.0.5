package com.secondy.client.ui;

import com.secondy.client.module.Category;
import com.secondy.client.module.HudModule;
import com.secondy.client.module.Module;
import com.secondy.client.module.ModuleManager;
import com.secondy.client.settings.*;
import com.secondy.client.util.Config;
import com.secondy.client.util.Input;
import com.secondy.client.util.Theme;
import com.secondy.client.util.Ui;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Меню модулей в стиле iOS: тёмное стеклянное окно, сегмент-контрол сверху, список с переключателями и вкладка цвета. */
public class SecondyMenuScreen extends Screen {
    // Размеры
    private static final int PW = 400, PH = 280, PAD = 12, RADIUS = 9;
    private static final int ROW_H = 26;
    private static final int LIST_TOP = 56;   // отступ карточки от верха окна
    private static final int FOOTER = 22;     // место под подсказку внизу

    // Цвета (ARGB)
    private static final int PANEL        = 0xB8000000; // чёрное стекло
    private static final int PANEL_BORDER = 0x2EFFFFFF;
    private static final int CARD         = 0x1CFFFFFF;
    private static final int CARD_HOVER   = 0x14FFFFFF;
    private static final int SEPARATOR    = 0x1AFFFFFF;
    private static final int SEG_TRACK    = 0x1FFFFFFF;
    private static final int TEXT         = 0xFFFFFFFF;
    private static final int TEXT_DIM     = 0xFF8E8E93;
    private static final int SW_OFF       = 0xFF39393D;

    /** Готовые цвета (как в iOS). */
    private static final int[] PRESETS = {
            0x0A84FF, 0x30D158, 0xFF453A, 0xFF9F0A, 0xFFD60A, 0xFF375F, 0xBF5AF2, 0x64D2FF
    };

    /** Готовые цвета для настроек модулей (+ белый). */
    private static final int[] SET_PRESETS = {
            0x0A84FF, 0x30D158, 0xFF453A, 0xFF9F0A, 0xFFD60A, 0xFF375F, 0xBF5AF2, 0x64D2FF, 0xFFFFFF
    };
    private static final int COLOR_ROW_H = 116, SET_ROW_H = 22;

    private Category selected = Category.HUD;
    private boolean wasDown;
    private boolean shiftWasDown;

    private long openedAt;
    private long lastFrame;
    private float selPos = 0f;
    private final Map<Module, Float> anim = new HashMap<>();   // переключатели модулей
    private final Map<Module, Float> bgAnim = new HashMap<>(); // переключатели «Фон»
    private float hudSw;
    private float closeHover;
    private int scrollRow = 0;
    private int dragSlider = -1;

    // Страница настроек модуля
    private Module openModule;
    private int setScroll = 0;
    private final Map<Object, Float> swAnim = new HashMap<>();
    private NumberSetting dragNum;
    private int dragNumX, dragNumW;
    private ColorSetting dragCol;
    private int dragCh, dragColX, dragColW;

    public SecondyMenuScreen() {
        super(Text.literal("_2ndy Client"));
    }

    @Override
    protected void init() {
        wasDown = Input.mouse(0);
        shiftWasDown = Input.key(GLFW.GLFW_KEY_RIGHT_SHIFT);
        openedAt = System.currentTimeMillis();
        lastFrame = System.nanoTime();
        selPos = selected.ordinal();
        hudSw = Theme.hudAccent ? 1f : 0f;
        dragSlider = -1;
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void close() {
        Config.save();
        super.close();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (openModule != null && selected != Category.COLOR) {
            setScroll = Math.max(0, setScroll - (int) Math.signum(verticalAmount));
        } else {
            scrollRow = Math.max(0, scrollRow - (int) Math.signum(verticalAmount));
        }
        return true;
    }

    private List<Module> modules() {
        List<Module> list = new ArrayList<>();
        for (Module m : ModuleManager.ALL) if (m.category == selected) list.add(m);
        return list;
    }

    private static float step(float cur, float target, float dt) {
        return cur + Math.signum(target - cur) * Math.min(Math.abs(target - cur), dt * 7f);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);

        boolean shift = Input.key(GLFW.GLFW_KEY_RIGHT_SHIFT);
        if (shift && !shiftWasDown) { close(); return; }
        shiftWasDown = shift;

        boolean down = Input.mouse(0);
        boolean clicked = down && !wasDown;
        wasDown = down;

        long nowN = System.nanoTime();
        float dt = Math.min(0.1f, (nowN - lastFrame) / 1_000_000_000f);
        lastFrame = nowN;

        // Анимация появления
        float open = Ui.ease((System.currentTimeMillis() - openedAt) / 220f);
        int ph = Math.min(PH, this.height - 16);
        int px = (this.width - PW) / 2;
        int py = (this.height - ph) / 2 + Math.round((1f - open) * 10f);

        // Окно
        Ui.round(ctx, px, py, PW, ph, RADIUS, Ui.alpha(PANEL, open));
        Ui.roundOutline(ctx, px, py, PW, ph, RADIUS, Ui.alpha(PANEL_BORDER, open));

        // Заголовок
        Ui.text(ctx, this.textRenderer, Text.literal("_2ndy Client").formatted(Formatting.BOLD),
                px + PAD, py + 11, Ui.alpha(Theme.color(0f), open));

        // Кнопка закрытия
        int cr = 6, ccx = px + PW - PAD - cr, ccy = py + 10 + cr - 1;
        boolean closeOver = mx >= ccx - cr && mx <= ccx + cr && my >= ccy - cr && my <= ccy + cr;
        closeHover += ((closeOver ? 1f : 0f) - closeHover) * Math.min(1f, dt * 14f);
        Ui.round(ctx, ccx - cr, ccy - cr, cr * 2, cr * 2, cr,
                Ui.alpha(Ui.lerp(0x33FFFFFF, 0x59FFFFFF, closeHover), open));
        int xc = Ui.alpha(0xFFFFFFFF, open);
        for (int i = -2; i <= 2; i++) {
            ctx.fill(ccx + i, ccy + i, ccx + i + 1, ccy + i + 1, xc);
            ctx.fill(ccx + i, ccy - i, ccx + i + 1, ccy - i + 1, xc);
        }
        if (clicked && closeOver) { close(); return; }

        // Сегмент-контрол (вкладки)
        Category[] cats = Category.values();
        int sx = px + PAD, sy = py + 28, sw = PW - PAD * 2, sh = 18;
        int tw = sw / cats.length;
        Ui.round(ctx, sx, sy, tw * cats.length, sh, 7, Ui.alpha(SEG_TRACK, open));

        selPos += (selected.ordinal() - selPos) * Math.min(1f, dt * 16f);
        Ui.round(ctx, sx + 2 + Math.round(selPos * tw), sy + 2, tw - 4, sh - 4, 5,
                Ui.alpha(Theme.accentColor(0f), 0.85f * open));

        for (int i = 0; i < cats.length; i++) {
            int tx = sx + i * tw;
            boolean hover = in(mx, my, tx, sy, tw, sh);
            String t = cats[i].label;
            int base = cats[i] == selected ? TEXT : (hover ? 0xFFEBEBF5 : 0xFF9A9AA2);
            Ui.text(ctx, this.textRenderer, t,
                    tx + (tw - this.textRenderer.getWidth(t)) / 2, sy + 5, Ui.alpha(base, open));
            if (clicked && hover && cats[i] != selected) {
                selected = cats[i];
                scrollRow = 0;
                openModule = null;
                dragNum = null;
                dragCol = null;
            }
        }

        int cx = px + PAD, cy = py + LIST_TOP, cw = PW - PAD * 2;

        if (selected == Category.COLOR) {
            renderColor(ctx, mx, my, clicked, down, dt, open, cx, cy, cw);
        } else if (openModule != null) {
            renderSettings(ctx, mx, my, clicked, down, dt, open, cx, cy, cw, px, ph);
        } else {
            renderList(ctx, mx, my, clicked, dt, open, cx, cy, cw, px, ph);
        }

        // Подсказка внизу
        String foot = "Right Shift — закрыть   •   Чат (T) — двигать HUD   •   Настр. — настройки модуля";
        Ui.text(ctx, this.textRenderer, foot,
                px + (PW - this.textRenderer.getWidth(foot)) / 2, py + ph - 16, Ui.alpha(0xFF6C6C70, open));
    }

    // ───────────────────────── Список модулей ─────────────────────────

    private void renderList(DrawContext ctx, int mx, int my, boolean clicked, float dt, float open,
                            int cx, int cy, int cw, int px, int ph) {
        List<Module> list = modules();
        int rowsFit = Math.max(3, (ph - LIST_TOP - FOOTER) / ROW_H);
        int visible = Math.min(list.size(), rowsFit);
        int maxScroll = Math.max(0, list.size() - visible);
        scrollRow = Math.max(0, Math.min(maxScroll, scrollRow));

        int ch = visible * ROW_H;
        Ui.round(ctx, cx, cy, cw, ch, 7, Ui.alpha(CARD, open));

        for (int i = 0; i < visible; i++) {
            Module m = list.get(scrollRow + i);
            int ry = cy + i * ROW_H;
            boolean hover = in(mx, my, cx, ry, cw, ROW_H);

            if (hover) Ui.round(ctx, cx + 1, ry + 1, cw - 2, ROW_H - 2, 6, Ui.alpha(CARD_HOVER, open));
            if (i > 0) ctx.fill(cx + 10, ry, cx + cw, ry + 1, Ui.alpha(SEPARATOR, open));

            // Раскладка справа налево: переключатель, «Фон», «Настр.»
            int tgw = 26, tgh = 14;
            int tgx = cx + cw - tgw - 10, tgy = ry + (ROW_H - tgh) / 2;
            int left = tgx - 8;

            boolean isHud = m instanceof HudModule;
            int mw = 20, mh = 10;
            int mxs = tgx - 16 - mw;
            String lbl = "Фон";
            int lblW = this.textRenderer.getWidth(lbl);
            if (isHud) left = mxs - 4 - lblW - 10;

            boolean hasSettings = !m.settings.isEmpty();
            String gl = "Настр.";
            int gw = this.textRenderer.getWidth(gl) + 10;
            int gx = left - gw;
            if (hasSettings) left = gx - 6;

            Ui.text(ctx, this.textRenderer, m.name, cx + 10, ry + 5, Ui.alpha(TEXT, open));
            String desc = Ui.trim(this.textRenderer, m.description, Math.max(20, left - (cx + 10)));
            Ui.text(ctx, this.textRenderer, desc, cx + 10, ry + 15, Ui.alpha(TEXT_DIM, open));

            // Главный переключатель
            float target = m.isEnabled() ? 1f : 0f;
            float cur = step(anim.getOrDefault(m, target), target, dt);
            anim.put(m, cur);
            drawSwitch(ctx, tgx, tgy, tgw, tgh, Ui.ease(cur), open);

            // Мини-переключатель «Фон» (только для HUD-модулей)
            boolean bgHit = false;
            if (m instanceof HudModule h) {
                float bt = h.showBackground ? 1f : 0f;
                float bc = step(bgAnim.getOrDefault(m, bt), bt, dt);
                bgAnim.put(m, bc);
                int mys = ry + (ROW_H - mh) / 2;
                drawSwitch(ctx, mxs, mys, mw, mh, Ui.ease(bc), open);
                Ui.text(ctx, this.textRenderer, lbl, mxs - 4 - lblW, ry + (ROW_H - 8) / 2, Ui.alpha(TEXT_DIM, open));
                bgHit = in(mx, my, mxs - 4 - lblW, ry, mw + 8 + lblW, ROW_H);
                if (clicked && bgHit) {
                    h.showBackground = !h.showBackground;
                    Config.save();
                    continue;
                }
            }

            // Кнопка «Настр.»
            boolean gearHit = false;
            if (hasSettings) {
                gearHit = in(mx, my, gx, ry, gw, ROW_H);
                Ui.round(ctx, gx, ry + (ROW_H - 14) / 2, gw, 14, 6,
                        Ui.alpha(gearHit ? 0x4AFFFFFF : 0x2EFFFFFF, open));
                Ui.text(ctx, this.textRenderer, gl, gx + 5, ry + (ROW_H - 8) / 2, Ui.alpha(TEXT, open));
                if (clicked && gearHit) {
                    openModule = m;
                    setScroll = 0;
                    return;
                }
            }

            if (clicked && hover && !bgHit && !gearHit) {
                m.toggle();
                Config.save();
            }
        }

        // Полоса прокрутки
        if (list.size() > visible) {
            int barH = Math.max(12, ch * visible / list.size());
            int barY = cy + (maxScroll == 0 ? 0 : (ch - barH) * scrollRow / maxScroll);
            Ui.round(ctx, px + PW - 7, barY, 3, barH, 1, Ui.alpha(0x55FFFFFF, open));
        }
    }

    // ───────────────────────── Страница настроек модуля ─────────────────────────

    private static int rowH(Setting s) {
        return s instanceof ColorSetting ? COLOR_ROW_H : SET_ROW_H;
    }

    private void renderSettings(DrawContext ctx, int mx, int my, boolean clicked, boolean down, float dt,
                                float open, int cx, int cy, int cw, int px, int ph) {
        Module m = openModule;

        // Шапка: «< Назад», имя модуля, главный переключатель
        String back = "< Назад";
        int bw = this.textRenderer.getWidth(back) + 14;
        boolean backHover = in(mx, my, cx, cy, bw, 18);
        Ui.round(ctx, cx, cy, bw, 18, 6, Ui.alpha(backHover ? 0x40FFFFFF : 0x26FFFFFF, open));
        Ui.text(ctx, this.textRenderer, back, cx + 7, cy + 5, Ui.alpha(TEXT, open));
        Ui.text(ctx, this.textRenderer, m.name, cx + bw + 10, cy + 5, Ui.alpha(Theme.color(0f), open));

        float target = m.isEnabled() ? 1f : 0f;
        float cur = step(anim.getOrDefault(m, target), target, dt);
        anim.put(m, cur);
        int tgw = 26, tgh = 14, tgx = cx + cw - tgw - 4, tgy = cy + 2;
        drawSwitch(ctx, tgx, tgy, tgw, tgh, Ui.ease(cur), open);

        if (clicked && backHover) {
            openModule = null;
            dragNum = null;
            dragCol = null;
            Config.save();
            return;
        }
        if (clicked && in(mx, my, tgx - 4, cy, tgw + 8, 18)) {
            m.toggle();
            Config.save();
        }

        // Строки настроек (прокрутка по строкам; показываем только те, что целиком помещаются)
        List<Setting> list = m.settings;
        int top = cy + 24;
        int avail = ph - LIST_TOP - FOOTER - 24;

        int maxScroll = 0, sum = 0;
        for (int i = list.size() - 1; i >= 0; i--) {
            sum += rowH(list.get(i));
            if (sum > avail) { maxScroll = i + 1; break; }
        }
        setScroll = Math.max(0, Math.min(maxScroll, setScroll));

        int used = 0, end = setScroll;
        while (end < list.size() && used + rowH(list.get(end)) <= avail) {
            used += rowH(list.get(end));
            end++;
        }
        Ui.round(ctx, cx, top, cw, used, 7, Ui.alpha(CARD, open));

        int ry = top;
        for (int i = setScroll; i < end; i++) {
            Setting st = list.get(i);
            int h = rowH(st);
            if (i > setScroll) ctx.fill(cx + 10, ry, cx + cw, ry + 1, Ui.alpha(SEPARATOR, open));
            drawSetting(ctx, st, mx, my, clicked, dt, open, cx, ry, cw, h);
            ry += h;
        }

        // Полоса прокрутки
        if (maxScroll > 0) {
            int barH = Math.max(12, used * (end - setScroll) / list.size());
            int barY = top + (used - barH) * setScroll / maxScroll;
            Ui.round(ctx, px + PW - 7, barY, 3, barH, 1, Ui.alpha(0x55FFFFFF, open));
        }

        // Перетаскивание ползунков
        if (!down) {
            if (dragNum != null || dragCol != null) Config.save();
            dragNum = null;
            dragCol = null;
        } else {
            if (dragNum != null) {
                dragNum.setFraction((mx - dragNumX) / (float) dragNumW);
            }
            if (dragCol != null) {
                int v = Math.round((mx - dragColX) / (float) (dragColW - 1) * 255f);
                v = Math.max(0, Math.min(255, v));
                int rgb = dragCol.rgb();
                int r = (rgb >> 16) & 255, g = (rgb >> 8) & 255, b = rgb & 255;
                if (dragCh == 0) r = v; else if (dragCh == 1) g = v; else b = v;
                dragCol.setRgb((r << 16) | (g << 8) | b);
            }
        }
    }

    private void drawSetting(DrawContext ctx, Setting st, int mx, int my, boolean clicked, float dt, float open,
                             int cx, int ry, int cw, int h) {
        if (st instanceof BoolSetting b) {
            Ui.text(ctx, this.textRenderer, b.name, cx + 10, ry + 7, Ui.alpha(TEXT, open));
            float t = b.get() ? 1f : 0f;
            float k = step(swAnim.getOrDefault(b, t), t, dt);
            swAnim.put(b, k);
            drawSwitch(ctx, cx + cw - 26 - 10, ry + 4, 26, 14, Ui.ease(k), open);
            if (clicked && in(mx, my, cx, ry, cw, h)) {
                b.toggle();
                Config.save();
            }
        } else if (st instanceof NumberSetting n) {
            Ui.text(ctx, this.textRenderer, n.name, cx + 10, ry + 7, Ui.alpha(TEXT, open));
            int trackX = cx + 150, trackW = cw - 150 - 58;
            int fw = Math.round(n.fraction() * trackW);
            Ui.round(ctx, trackX, ry + 9, trackW, 4, 2, Ui.alpha(0x33FFFFFF, open));
            Ui.round(ctx, trackX, ry + 9, Math.max(3, fw), 4, 2, Ui.alpha(Theme.accentColor(0f), open));
            Ui.round(ctx, trackX + fw - 5, ry + 6, 10, 10, 5, Ui.alpha(0xFFFFFFFF, open));
            Ui.roundOutline(ctx, trackX + fw - 5, ry + 6, 10, 10, 5, Ui.alpha(0x55000000, open));
            String val = n.display();
            Ui.text(ctx, this.textRenderer, val, cx + cw - 10 - this.textRenderer.getWidth(val), ry + 7,
                    Ui.alpha(TEXT_DIM, open));
            if (clicked && in(mx, my, trackX - 6, ry, trackW + 12, h)) {
                dragNum = n;
                dragNumX = trackX;
                dragNumW = trackW;
            }
        } else if (st instanceof ModeSetting md) {
            Ui.text(ctx, this.textRenderer, md.name, cx + 10, ry + 7, Ui.alpha(TEXT, open));
            String val = md.get();
            int pw = this.textRenderer.getWidth(val) + 14;
            int pxx = cx + cw - 10 - pw;
            boolean hv = in(mx, my, cx, ry, cw, h);
            Ui.round(ctx, pxx, ry + 3, pw, 16, 6, Ui.alpha(hv ? 0x4AFFFFFF : 0x2EFFFFFF, open));
            Ui.text(ctx, this.textRenderer, val, pxx + 7, ry + 7, Ui.alpha(TEXT, open));
            if (clicked && hv) {
                md.next();
                Config.save();
            }
        } else if (st instanceof FileSetting fs) {
            Ui.text(ctx, this.textRenderer, fs.name, cx + 10, ry + 7, Ui.alpha(TEXT, open));
            String val = fs.display();
            boolean cut = false;
            while (val.length() > 1 && this.textRenderer.getWidth(val + (cut ? ".." : "")) > 150) {
                val = val.substring(0, val.length() - 1);
                cut = true;
            }
            if (cut) val += "..";
            int pw = this.textRenderer.getWidth(val) + 14;
            int pxx = cx + cw - 10 - pw;
            boolean hv = in(mx, my, cx, ry, cw, h);
            Ui.round(ctx, pxx, ry + 3, pw, 16, 6, Ui.alpha(hv ? 0x4AFFFFFF : 0x2EFFFFFF, open));
            Ui.text(ctx, this.textRenderer, val, pxx + 7, ry + 7, Ui.alpha(TEXT, open));
            if (clicked && hv) {
                fs.next();
                Config.save();
            }
        } else if (st instanceof ActionSetting a) {
            boolean hv = in(mx, my, cx, ry, cw, h);
            Ui.round(ctx, cx + 10, ry + 3, cw - 20, 16, 6,
                    Ui.alpha(hv ? Ui.alpha(Theme.accentColor(0f), 0.55f) : Ui.alpha(Theme.accentColor(0f), 0.35f), open));
            Ui.text(ctx, this.textRenderer, a.name, cx + (cw - this.textRenderer.getWidth(a.name)) / 2, ry + 7,
                    Ui.alpha(TEXT, open));
            if (clicked && hv) {
                a.run();
                Config.save();
            }
        } else if (st instanceof ColorSetting c) {
            drawColorSetting(ctx, c, mx, my, clicked, dt, open, cx, ry, cw);
        }
    }

    private void drawColorSetting(DrawContext ctx, ColorSetting c, int mx, int my, boolean clicked, float dt,
                                  float open, int cx, int ry, int cw) {
        int rgb = c.rgb();
        int[] ch = { (rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255 };

        // Заголовок: имя, hex и превью текущего цвета
        Ui.text(ctx, this.textRenderer, c.name, cx + 10, ry + 7, Ui.alpha(TEXT, open));
        Ui.round(ctx, cx + cw - 10 - 12, ry + 4, 12, 12, 6, Ui.alpha(c.get(), open));
        String hex = String.format("#%06X", rgb);
        Ui.text(ctx, this.textRenderer, hex, cx + cw - 10 - 12 - 6 - this.textRenderer.getWidth(hex), ry + 7,
                Ui.alpha(TEXT_DIM, open));

        // Готовые цвета
        int swY = ry + 24, swD = 14, gap = 10;
        for (int i = 0; i < SET_PRESETS.length; i++) {
            int sxp = cx + 10 + i * (swD + gap);
            Ui.round(ctx, sxp, swY, swD, swD, swD / 2, Ui.alpha(0xFF000000 | SET_PRESETS[i], open));
            if (SET_PRESETS[i] == rgb) {
                Ui.roundOutline(ctx, sxp - 3, swY - 3, swD + 6, swD + 6, (swD + 6) / 2, Ui.alpha(0xFFFFFFFF, open));
            }
            if (clicked && in(mx, my, sxp - 2, swY - 2, swD + 4, swD + 4)) {
                c.setRgb(SET_PRESETS[i]);
                Config.save();
            }
        }

        // Ползунки R, G, B
        String[] names = { "R", "G", "B" };
        int[] labelCol = { 0xFFFF453A, 0xFF30D158, 0xFF0A84FF };
        int trackX = cx + 28, trackW = cw - 28 - 44;
        for (int i = 0; i < 3; i++) {
            int sy = ry + 46 + i * 18;
            Ui.text(ctx, this.textRenderer, names[i], cx + 12, sy + 3, Ui.alpha(labelCol[i], open));
            for (int x = 0; x < trackW; x++) {
                int v = x * 255 / (trackW - 1);
                int r2 = i == 0 ? v : ch[0], g2 = i == 1 ? v : ch[1], b2 = i == 2 ? v : ch[2];
                int col = 0xFF000000 | (r2 << 16) | (g2 << 8) | b2;
                int inset = (x == 0 || x == trackW - 1) ? 1 : 0;
                ctx.fill(trackX + x, sy + 4 + inset, trackX + x + 1, sy + 9 - inset, Ui.alpha(col, open));
            }
            int kx = trackX + Math.round(ch[i] / 255f * (trackW - 1));
            Ui.round(ctx, kx - 5, sy + 1, 10, 10, 5, Ui.alpha(0xFFFFFFFF, open));
            Ui.roundOutline(ctx, kx - 5, sy + 1, 10, 10, 5, Ui.alpha(0x55000000, open));
            String val = String.valueOf(ch[i]);
            Ui.text(ctx, this.textRenderer, val, cx + cw - 10 - this.textRenderer.getWidth(val), sy + 3,
                    Ui.alpha(TEXT_DIM, open));
            if (clicked && in(mx, my, trackX - 6, sy, trackW + 12, 14)) {
                dragCol = c;
                dragCh = i;
                dragColX = trackX;
                dragColW = trackW;
            }
        }

        // Радуга
        int rowY = ry + 100;
        ctx.fill(cx + 10, rowY - 2, cx + cw, rowY - 1, Ui.alpha(SEPARATOR, open));
        Ui.text(ctx, this.textRenderer, "Радуга (плавная смена цвета)", cx + 10, rowY + 3, Ui.alpha(TEXT, open));
        float t = c.rainbow ? 1f : 0f;
        float k = step(swAnim.getOrDefault(c, t), t, dt);
        swAnim.put(c, k);
        drawSwitch(ctx, cx + cw - 26 - 10, rowY, 26, 14, Ui.ease(k), open);
        if (clicked && in(mx, my, cx, rowY - 2, cw, 18)) {
            c.rainbow = !c.rainbow;
            Config.save();
        }
    }

    // ───────────────────────── Вкладка «Цвет» ─────────────────────────

    private void renderColor(DrawContext ctx, int mx, int my, boolean clicked, boolean down, float dt,
                             float open, int cx, int cy, int cw) {
        int cardH = 162;
        Ui.round(ctx, cx, cy, cw, cardH, 7, Ui.alpha(CARD, open));

        int rgb = Theme.accent & 0xFFFFFF;
        int[] ch = { (rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255 };

        // Превью + hex
        Ui.round(ctx, cx + 10, cy + 8, 18, 18, 9, Ui.alpha(Theme.accent, open));
        Ui.text(ctx, this.textRenderer, "Основной цвет", cx + 36, cy + 13, Ui.alpha(TEXT, open));
        String hex = String.format("#%06X", rgb);
        Ui.text(ctx, this.textRenderer, hex, cx + cw - 10 - this.textRenderer.getWidth(hex), cy + 13,
                Ui.alpha(TEXT_DIM, open));

        // Готовые цвета
        int swY = cy + 34, swD = 16, gap = 12;
        for (int i = 0; i < PRESETS.length; i++) {
            int sxp = cx + 10 + i * (swD + gap);
            Ui.round(ctx, sxp, swY, swD, swD, swD / 2, Ui.alpha(0xFF000000 | PRESETS[i], open));
            if (PRESETS[i] == rgb) {
                Ui.roundOutline(ctx, sxp - 3, swY - 3, swD + 6, swD + 6, (swD + 6) / 2, Ui.alpha(0xFFFFFFFF, open));
            }
            if (clicked && in(mx, my, sxp - 2, swY - 2, swD + 4, swD + 4)) {
                Theme.accent = 0xFF000000 | PRESETS[i];
                Config.save();
            }
        }

        // Ползунки R, G, B
        String[] names = { "R", "G", "B" };
        int[] labelCol = { 0xFFFF453A, 0xFF30D158, 0xFF0A84FF };
        int trackX = cx + 28, trackW = cw - 28 - 44;
        for (int i = 0; i < 3; i++) {
            int sy = cy + 62 + i * 24;
            Ui.text(ctx, this.textRenderer, names[i], cx + 12, sy + 6, Ui.alpha(labelCol[i], open));

            // Градиентная дорожка
            for (int c = 0; c < trackW; c++) {
                int v = c * 255 / (trackW - 1);
                int r2 = i == 0 ? v : ch[0], g2 = i == 1 ? v : ch[1], b2 = i == 2 ? v : ch[2];
                int col = 0xFF000000 | (r2 << 16) | (g2 << 8) | b2;
                int inset = (c == 0 || c == trackW - 1) ? 1 : 0;
                ctx.fill(trackX + c, sy + 7 + inset, trackX + c + 1, sy + 13 - inset, Ui.alpha(col, open));
            }
            // Ползунок
            int kx = trackX + Math.round(ch[i] / 255f * (trackW - 1));
            Ui.round(ctx, kx - 5, sy + 5, 10, 10, 5, Ui.alpha(0xFFFFFFFF, open));
            Ui.roundOutline(ctx, kx - 5, sy + 5, 10, 10, 5, Ui.alpha(0x55000000, open));

            String val = String.valueOf(ch[i]);
            Ui.text(ctx, this.textRenderer, val, cx + cw - 10 - this.textRenderer.getWidth(val), sy + 6,
                    Ui.alpha(TEXT_DIM, open));

            if (clicked && in(mx, my, trackX - 6, sy, trackW + 12, 20)) dragSlider = i;
        }

        if (!down && dragSlider != -1) {
            dragSlider = -1;
            Config.save();
        }
        if (down && dragSlider >= 0) {
            int v = Math.round((mx - trackX) / (float) (trackW - 1) * 255f);
            v = Math.max(0, Math.min(255, v));
            ch[dragSlider] = v;
            Theme.accent = 0xFF000000 | (ch[0] << 16) | (ch[1] << 8) | ch[2];
        }

        // Применять ли цвет к тексту HUD
        int ry = cy + 62 + 3 * 24 + 4;
        ctx.fill(cx + 10, ry - 2, cx + cw, ry - 1, Ui.alpha(SEPARATOR, open));
        boolean hover = in(mx, my, cx, ry - 1, cw, 24);
        Ui.text(ctx, this.textRenderer, "Красить текст HUD в этот цвет", cx + 10, ry + 8, Ui.alpha(TEXT, open));
        hudSw = step(hudSw, Theme.hudAccent ? 1f : 0f, dt);
        drawSwitch(ctx, cx + cw - 26 - 10, ry + 5, 26, 14, Ui.ease(hudSw), open);
        if (clicked && hover) {
            Theme.hudAccent = !Theme.hudAccent;
            Config.save();
        }
    }

    // ───────────────────────── Общее ─────────────────────────

    private void drawSwitch(DrawContext ctx, int x, int y, int w, int h, float k, float open) {
        Ui.round(ctx, x, y, w, h, h / 2, Ui.alpha(Ui.lerp(SW_OFF, Theme.accentColor(0f), k), open));
        int kd = h - 4;
        int kx = x + 2 + Math.round(k * (w - kd - 4));
        Ui.round(ctx, kx, y + 2, kd, kd, kd / 2, Ui.alpha(0xFFFFFFFF, open));
    }

    private static boolean in(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
