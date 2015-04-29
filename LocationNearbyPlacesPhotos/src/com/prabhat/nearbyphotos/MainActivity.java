package com.prabhat.nearbyphotos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

import com.prabhat.nearbyphotos.parser.PlaceJSONParser;
import com.prabhat.nearbyphotos.parser.PlaceWithPhoto;
import com.prabhat.nearbyphotos.util.GPSTracker;
import com.prabhat.nearbyphotos.util.ImageLoadingAdapter;
import com.prabhat.nearbyphotos.util.OfflineLoadingAdapter;
import com.prabhat.nearbyphotos.util.Utils;

public class MainActivity extends Activity {

	private ListView mListView;

	private GPSTracker gps;

	private String mNextPageToken = null;

	private String mSearchQuery;

	private ArrayList<PlaceWithPhoto> mPlacesWithPhoto; //photos list for updating dynamically
	private ImageLoadingAdapter mPhotoListAdapter; //online photo adapter
	private OfflineLoadingAdapter mOfflineAdapter; //content is different from online adapter

	private LinearLayout mHeaderProgress;

	private boolean mOfflineMode = true;

	private boolean mNewSearch = false;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

		SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView search = (SearchView) menu.findItem(R.id.action_search).getActionView();
		search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
		search.setOnQueryTextListener(new OnQueryTextListener() {
			@Override
			public boolean onQueryTextChange(String query) {
				// loadData(query);
				return true;
			}

			@Override
			public boolean onQueryTextSubmit(String query) {
				//Clear erlier result
				mPlacesWithPhoto.clear();
				// mListView.setAdapter(null);

				//loading new data from search
				mNewSearch = true;
				mNextPageToken = null;
				query = query.replaceAll(" ", "%20");
				mSearchQuery = query;
				loadDataFromAPI(query);

				return true;
			}
		});

		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mHeaderProgress = (LinearLayout) findViewById(R.id.headerProgress);
		mListView = (ListView) findViewById(R.id.list);
		mPlacesWithPhoto = new ArrayList<PlaceWithPhoto>();
		
		mPhotoListAdapter = new ImageLoadingAdapter(this, mPlacesWithPhoto);
		mListView.setAdapter(mPhotoListAdapter);

