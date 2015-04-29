package com.prabhat.nearbyphotos.util;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.prabhat.nearbyphotos.R;
import com.prabhat.nearbyphotos.parser.PlaceWithPhoto;
import com.prabhat.nearbyphotos.util.ImageLoadingAdapter.ViewHolder;

public class OfflineLoadingAdapter extends BaseAdapter {

	private Activity activity;
	private int mSavedBitmapCount = 0;
	private static LayoutInflater inflater = null;
	public ImageLoader imageLoader;
	private int mDefaultCount = 10;
	private int mTotalVisibleCount = 0;

	public OfflineLoadingAdapter(Activity a) {
		activity = a;
		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// Create ImageLoader object to download and show image in list
		// Call ImageLoader constructor to initialize FileCache
		imageLoader = new ImageLoader(activity.getApplicationContext());
		mSavedBitmapCount = imageLoader.getSavedBitmapsCount();
		mTotalVisibleCount += mDefaultCount;
	}

	public int getCount() {
		return (mTotalVisibleCount > mSavedBitmapCount) ? mSavedBitmapCount : mTotalVisibleCount;
	}
	
	public void loadMore() {
		mTotalVisibleCount += mDefaultCount;
		notifyDataSetChanged();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	/********* Create a holder Class to contain inflated xml file elements *********/
	public static class ViewHolder {

		public TextView text;
		public ImageView image;

	}

	public View getView(int position, View convertView, ViewGroup parent) {

		View vi = convertView;
		ViewHolder holder;

		if (convertView == null) {

			vi = inflater.inflate(R.layout.list_item, null);

			holder = new ViewHolder();
			holder.text = (TextView) vi.findViewById(R.id.name);
			holder.image = (ImageView) vi.findViewById(R.id.icon);

			vi.setTag(holder);
		} else
			holder = (ViewHolder) vi.getTag();

		holder.text.setText("Cached image");
		ImageView image = holder.image;
		// DisplayImage function from ImageLoader Class
		imageLoader.loadSavedBitmaps(image, position);

		return vi;
	}
}
