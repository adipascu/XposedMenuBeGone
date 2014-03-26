package ro.epb.menubegone;

import java.lang.reflect.Field;

import android.view.KeyEvent;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Main implements IXposedHookLoadPackage {

	@Override
	public void handleLoadPackage(final LoadPackageParam packageParam) throws Throwable {

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

		if (!packageParam.packageName.equals("android"))
			return;

		XposedBridge.log("Loaded sys app(package: android)");

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