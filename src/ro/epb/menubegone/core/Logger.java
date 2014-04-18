package ro.epb.menubegone.core;

import android.util.Log;
import de.robv.android.xposed.XposedBridge;

public class Logger {
	public static void Log(String message) {
		try {
			XposedBridge.log("1133:" + message);
		} catch (NoClassDefFoundError e) {
			Log.i("1133", message);
		}

	}
}
