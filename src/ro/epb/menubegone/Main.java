package ro.epb.menubegone;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.TreeSet;

import android.view.KeyEvent;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Main implements IXposedHookLoadPackage , IXposedHookZygoteInit {

	static final String PREF_FILE = "preferences";
	static final String PREF_BLACKLIST = "blacklist";


	private Set<String> blacklist;
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		
//		if(blacklist.isEmpty())
//			Logger.Log("blacklist is empty");
//		for (String string : blacklist) {
//			Logger.Log(string);
//		}

	}

	@Override
	public void handleLoadPackage(LoadPackageParam packageParam) throws Throwable {
		XSharedPreferences prefs = new XSharedPreferences(Main.class.getPackage().getName(), PREF_FILE);
		blacklist = prefs.getStringSet(PREF_BLACKLIST, new TreeSet<String>());
		if(!blacklist.contains(packageParam.packageName)){
			Logger.Log("run");
			hookAppProcess(packageParam);
		}
		else{
			Logger.Log("ignore " + packageParam.packageName);
		}

		if (packageParam.packageName.equals("android"))
			hookAndroidProcess(packageParam);


	}

	private void hookAppProcess(LoadPackageParam packageParam){
		Logger.Log("hook: " + packageParam.packageName);
		XposedHelpers.findAndHookMethod("com.android.internal.view.ActionBarPolicy", packageParam.classLoader, "showsOverflowMenuButton",new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.setResult(Boolean.valueOf(true));
			}
		});

		XposedHelpers.findAndHookMethod("android.view.ViewConfiguration", packageParam.classLoader, "hasPermanentMenuKey",new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.setResult(Boolean.valueOf(false));
			}
		});
	}

	private void hookAndroidProcess(LoadPackageParam packageParam){
		Logger.Log("Loaded sys app(package: android)");

		//this is how you hook overrides
		Class<?> PhoneWindowManager = XposedHelpers.findClass("com.android.internal.policy.impl.PhoneWindowManager", packageParam.classLoader);

		XposedBridge.hookAllMethods(PhoneWindowManager, "interceptKeyBeforeQueueing", new XC_MethodHook() {

			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				alterKeyEvent((KeyEvent) param.args[0]);
			}

		});

		XposedBridge.hookAllMethods(PhoneWindowManager, "interceptKeyBeforeDispatching", new XC_MethodHook() {

			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				alterKeyEvent((KeyEvent) param.args[1]);
			}

		});
	}

	private void alterKeyEvent(KeyEvent event) throws Throwable{
		if(event.getKeyCode() == KeyEvent.KEYCODE_MENU)
		{
			Field mKeyCode = KeyEvent.class.getDeclaredField("mKeyCode");
			mKeyCode.setInt(event, KeyEvent.KEYCODE_APP_SWITCH);
		}
	}
}