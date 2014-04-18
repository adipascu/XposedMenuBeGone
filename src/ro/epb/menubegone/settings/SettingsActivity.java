package ro.epb.menubegone.settings;

import ro.epb.menubegone.R;
import ro.epb.menubegone.core.Constants;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;

public class SettingsActivity extends FragmentActivity implements
		OnClickListener {

	private SharedPreferences preferences;
	private CheckBox longPress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_activity);
		findViewById(R.id.white_list).setOnClickListener(this);
		preferences = PreferencesHelper.getPreferences(this);

		longPress = (CheckBox) findViewById(R.id.long_press);
		boolean longRemap = preferences.getBoolean(Constants.PREF_LONG_REMAP,
				true);
		longPress.setChecked(longRemap);

		TextView whitelistDisclaimer = (TextView) findViewById(R.id.whitelist_disclaimer);
		whitelistDisclaimer.setMovementMethod(LinkMovementMethod.getInstance());

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.white_list:
			startActivity(new Intent(this, BlackListActivity.class));
			break;

		default:
			throw new UnsupportedOperationException("unknown clicked view!");
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		preferences.edit()
				.putBoolean(Constants.PREF_LONG_REMAP, longPress.isChecked())
				.apply();
	}

}
