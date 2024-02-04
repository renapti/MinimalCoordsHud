package com.renapti.minimalcoordshud.client;

import com.renapti.minimalcoordshud.MinimalCoordsHud;
import com.renapti.minimalcoordshud.config.Vars;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class MinimalCoordsHudClient implements ClientModInitializer
{
    public static KeyBinding copyCoords;
    public static KeyBinding copyInterCoords;
    public static KeyBinding toggleHUD;
    public static KeyBinding changeCorner;

    private static void copyCoords(MinecraftClient mc)
    {
        assert mc.player != null;

        // Formatted string to copy
        String copied = (int) Math.ceil(mc.player.getX()) + " " +
                (int) Math.ceil(mc.player.getY()) + " " +
                (int) Math.ceil(mc.player.getZ());

        // Notify the player and copy
        mc.player.sendMessage(Text.translatable("chat.minimalcoordshud.copied", copied));
        mc.keyboard.setClipboard(copied);
    }

    private static void copyInterCoords(MinecraftClient mc)
    {
        assert mc.player != null;

        String copied = "";
        double scale = mc.player.getWorld().getDimension().coordinateScale();
        boolean failed = false;

        // Nether -- copy Overworld coords
        if (scale == 8.0)
        {
            // Since we're in the Nether, multiply to get Overworld coords
            copied = (int) Math.ceil(mc.player.getX() * 8) + " " +
                    (int) Math.ceil(mc.player.getY()) + " " +
                    (int) Math.ceil(mc.player.getZ() * 8);

            mc.player.sendMessage(Text.translatable("chat.minimalcoordshud.copied.overworld", copied));
        }
        // Overworld or End -- copy Nether coords
        else if (scale == 1.0)
        {
            // Since we're in the Overworld or End, divide to get Nether coords
            copied = (int) Math.ceil(mc.player.getX() / 8) + " " +
                    (int) Math.ceil(mc.player.getY()) + " " +
                    (int) Math.ceil(mc.player.getZ() / 8);

            mc.player.sendMessage(Text.translatable("chat.minimalcoordshud.copied.nether", copied));
        }
        else
        {
            // The dimension must be modded, because the scale is neither 8 or 1.
            mc.player.sendMessage(Text.translatable("chat.minimalcoordshud.failed"));
            failed = true;
        }

        // Copy to clipboard if successful
        if (!failed)
            mc.keyboard.setClipboard(copied);
    }

    @Override
    public void onInitializeClient()
    {
        // Copy normal coords keybind - default is M
        copyCoords = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.minimalcoordshud.copycoords",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "category.minimalcoordshud.main"
        ));

        // Copy translated coords keybind - default is comma
        copyInterCoords = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.minimalcoordshud.copycoords.interdimensional",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_COMMA,
                "category.minimalcoordshud.main"
        ));

        // Toggle mod HUD - default is N
        toggleHUD = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.minimalcoordshud.togglehud",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                "category.minimalcoordshud.main"
        ));

        // Toggle mod corner - default is period
        changeCorner = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.minimalcoordshud.changecorner",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_PERIOD,
                "category.minimalcoordshud.main"
        ));

        // Listen for key presses
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

            // Toggle the HUD
            while (MinimalCoordsHudClient.toggleHUD.wasPressed())
            {
                Vars.isHUDToggled = !Vars.isHUDToggled;
            }

            // Change corner
            while (MinimalCoordsHudClient.changeCorner.wasPressed())
            {
                switch (Vars.corner)
                {
                    case TL ->
                        Vars.corner = Vars.CORNERS.TR;
                    case TR ->
                        Vars.corner = Vars.CORNERS.BR;
                    case BR ->
                        Vars.corner = Vars.CORNERS.BL;
                    case BL ->
                        Vars.corner = Vars.CORNERS.TL;
                    default ->
                    {
                        MinimalCoordsHud.LOGGER.warn("Invalid corner (Was " + Vars.corner + ")");
                        Vars.corner = Vars.CORNERS.TL;
                    }
                }
            }
        });
    }
}
