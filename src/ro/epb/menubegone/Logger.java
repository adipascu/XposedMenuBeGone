package ro.epb.menubegone;

import android.util.Log;
import de.robv.android.xposed.XposedBridge;

public class Logger {
	static void Log(String message)
	{
		try{
			XposedBridge.log("1133:" + message);
		}
		catch(NoClassDefFoundError e)
		{
			Log.i("1133", message);
		}
		
	}
}
