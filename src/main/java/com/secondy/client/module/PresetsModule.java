package com.secondy.client.module;

import com.secondy.client.settings.ActionSetting;
import com.secondy.client.settings.ModeSetting;
import com.secondy.client.util.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

/** Менеджер пресетов: сохраняет и загружает все настройки модулей в 5 слотов. */
public class PresetsModule extends Module {
    private static final int SLOTS = 5;

    private final ModeSetting slot = add(new ModeSetting("Слот", "slot", 0, "1", "2", "3", "4", "5"));
    @SuppressWarnings("unused")
    private final ActionSetting save = add(new ActionSetting("Сохранить текущие настройки в слот", this::saveSlot));
    @SuppressWarnings("unused")
    private final ActionSetting load = add(new ActionSetting("Загрузить настройки из слота", this::loadSlot));
    @SuppressWarnings("unused")
    private final ActionSetting delete = add(new ActionSetting("Очистить слот", this::deleteSlot));

    public PresetsModule() {
        super(Config.PRESETS_MODULE, "Сохранить и загрузить все настройки (5 слотов)", Category.UTILITY);
        setEnabled(true);
        refreshLabels();
    }

    private int slotNumber() {
        return slot.index() + 1;
    }

    /** Подписи слотов показывают, какие из них заняты. */
    private void refreshLabels() {
        for (int i = 0; i < SLOTS; i++) {
            slot.options[i] = (i + 1) + (Config.presetExists(i + 1) ? " (занят)" : " (пусто)");
        }
    }

    private void saveSlot() {
        int n = slotNumber();
        say(Config.savePreset(n) ? "Пресет " + n + " сохранён" : "Не удалось сохранить пресет " + n);
        refreshLabels();
    }

    private void loadSlot() {
        int n = slotNumber();
        if (!Config.presetExists(n)) {
            say("Слот " + n + " пуст");
            return;
        }
        say(Config.loadPreset(n) ? "Пресет " + n + " загружен" : "Не удалось загрузить пресет " + n);
    }

    private void deleteSlot() {
        int n = slotNumber();
        if (!Config.presetExists(n)) {
            say("Слот " + n + " и так пуст");
            return;
        }
        say(Config.deletePreset(n) ? "Слот " + n + " очищен" : "Не удалось очистить слот " + n);
        refreshLabels();
    }

    private static void say(String text) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.inGameHud != null) {
            mc.inGameHud.getChatHud().addMessage(Text.literal("[_2ndy] " + text));
        }
    }
}
