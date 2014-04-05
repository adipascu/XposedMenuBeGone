package ro.epb.menubegone;

import java.util.Set;
import java.util.TreeSet;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MainActivity extends Activity implements OnItemClickListener {
	private ListView appList;
	private AppListAdapter adapter;
	private Set<String> blackPackages;
	private SharedPreferences preferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		preferences = getSharedPreferences(Main.PREF_FILE, Context.MODE_WORLD_READABLE);
		
		blackPackages = preferences.getStringSet(Main.PREF_BLACKLIST, new TreeSet<String>());
		blackPackages = new TreeSet<String>(blackPackages);//this is a fix for an android bug
		//http://androiddev.orkitra.com/?p=7297
		

		appList = (ListView) findViewById(R.id.app_list);
		adapter = new AppListAdapter(this);
		appList.setAdapter(adapter);
		appList.setOnItemClickListener(this);
		
		for (String packageName : blackPackages) {
			int position = adapter.getPosition(packageName);
			if(position>=0)
				appList.setItemChecked(position, true);
		}

	}
	
	void refresh(){
		
	}

	/**
	 * updatePreference
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		String packageName = adapter.getItem(position).packageName;
		boolean isChecked = appList.getCheckedItemPositions().get(position, false);
		if(isChecked)		
			blackPackages.add(packageName);		
		else
			blackPackages.remove(packageName);
		
		
	}

	@Override
	protected void onPause() {
		super.onPause();		
		preferences.edit().putStringSet(Main.PREF_BLACKLIST, blackPackages).apply();
	}



}
