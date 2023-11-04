package com.renapti.minimalcoordshud;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinimalCoordsHud implements ModInitializer
{
    public static final Logger LOGGER = LoggerFactory.getLogger("minimalcoordshud");

	// Whether or not the HUD should be visible
    public static boolean isHUDToggled = true;

    @Override
    public void onInitialize()
    {
        // Do nothing!
    }
}