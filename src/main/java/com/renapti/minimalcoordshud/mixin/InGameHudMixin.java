package com.renapti.minimalcoordshud.mixin;

import com.renapti.minimalcoordshud.config.Vars;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
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
    @Unique
    final int GREEN = 0x98ffad;
    @Unique
    final int RED = 0xff98aa;
    @Unique
    final int DEEP_RED = 0xe10038;
    @Unique
    final int WHITE = 0xffffff;
    @Unique
    final int BLACK = 0x000000;

    @Unique
    final private Text positive = Text.translatable("hud.minimalcoordshud.facing.positive");
    @Unique
    final private Text negative = Text.translatable("hud.minimalcoordshud.facing.negative");
    @Unique
    final private Text error = Text.translatable("hud.minimalcoordshud.coords.error");

    @Unique
    final int xPadding = 5;
    @Unique
    final int yPadding = 5;

    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow
    private int scaledHeight;
    @Shadow
    private int scaledWidth;

    @Shadow
    public abstract TextRenderer getTextRenderer();

    @Shadow
    protected abstract PlayerEntity getCameraPlayer();

    // Render inject to draw text on the game HUD
    // TODO: Configurable anchoring, positioning, linespacing, etc.
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderAutosaveIndicator(Lnet/minecraft/client/gui/DrawContext;)V"), method = "render")
    private void render(DrawContext context, float tickDelta, CallbackInfo ci)
    {
        // Hide if the player has the HUD hidden, is using debug, or the mod is toggled off
        if (client.options.hudHidden || client.options.debugEnabled || !Vars.isHUDToggled)
            return;

        TextRenderer textRenderer = getTextRenderer();
        PlayerEntity player = getCameraPlayer();

        int lineSpacing = 10;

        boolean isOnRight = Vars.corner == Vars.CORNERS.TR || Vars.corner == Vars.CORNERS.BR;
        boolean isOnBottom = Vars.corner == Vars.CORNERS.BR || Vars.corner == Vars.CORNERS.BL;

        int xGlobalOffset = (isOnRight ? scaledWidth - xPadding : xPadding);
        int yOffset = (isOnBottom ? scaledHeight - lineSpacing * 3 - (yPadding - 2) : yPadding);

        // If player is null, prevent crash
        if (player == null)
        {
            drawTextOutlined(context,
                    textRenderer,
                    error,
                    xGlobalOffset - (isOnRight ? textRenderer.getWidth(error) : 0),
                    (isOnBottom ? yOffset + lineSpacing * 2 : yOffset),
                    DEEP_RED,
                    BLACK,
                    true);
            return;
        }

        // Format to two decimal digits
        DecimalFormat formatXZ = new DecimalFormat("0.00");
        DecimalFormat formatY = new DecimalFormat("0.##");

        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        // Coordinate lines
        Text xT = Text.of("x: " + formatXZ.format(x));
        Text yT = Text.of("y: " + formatY.format(y));
        Text zT = Text.of("z: " + formatXZ.format(z));

        // Widths of strings, in pixels
        int xWidth = textRenderer.getWidth(xT.getString());
        int yWidth = textRenderer.getWidth(yT.getString());
        int zWidth = textRenderer.getWidth(zT.getString());
        int dirIndicatorWidth = textRenderer.getWidth(positive);

        // Used when the anchor is on the right side, TODO: This could be significantly optimized
        int[] xRightOffsets = (isOnRight ? new int[]{xWidth, yWidth, zWidth} : new int[]{0, 0, 0});

        // Offset of facing indicators
        int xSignOffset = (isOnRight ? (xWidth + dirIndicatorWidth + 4) * -1 : xWidth + 4);
        int zSignOffset = (isOnRight ? (zWidth + dirIndicatorWidth + 4) * -1 : zWidth + 4);

        drawTextOutlined(context, textRenderer, xT, xGlobalOffset - xRightOffsets[0], yOffset, WHITE, BLACK, true);
        drawTextOutlined(context, textRenderer, yT, xGlobalOffset - xRightOffsets[1], yOffset + lineSpacing, WHITE, BLACK, true);
        drawTextOutlined(context, textRenderer, zT, xGlobalOffset - xRightOffsets[2], yOffset + lineSpacing * 2, WHITE, BLACK, true);

        // Player direction indicators
        String facing = player.getHorizontalFacing().getName();
        switch (facing)
        {
            // Facing +X
            case "east" -> drawTextOutlined(context, textRenderer, positive,
                    xGlobalOffset + xSignOffset, yOffset, GREEN, BLACK, true);
            // Facing +Z
            case "south" -> drawTextOutlined(context, textRenderer, positive,
                    xGlobalOffset + zSignOffset, yOffset + lineSpacing * 2, GREEN, BLACK, true);
            // Facing -X
            case "west" -> drawTextOutlined(context, textRenderer, negative,
                    xGlobalOffset + xSignOffset, yOffset, RED, BLACK, true);
            // Facing -Z
            case "north" -> drawTextOutlined(context, textRenderer, negative,
                    xGlobalOffset + zSignOffset, yOffset + lineSpacing * 2, RED, BLACK, true);
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