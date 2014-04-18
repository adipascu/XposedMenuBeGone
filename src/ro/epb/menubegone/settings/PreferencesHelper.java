package ro.epb.menubegone.settings;

import ro.epb.menubegone.core.Constants;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesHelper {

	@SuppressWarnings("deprecation")
	@SuppressLint("WorldReadableFiles")
	public static SharedPreferences getPreferences(Context context) {
		return context.getSharedPreferences(Constants.PREF_FILE,
				Context.MODE_WORLD_READABLE);
	}

}
