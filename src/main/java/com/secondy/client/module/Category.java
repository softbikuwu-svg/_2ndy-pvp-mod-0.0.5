package com.secondy.client.module;

public enum Category {
    HUD("HUD"),
    PVP("PvP"),
    UTILITY("Утилиты"),
    VISUAL("Визуал"),
    OPTIMIZE("Оптимиз."),
    COLOR("Цвет");

    public final String label;

    Category(String label) {
        this.label = label;
    }
}
