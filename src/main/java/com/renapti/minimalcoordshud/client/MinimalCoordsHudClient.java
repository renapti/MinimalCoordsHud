package com.renapti.minimalcoordshud.client;

import com.renapti.minimalcoordshud.MinimalCoordsHud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.world.dimension.DimensionType;
import org.lwjgl.glfw.GLFW;

public class MinimalCoordsHudClient implements ClientModInitializer
{
    public static KeyBinding copyCoords;
    public static KeyBinding copyInterCoords;
    public static KeyBinding toggleHUD;

    @Override
    public void onInitializeClient()
    {
        copyCoords = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.minimalcoordshud.copycoords",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "category.minimalcoordshud.main"
        ));

        copyInterCoords = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.minimalcoordshud.copycoords.interdimensional",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_COMMA,
                "category.minimalcoordshud.main"
        ));

        toggleHUD = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.minimalcoordshud.togglehud",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                "category.minimalcoordshud.main"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(mc ->
        {
            if (MinimalCoordsHudClient.copyCoords.wasPressed())
            {
                copyCoords(mc);
            }
            else if (MinimalCoordsHudClient.copyInterCoords.wasPressed())
            {
                copyInterCoords(mc);
            }

            while (MinimalCoordsHudClient.toggleHUD.wasPressed())
            {
                MinimalCoordsHud.isHUDToggled = !MinimalCoordsHud.isHUDToggled;
            }
        });
    }

    private static void copyCoords(MinecraftClient mc)
    {
        assert mc.player != null;
        String copied = (int) Math.ceil(mc.player.getX()) + " " +
                (int) Math.ceil(mc.player.getY()) + " " +
                (int) Math.ceil(mc.player.getZ());

        mc.player.sendMessage(Text.translatable("chat.minimalcoordshud.copied", copied));
        mc.keyboard.setClipboard(copied);
    }

    private static void copyInterCoords(MinecraftClient mc)
    {
        assert mc.player != null;
        double scale = mc.player.getWorld().getDimension().coordinateScale();

        String copied = "";

        // Nether -- copy Overworld coords
        if (scale == 8.0)
        {
            copied = (int) Math.ceil(mc.player.getX() * 8) + " " +
                    (int) Math.ceil(mc.player.getY()) + " " +
                    (int) Math.ceil(mc.player.getZ() * 8);

            mc.player.sendMessage(Text.translatable("chat.minimalcoordshud.copied.overworld", copied));
        }
        // Overworld -- copy Nether coords
        else if (scale == 1.0)
        {
            copied = (int) Math.ceil(mc.player.getX() / 8) + " " +
                    (int) Math.ceil(mc.player.getY()) + " " +
                    (int) Math.ceil(mc.player.getZ() / 8);

            mc.player.sendMessage(Text.translatable("chat.minimalcoordshud.copied.nether", copied));
        }
        else
        {
            mc.player.sendMessage(Text.translatable("chat.minimalcoordshud.failed"));
        }

        mc.keyboard.setClipboard(copied);
    }
}
