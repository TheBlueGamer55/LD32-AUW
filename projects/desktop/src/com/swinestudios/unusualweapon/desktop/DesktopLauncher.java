package com.swinestudios.unusualweapon.desktop;

import org.mini2Dx.desktop.DesktopMini2DxGame;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.swinestudios.unusualweapon.UnusualWeaponGame;

public class DesktopLauncher{
	public static void main (String[] arg){		
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Aquatic Regia";
        cfg.width = 640;
        cfg.height = 480;
        cfg.vSyncEnabled = true;
        cfg.foregroundFPS = 60;
        cfg.backgroundFPS = 60;
        new LwjglApplication(new DesktopMini2DxGame("org.mini2dx.sample.basicgameexample", new UnusualWeaponGame()), cfg);
	}
}
