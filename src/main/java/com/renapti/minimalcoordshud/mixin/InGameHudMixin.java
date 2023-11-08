package com.renapti.minimalcoordshud.mixin;

import com.renapti.minimalcoordshud.MinimalCoordsHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
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
    @Shadow
    public abstract TextRenderer getTextRenderer();

    @Shadow
    protected abstract PlayerEntity getCameraPlayer();

    @Shadow @Final
    private MinecraftClient client;

    @Shadow private int scaledHeight;
    @Shadow private int scaledWidth;
    // Colors
    @Unique
    final int GREEN = 0x98ffad;
    @Unique
    final int RED = 0xff98aa;
    @Unique
    final int WHITE = 0xffffff;
    @Unique
    final int BLACK = 0x000000;

    // TODO: Is HUD RTL?
    @Unique
    boolean isRTL = false;

    // TODO: Is HUD bottom-to-top?
    @Unique
    boolean isFromBottom = false;

    // Indicators
    @Unique
    final private Text positive = Text.translatable("hud.minimalcoordshud.facing.positive");

    @Unique
    final private Text negative = Text.translatable("hud.minimalcoordshud.facing.negative");

	// Render inject to draw text on the game HUD
    // TODO: Configurable anchoring, positioning, linespacing, etc.
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderAutosaveIndicator(Lnet/minecraft/client/gui/DrawContext;)V"), method = "render")
    private void render(DrawContext context, float tickDelta, CallbackInfo ci)
    {
		// Only show if HUD is not hidden, debug is not enabled, and mod is toggled on
        if (client.options.hudHidden || client.options.debugEnabled || !MinimalCoordsHud.isHUDToggled)
            return;

        PlayerEntity player = getCameraPlayer();

        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        TextRenderer textRenderer = getTextRenderer();

		// Format to two decimal digits
        DecimalFormat formatXZ = new DecimalFormat("0.00");
        DecimalFormat formatY  = new DecimalFormat("0.##");

		// Position of the text, anchored to top left, in pixels
        // TODO: Fix offsets
        int xOffset = (isRTL ? scaledWidth - 100 : 5);
        int yOffset = (isFromBottom ? scaledHeight - 100 : 5);

        // Coordinate lines
        Text xT = Text.of("x: " + formatXZ.format(x));
        Text yT = Text.of("y: " + formatY.format(y));
        Text zT = Text.of("z: " + formatXZ.format(z));

        // Offset of facing indicators
        // TODO: Fix these offsets too
        int xSignOffset = (isRTL ? (textRenderer.getWidth(xT.getString()) + textRenderer.getWidth(positive) + 4) * -1 :
                textRenderer.getWidth(xT.getString()) + 4);
        int zSignOffset = (isRTL ? (textRenderer.getWidth(zT.getString()) + textRenderer.getWidth(positive) + 4) * -1 :
                textRenderer.getWidth(zT.getString()) + 4);

		// Spacing between the lines in pixels
        int lineSpacing = 10;

        drawTextOutlined(context, textRenderer, xT, xOffset, yOffset, WHITE, BLACK, true);
        drawTextOutlined(context, textRenderer, yT, xOffset, yOffset + lineSpacing, WHITE, BLACK, true);
        drawTextOutlined(context, textRenderer, zT, xOffset, yOffset + lineSpacing * 2, WHITE, BLACK, true);

        // Player direction indicators
        String facing = player.getHorizontalFacing().getName();
        switch (facing)
        {
            // Facing +X
            case "east" ->
            {
                drawTextOutlined(context, textRenderer, positive,
                        xOffset + xSignOffset, yOffset, GREEN, BLACK, true);
            }
            // Facing +Z
            case "south" ->
            {
                drawTextOutlined(context, textRenderer, positive,
                        xOffset + zSignOffset, yOffset + lineSpacing * 2, GREEN, BLACK, true);
            }
            // Facing -X
            case "west" ->
            {
                drawTextOutlined(context, textRenderer, negative,
                        xOffset + xSignOffset, yOffset, RED, BLACK, true);
            }
            // Facing -Z
            case "north" ->
            {
                drawTextOutlined(context, textRenderer, negative,
                        xOffset + zSignOffset, yOffset + lineSpacing * 2, RED, BLACK, true);
            }
        }
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
    private void drawTextOutlined(DrawContext context, TextRenderer textRenderer, Text message, int x, int y, int color, int outlineColor, boolean thick)
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