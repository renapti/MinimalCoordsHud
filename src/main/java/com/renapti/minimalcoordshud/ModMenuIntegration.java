package com.renapti.minimalcoordshud;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final TextRenderer textRenderer = mc.textRenderer;
    Screen parent;

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("config.minimalcoordshud.title"));

        ConfigCategory general = builder.getOrCreateCategory(
                Text.translatable("config.minimalcoordshud.general.title")
        );

        ConfigEntryBuilder entries = builder.entryBuilder();

        double dummy = 0;
        general.addEntry(entries.startDoubleField(Text.translatable("config.minimalcoordshud.general.dummy"), dummy)
                .setDefaultValue(5.0)
                .setMax(20)
                .setMin(-5)
                .setTooltip(Text.translatable("config.minimalcoordshud.general.dummy.tooltip"))
                .build()
        );

        Screen screen = builder.build();
        return parent -> screen;
    }
}
