package uk.ac.man.cs.eventlite.entities;

import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


@Entity
@Table(name = "venues")
public class Venue {
	
	private final static Logger log = LoggerFactory.getLogger(Venue.class);
	
	@Id
	@GeneratedValue
	private long id;
	
	@JsonIgnore
	@OneToMany(mappedBy="venue")
	@OrderBy("date ASC, time ASC")
	private List<Event> events;

	@NotNull(message = "Venue name is empty!")
	@Size(max = 255, message = "Name length shouldn't exceed 256 characters!")
	private String name;

	@NotNull(message = "Venue capacity is empty!")
	@Positive(message = "Venue capacity must be greater than 0!")
	private int capacity;
	
	@Size(max = 299, message = "Venue address shouldn't exceed 300 characters!")
	private String address;
	
	//not sure if post code is needed but still added
	//make sure the post code is legal
	@Pattern(regexp="^[A-Z]{1,2}[0-9R][0-9A-Z]? [0-9][ABD-HJLNP-UW-Z]{2}$", message="Invalid postcode")
	@NotNull(message = "Venue postcode is empty")
	private String postcode;
	
	private double longitude;
	
	private double latitude;

	public Venue() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public List<Event> getEvents() {
		return events;
	}
	
	public void setEvents(List<Event> events) {
		this.events = events;
	}
	
	//getter and setter for address
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	//getter and setter for postcode
	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
		retrieveCoordinates(address + " " + postcode);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//getter and setter for longitude
	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
		
	//getter and setter for latitude
	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	public void retrieveCoordinates(String address) {
		MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
				.accessToken("pk.eyJ1IjoiZXZlbnRsaXRlLWYxMiIsImEiOiJjbDFmaXZpZGkwMzAzM2ltcGYwNDkydzJnIn0.sG5gqWfu65hN5_wrHgv1tQ")
				.query(address)
				.build();
		
		mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
			@Override
			public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
		 
				List<CarmenFeature> results = response.body().features();
		 
				if (results.size() > 0) {
		 
				  // Log the first results Point.
				  Point firstResultPoint = results.get(0).center();
				  setLatitude(firstResultPoint.latitude());
				  setLongitude(firstResultPoint.longitude());
				  log.info("onResponse: " + address + firstResultPoint.toString());
		 
				} else {
		 
				  // No result for your request were found.
				  log.info("onResponse: No result found");
		 
				}
			}
		 
			@Override
			public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
				throwable.printStackTrace();
			}
		});
	}


}
