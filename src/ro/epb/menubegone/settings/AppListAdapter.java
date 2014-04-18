package ro.epb.menubegone.settings;

import java.util.List;

import ro.epb.menubegone.R;
import ro.epb.menubegone.core.Logger;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AppListAdapter extends BaseAdapter {

	private PackageManager packageManager;
	private LayoutInflater inflater;
	private List<PackageInfo> packages;

	public AppListAdapter(Context context) {
		packageManager = context.getPackageManager();
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		loadPackages();
	}

	@Override
	public int getCount() {
		if (packages == null) {
			return 0;
		}
		return packages.size();
	}

	@Override
	public ApplicationInfo getItem(int position) {
		return packages.get(position).applicationInfo;
	}

	public int getPosition(String packageName) {
		if (packages != null) {
			for (int i = 0; i < getCount(); i++) {
				ApplicationInfo app = getItem(i);
				if (packageName.equals(app.packageName))
					return i;
			}
		}
		return -1;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			Logger.Log("Load " + position);
			convertView = inflater.inflate(R.layout.blacklist_cell, null);
			final ApplicationInfo app = getItem(position);
			final TextView nameView = (TextView) convertView
					.findViewById(R.id.name);
			final ImageView iconView = (ImageView) convertView
					.findViewById(R.id.icon);
			nameView.setText(app.packageName);
			new AsyncTask<Void, Void, Void>() {
				Drawable icon;
				CharSequence name;

				@Override
				protected Void doInBackground(Void... params) {
					name = packageManager.getApplicationLabel(app);
					icon = packageManager.getApplicationIcon(app);
					return null;
				}

				@Override
				protected void onPostExecute(Void result) {
					nameView.setText(name);
					iconView.setImageDrawable(icon);
				}
			}.execute();

		}
		return convertView;
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	@Override
	public int getViewTypeCount() {
		return packages.size();
	}

	public void loadPackages() {
		packages = packageManager.getInstalledPackages(0);
		notifyDataSetChanged();
	}

}
