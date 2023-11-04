package com.renapti.minimalcoordshud.mixin;

import com.renapti.minimalcoordshud.MinimalCoordsHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.DecimalFormat;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin
{
    @Unique
    private final MinecraftClient mc = MinecraftClient.getInstance();

    @Shadow
    public abstract TextRenderer getTextRenderer();

    @Shadow
    protected abstract PlayerEntity getCameraPlayer();

	// Render inject to draw text on the game HUD
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderAutosaveIndicator(Lnet/minecraft/client/gui/DrawContext;)V"), method = "render")
    private void render(DrawContext context, float tickDelta, CallbackInfo ci)
    {
		// Only show if HUD is not hidden, debug is not enabled, and mod is toggled on
        if (mc.options.hudHidden || mc.options.debugEnabled || !MinimalCoordsHud.isHUDToggled)
            return;

        PlayerEntity player = getCameraPlayer();

        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        TextRenderer textRenderer = getTextRenderer();

		// Format to two decimal digits
        DecimalFormat df = new DecimalFormat("0.##");

		// Position of the text, anchored to top left
        int xOffset = 5;
        int yOffset = 5;

		// Spacing between the lines
        int lineSpacing = 10;

		// TODO: Configurable anchoring, positioning, linespacing, etc.
        drawTextOutlined(context, textRenderer, "x: " + df.format(x), xOffset, yOffset, 0xffffff, 0x000000, true);
        drawTextOutlined(context, textRenderer, "y: " + df.format(y), xOffset, yOffset + lineSpacing, 0xffffff, 0x000000, true);
        drawTextOutlined(context, textRenderer, "z: " + df.format(z), xOffset, yOffset + lineSpacing * 2, 0xffffff, 0x000000, true);
    }

	/**
	 * Draws a text message with a colored outline, either 4-pixel-connected or 8-pixel-connected.
	 *
	 * @param context      The given DrawContext to render from.
	 * @param textRenderer The given TextRenderer to render text from.
	 * @param message      The message to draw.
	 * @param x            The X position of the text.
	 * @param y            The Y position of the text.
	 * @param color        The color of the fill.
	 * @param outlineColor The color of the outline.
	 * @param thick        Whether or not the outline should be 4-pixel-connected or 8-pixel-connected.
	 */
    @Unique
    private void drawTextOutlined(DrawContext context, TextRenderer textRenderer, String message, int x, int y, int color, int outlineColor, boolean thick)
    {
		// Draw 4-pixel-connected outline
        context.drawText(textRenderer, message, x - 1, y, outlineColor, false);
        context.drawText(textRenderer, message, x + 1, y, outlineColor, false);
        context.drawText(textRenderer, message, x, y - 1, outlineColor, false);
        context.drawText(textRenderer, message, x, y + 1, outlineColor, false);

		// Draw 8-pixel-connected outline if thick
        if (thick)
        {
            context.drawText(textRenderer, message, x - 1, y - 1, outlineColor, false);
            context.drawText(textRenderer, message, x + 1, y + 1, outlineColor, false);
            context.drawText(textRenderer, message, x + 1, y - 1, outlineColor, false);
            context.drawText(textRenderer, message, x - 1, y + 1, outlineColor, false);
        }

		// Finally, draw fill
        context.drawText(textRenderer, message, x, y, color, false);
    }
}