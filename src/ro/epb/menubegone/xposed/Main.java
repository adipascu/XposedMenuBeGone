package ro.epb.menubegone.xposed;

import java.util.Set;
import java.util.TreeSet;

import ro.epb.menubegone.core.Constants;
import ro.epb.menubegone.core.Logger;
import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Main implements IXposedHookLoadPackage, IXposedHookZygoteInit {

	private Set<String> blacklist;
	private boolean remapLong;

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {

		XSharedPreferences prefs = new XSharedPreferences(Constants.PACKAGE,
				Constants.PREF_FILE);
		blacklist = prefs.getStringSet(Constants.PREF_BLACKLIST,
				new TreeSet<String>());

		remapLong = prefs.getBoolean(Constants.PREF_LONG_REMAP, true);

		if (blacklist.isEmpty())
			Logger.Log("blacklist is empty");
		else
			Logger.Log("Blacklist start:");
		for (String string : blacklist) {
			Logger.Log(string);
		}

	}

	@Override
	public void handleLoadPackage(LoadPackageParam packageParam)
			throws Throwable {
		if (!blacklist.contains(packageParam.packageName)) {
			Logger.Log("run");
			hookAppProcess(packageParam);
		} else {
			Logger.Log("ignore " + packageParam.packageName);
		}

		if (packageParam.packageName.equals("android"))
			hookAndroidProcess(packageParam);

	}

	private void hookAppProcess(LoadPackageParam packageParam) {
		XposedHelpers.findAndHookMethod("android.view.ViewConfiguration",
				packageParam.classLoader, "hasPermanentMenuKey",
				new XC_MethodHook() {
					@Override
					protected void beforeHookedMethod(MethodHookParam param)
							throws Throwable {
						param.setResult(Boolean.valueOf(false));
					}
				});
	}

	private void hookAndroidProcess(LoadPackageParam packageParam) {

		Logger.Log("Loaded sys app(package: android)");

		// this is how you hook overrides
		Class<?> PhoneWindowManager = XposedHelpers.findClass(
				"com.android.internal.policy.impl.PhoneWindowManager",
				packageParam.classLoader);

		XposedBridge.hookAllMethods(PhoneWindowManager,
				"interceptKeyBeforeDispatching", new XC_MethodHook() {

					boolean nextTimeOpenMenu = false;

					@Override
					protected void beforeHookedMethod(MethodHookParam param)
							throws Throwable {
						KeyEvent event = (KeyEvent) param.args[1];
						if (event.getKeyCode() != KeyEvent.KEYCODE_MENU)
							return;
						boolean virtualKey = event.getDeviceId() == KeyCharacterMap.VIRTUAL_KEYBOARD;
						if (virtualKey) {
							param.setResult(Long.valueOf(0));
							return;
						}

						boolean longPress = (event.getFlags() & KeyEvent.FLAG_LONG_PRESS) != 0;
						boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
						// Logger.Log("long " + longPress);
						// Logger.Log("down " + down);
						if (remapLong) {
							if (down) {
								if (longPress) {
									nextTimeOpenMenu = false;
									injectMenuKey();
								} else {
									nextTimeOpenMenu = true;
								}
							} else if (nextTimeOpenMenu) {
								XposedHelpers.callMethod(param.thisObject,
										"toggleRecentApps");
								nextTimeOpenMenu = false;
							}
						} else
							XposedHelpers.callMethod(param.thisObject,
									"toggleRecentApps");
						param.setResult(Long.valueOf(-1));
						// return value 0 makes the dispatcher share the key
						// with an app
						// return value -1 makes the dispatcher share the key
						// with the os
						// positive value is time in ms that the dispatcher
						// tries again (used to debounce)
					}

				});
	}

	protected void injectMenuKey() {
		InputManager inputManager = (InputManager) XposedHelpers
				.callStaticMethod(InputManager.class, "getInstance");
		long now = SystemClock.uptimeMillis();
		final KeyEvent downEvent = new KeyEvent(now, now, KeyEvent.ACTION_DOWN,
				KeyEvent.KEYCODE_MENU, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD,
				0, KeyEvent.FLAG_FROM_SYSTEM, InputDevice.SOURCE_KEYBOARD);
		final KeyEvent upEvent = KeyEvent.changeAction(downEvent,
				KeyEvent.ACTION_UP);

		Integer INJECT_INPUT_EVENT_MODE_ASYNC = XposedHelpers
				.getStaticIntField(InputManager.class,
						"INJECT_INPUT_EVENT_MODE_ASYNC");

		XposedHelpers.callMethod(inputManager, "injectInputEvent", downEvent,
				INJECT_INPUT_EVENT_MODE_ASYNC);
		XposedHelpers.callMethod(inputManager, "injectInputEvent", upEvent,
				INJECT_INPUT_EVENT_MODE_ASYNC);

	}
}