		/*handle infinite scrolling list */
		mListView.setOnScrollListener(new InfiniteScrollListener() {

			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				/* Offline mode default 10 will display and next 10 on scroll 
				 * for online content place api return 20 by default
				 * next 20 will be queried to server after this and so on 
				 */
				//Toast.makeText(MainActivity.this, "page : " + page + " totalCount : " + totalItemsCount, Toast.LENGTH_LONG).show();
				if (totalItemsCount >= 50)
					return;

				if (mOfflineMode) {
					if (mOfflineAdapter != null)
						mOfflineAdapter.loadMore();
				} else {
					loadDataFromAPI(mSearchQuery);
				}
			}

		});

		/*handling list item click listener to start pinch zoom image view*/
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

				/* adapter for online and offline is different, image loading is also different */
				Intent intent = new Intent(MainActivity.this, PhotoDetailActivity.class);
				if (mOfflineMode) {
					intent.putExtra("photoindex", position);
				} else {
					String photoReference = mPhotoListAdapter.getDataset().get(position).mPhotos.mPhotoReference;
					String placeName = mPhotoListAdapter.getDataset().get(position).mPlaceName;

					intent.putExtra("photoreference", photoReference);
					intent.putExtra("placename", placeName);
				}
				startActivity(intent);
			}
		});

		// Check if network/internet connection is available
		if (!isNetworkAvailable()) {
			Toast.makeText(MainActivity.this, "Internet connection is not available. you can still view offline data.", Toast.LENGTH_LONG).show();
			mOfflineMode = true;
		}

		// load offline data for first time, when user search new data check if
		// not offline and update with real data
		mOfflineAdapter = new OfflineLoadingAdapter(this);
		mListView.setAdapter(mOfflineAdapter);

		// creating GPS Class object
		gps = new GPSTracker(this);

		// check if GPS location can get
		if (gps.canGetLocation()) {
			Log.d("Your Location", "latitude:" + gps.getLatitude() + ", longitude: " + gps.getLongitude());
		} else {
			// Can't get user's current location
			Toast.makeText(MainActivity.this, "GPS Status,Couldn't get location information. Please enable GPS", Toast.LENGTH_LONG).show();
			// stop executing code by return
			return;
		}

	}

	/*load data from places api */
	private void loadDataFromAPI(String keyword) {

		StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
		sb.append("location=" + gps.getLatitude() + "," + gps.getLongitude());
		sb.append("&radius=50000");
		sb.append("&hasNextPage=" + true);
		sb.append("&nextPage()=" + true);
		sb.append("&sensor=false");
		sb.append("&key=" + Utils.API_KEY);
		if (mNextPageToken != null) {
			sb.append("&pagetoken=" + mNextPageToken);
		}
		sb.append("&keyword=" + keyword);

		// Creating a new non-ui thread task to download Google place
		// json data
		PlacesTask placesTask = new PlacesTask();

		// Invokes the "doInBackground()" method of the class PlaceTask
		placesTask.execute(sb.toString());
	}

	/** A method to download json data from argument url */
	private String downloadUrl(String strUrl) throws IOException {
		String data = "";
		InputStream iStream = null;
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(strUrl);

			// Creating an http connection to communicate with url
			urlConnection = (HttpURLConnection) url.openConnection();

			// Connecting to url
			urlConnection.connect();

			// Reading data from url
			iStream = urlConnection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

			StringBuffer sb = new StringBuffer();

			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			data = sb.toString();

			br.close();

		} catch (Exception e) {
			Log.d("Exception while downloading url", e.toString());
		} finally {
			iStream.close();
			urlConnection.disconnect();
		}
		return data;
	}

	/** A class, to download Google Places */
	private class PlacesTask extends AsyncTask<String, Integer, String> {

		String data = null;
		private ProgressDialog pDialog;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			mHeaderProgress.setVisibility(View.VISIBLE);

			/*
			 * pDialog = new ProgressDialog(MainActivity.this);
			 * pDialog.setMessage
			 * (Html.fromHtml("<b>Search</b><br/>Loading Places..."));
			 * pDialog.setIndeterminate(false); pDialog.setCancelable(false);
			 * pDialog.show();
			 */
		}

		// Invoked by execute() method of this object
		@Override
		protected String doInBackground(String... url) {
			try {
				Log.d("URL:", url[0]);
				data = downloadUrl(url[0]);
			} catch (Exception e) {
				Log.d("Background Task", e.toString());
			}
			return data;
		}

		// Executed after the complete execution of doInBackground() method
		@Override
		protected void onPostExecute(String result) {
			ParserTask parserTask = new ParserTask();

			mNextPageToken = getNextToken(result);
			// Start parsing the Google places in JSON format
			// Invokes the "doInBackground()" method of ParserTask
			parserTask.execute(result);

			// pDialog.dismiss();
			mHeaderProgress.setVisibility(View.GONE);

		}

	}

	/*get next window tokey for loading more places */
	private String getNextToken(String json_result) {
		String token = null;
		try {
			JSONObject jObject = new JSONObject(json_result);
			token = jObject.getString("next_page_token");
		} catch (Exception e) {
			Log.d("Exception", e.toString());
		}
		return token;
	}

	/** A class to parse the Google Places in JSON format */
	private class ParserTask extends AsyncTask<String, Integer, ArrayList<PlaceWithPhoto>> {

		JSONObject jObject;

		// Invoked by execute() method of this object
		@Override
		protected ArrayList<PlaceWithPhoto> doInBackground(String... jsonData) {

			ArrayList<PlaceWithPhoto> placesWithPhoto = null; //(ArrayList<PlaceWithPhoto>) mPlacesWithPhoto.clone();
			PlaceJSONParser placeJsonParser = new PlaceJSONParser();
			try {
				jObject = new JSONObject(jsonData[0]);
				/** Getting the parsed data as a List construct */
				placesWithPhoto = placeJsonParser.parse(jObject);

			} catch (Exception e) {
				Log.d("Exception", e.toString());
			}
			return placesWithPhoto;
		}

		// Executed after the complete execution of doInBackground() method
		@Override
		protected void onPostExecute(ArrayList<PlaceWithPhoto> placesWithPhoto) {
			if(placesWithPhoto != null)
				mPlacesWithPhoto.addAll(placesWithPhoto);
			// once new data loaded from server disable offline mode
			if (placesWithPhoto == null || mPlacesWithPhoto.size() == 0) {
				Toast.makeText(MainActivity.this, "Loading failed!", Toast.LENGTH_LONG).show();
				return;
			}
			

			if (mOfflineMode && mPlacesWithPhoto.size() > 0) {
				mListView.setAdapter(new ImageLoadingAdapter(MainActivity.this, mPlacesWithPhoto));
				mOfflineMode = false;
				mNewSearch = false;
			} else {
				// update listview with new data
				if (mNewSearch) {
					mListView.setAdapter(new ImageLoadingAdapter(MainActivity.this, mPlacesWithPhoto));
					mNewSearch = false;
				} else {
					mPhotoListAdapter.updateDataset(mPlacesWithPhoto);
				}
			}
		}

	}

	/*check if internet connection is available or not */
	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
}