package com.prabhat.nearbyphotos.util;

import java.util.ArrayList;

import com.prabhat.nearbyphotos.MainActivity;
import com.prabhat.nearbyphotos.R;
import com.prabhat.nearbyphotos.parser.PlaceWithPhoto;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageLoadingAdapter extends BaseAdapter {

	private Activity activity;
	private ArrayList<PlaceWithPhoto> mPlaceWithPhoto;
	private static LayoutInflater inflater = null;
	public ImageLoader imageLoader;

	public ImageLoadingAdapter(Activity a, ArrayList<PlaceWithPhoto> data) {
		activity = a;
		mPlaceWithPhoto = data;
		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// Create ImageLoader object to download and show image in list
		// Call ImageLoader constructor to initialize FileCache
		imageLoader = new ImageLoader(activity.getApplicationContext());
	}

	public int getCount() {
		return mPlaceWithPhoto.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	/* Update dataset with new data */
	public void updateDataset(ArrayList<PlaceWithPhoto> newData) {
		mPlaceWithPhoto = newData;
		notifyDataSetChanged();
	}
	
	public ArrayList<PlaceWithPhoto> getDataset() {
		return mPlaceWithPhoto;
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

		PlaceWithPhoto placeDetail = mPlaceWithPhoto.get(position);
		if (placeDetail != null) {
			holder.text.setText(placeDetail.mPlaceName);
			ImageView image = holder.image;

			if (placeDetail.mPhotos != null) {
				// DisplayImage function from ImageLoader Class
				imageLoader.DisplayImage(placeDetail.mPhotos.mPhotoReference, image);
			}
		}
		//vi.setOnClickListener(new OnItemClickListener(position));
		return vi;
	}
}