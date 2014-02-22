package com.foursquare.android.fakecheckin;

public class Venue {
	public Venue() {
	}

	public Venue(String name, String venueId, String adress, String venueType,String distance) {

		this.name = name;
		this.venueId = venueId;
		this.address = adress;
		this.category = venueType;
		this.distance=distance;
	}

	public String name;
	public String venueId;
	public String address;
	public String category;
	public String distance;

}
