package com.prabhat.nearbyphotos.parser;

import android.os.Parcel;
import android.os.Parcelable;

public class PlaceWithPhoto implements Parcelable {
	// Latitude of the place
	String mLat = "";

	// Longitude of the place
	String mLng = "";

	// Place Name
	public String mPlaceName = "";

	// Vicinity of the place
	String mVicinity = "";

	// Photos of the place
	// Photo is a Parcelable class
	public Photo mPhotos = new Photo();

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	/** Writing Place object data to Parcel */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mLat);
		dest.writeString(mLng);
		dest.writeString(mPlaceName);
		dest.writeString(mVicinity);
		dest.writeParcelable(mPhotos, 0);
	}

	public PlaceWithPhoto() {
	}

	/** Initializing Place object from Parcel object */
	private PlaceWithPhoto(Parcel in) {
		this.mLat = in.readString();
		this.mLng = in.readString();
		this.mPlaceName = in.readString();
		this.mVicinity = in.readString();
		this.mPhotos = (Photo) in.readParcelable(Photo.class.getClassLoader());
	}

	/** Generates an instance of Place class from Parcel */
	public static final Parcelable.Creator<PlaceWithPhoto> CREATOR = new Parcelable.Creator<PlaceWithPhoto>() {
		@Override
		public PlaceWithPhoto createFromParcel(Parcel source) {
			return new PlaceWithPhoto(source);
		}

		@Override
		public PlaceWithPhoto[] newArray(int size) {
			// TODO Auto-generated method stub
			return null;
		}
	};
}