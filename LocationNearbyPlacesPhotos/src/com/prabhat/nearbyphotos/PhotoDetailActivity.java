package com.prabhat.nearbyphotos;

import android.app.Activity;
import android.os.Bundle;

import com.prabhat.nearbyphotos.util.ImageLoader;
import com.prabhat.nearbyphotos.widget.TouchImageView;

public class PhotoDetailActivity extends Activity {
	String mPhotoReference;
	private ImageLoader imageLoader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		mPhotoReference = getIntent().getStringExtra("photoreference");
		String placeName = getIntent().getStringExtra("placename");
		int photoIndexOfflineMode = getIntent().getIntExtra("photoindex", -1);
		
		if (placeName != null)
			getActionBar().setTitle(placeName);

		setContentView(R.layout.photo_detail_layout);
		TouchImageView imageView = (TouchImageView) findViewById(R.id.image);

		// Load image from cache
		imageLoader = new ImageLoader(getApplicationContext());
		if(photoIndexOfflineMode != -1) {
			imageLoader.loadSavedBitmaps(imageView, photoIndexOfflineMode);
		} else if (mPhotoReference != null) {
			imageLoader.DisplayImage(mPhotoReference, imageView);
		}
	}

}
