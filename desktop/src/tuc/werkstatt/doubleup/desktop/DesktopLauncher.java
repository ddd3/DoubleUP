package tuc.werkstatt.doubleup.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import tuc.werkstatt.doubleup.DoubleUp;

public class DesktopLauncher {
	public static void main (String[] args) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "DoubleUp";
		// aspect ratio 16:10, same as course device
		config.width = 450;
		config.height = 720;
		new LwjglApplication(new DoubleUp(args), config);
	}
}