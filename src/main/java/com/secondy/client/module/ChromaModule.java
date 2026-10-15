package com.secondy.client.module;

import com.secondy.client.util.Theme;

public class ChromaModule extends Module {
    public ChromaModule() {
        super("Chroma", "Радужный текст в HUD и меню", Category.VISUAL);
    }

    @Override public void onEnable() { Theme.chroma = true; }
    @Override public void onDisable() { Theme.chroma = false; }
}
