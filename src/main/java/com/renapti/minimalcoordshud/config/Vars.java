package com.renapti.minimalcoordshud.config;

public class Vars
{
    // Whether the HUD should be visible
    public static boolean isHUDToggled = true;

    // Possible positions for the HUD
    public enum CORNERS
    {
        TL, TR, BL, BR;
    }

    // The current position of the HUD
    public static CORNERS corner = CORNERS.TL;
}
