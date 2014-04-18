package ro.epb.menubegone.settings;

import java.util.Set;
import java.util.TreeSet;

import ro.epb.menubegone.R;
import ro.epb.menubegone.core.Constants;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class BlackListActivity extends FragmentActivity implements
		OnItemClickListener {
	private ListView appList;
	private AppListAdapter adapter;
	private Set<String> blackPackages;
	private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.blacklist_activity);

		preferences = PreferencesHelper.getPreferences(this);

		blackPackages = preferences.getStringSet(Constants.PREF_BLACKLIST,
				new TreeSet<String>());
		blackPackages = new TreeSet<String>(blackPackages);
		// this is a fix for an android bug
		// http://androiddev.orkitra.com/?p=7297

		appList = (ListView) findViewById(R.id.app_list);
		adapter = new AppListAdapter(this);
		appList.setAdapter(adapter);
		appList.setOnItemClickListener(this);

		for (String packageName : blackPackages) {
			int position = adapter.getPosition(packageName);
			if (position >= 0)
				appList.setItemChecked(position, true);
		}

	}

	/**
	 * updatePreference
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		String packageName = adapter.getItem(position).packageName;
		boolean isChecked = appList.getCheckedItemPositions().get(position,
				false);
		if (isChecked)
			blackPackages.add(packageName);
		else
			blackPackages.remove(packageName);

	}

	@Override
	protected void onPause() {
		super.onPause();
		preferences.edit()
				.putStringSet(Constants.PREF_BLACKLIST, blackPackages).apply();
	}

}
