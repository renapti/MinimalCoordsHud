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

	@Shadow public abstract TextRenderer getTextRenderer();
	@Shadow protected abstract PlayerEntity getCameraPlayer();

	@Unique
	private final MinecraftClient mc = MinecraftClient.getInstance();

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderAutosaveIndicator(Lnet/minecraft/client/gui/DrawContext;)V"), method = "render")
	private void render(DrawContext context, float tickDelta, CallbackInfo ci) {

		if (mc.options.hudHidden || mc.options.debugEnabled || !MinimalCoordsHud.isHUDToggled)
			return;

		PlayerEntity player = getCameraPlayer();

		double x = player.getX();
		double y = player.getY();
		double z = player.getZ();

		TextRenderer textRenderer = getTextRenderer();
		DecimalFormat df = new DecimalFormat("0.##");

		int xOffset = 5;
		int yOffset = 5;
		int lineSpacing = 10;

		drawTextOutlined(context, textRenderer, "x: " + df.format(x), xOffset, yOffset, 0xffffff, 0x000000, true);
		drawTextOutlined(context, textRenderer, "y: " + df.format(y), xOffset, yOffset + lineSpacing, 0xffffff, 0x000000, true);
		drawTextOutlined(context, textRenderer, "z: " + df.format(z), xOffset, yOffset + lineSpacing * 2, 0xffffff, 0x000000, true);
	}

	@Unique
	private void drawTextOutlined(DrawContext context, TextRenderer textRenderer, String message, int x, int y, int color, int outlineColor, boolean thick)
	{
		context.drawText(textRenderer, message, x - 1, y    , outlineColor, false);
		context.drawText(textRenderer, message, x + 1, y    , outlineColor, false);
		context.drawText(textRenderer, message, x    , y - 1, outlineColor, false);
		context.drawText(textRenderer, message, x    , y + 1, outlineColor, false);

		if (thick)
		{
			context.drawText(textRenderer, message, x - 1, y - 1, outlineColor, false);
			context.drawText(textRenderer, message, x + 1, y + 1, outlineColor, false);
			context.drawText(textRenderer, message, x + 1, y - 1, outlineColor, false);
			context.drawText(textRenderer, message, x - 1, y + 1, outlineColor, false);
		}

		context.drawText(textRenderer, message, x, y, color, false);
	}
}