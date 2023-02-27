package uk.ac.man.cs.eventlite.config.data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.entities.Event; 

@Configuration
@Profile("default")
public class InitialDataLoader {

	private final static Logger log = LoggerFactory.getLogger(InitialDataLoader.class);

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	private final static String[][] VENUES = { {"Kilburn Building", "120", "Kilburn Building University of Manchester, Oxford Rd, Manchester", "M13 9PL", "53.46714546040857", "-2.2342326263347654"},
			{"University Place", "150", "176 Oxford Rd, Manchester", "M13 9PL", "53.46670584699203", "-2.233872164737207"},
			{"AMBS", "200", " Booth St W, Manchester", "M15 6PB", "53.46719655398117", "-2.2341789821565"}};
	private final static String[][] EVENTS = { {"COMP23412 Showcase, group F", "Software engineering showcase for group F","2022-05-17", "16:00"},
			{"COMP23412 Showcase, group G", "Software engineering showcase for group G","2022-05-19", "16:00"},
			{"COMP23412 Showcase, group H", "Software engineering showcase for group H","2022-05-20", "16:00"},
			{"COMP23412 Showcase, group A", "Software engineering showcase for group A","2022-05-17", "16:00"},
			{"COMP23412 Showcase, group B", "Software engineering showcase for group B","2022-01-01", "16:00"}};

	@Bean
	CommandLineRunner initDatabase() {
		return args -> {
			Venue venue;
			if (venueService.count() > 0) {
				venue = ((List<Venue>) venueService.findAll()).get(0);
				
				log.info("Database already populated with venues. Skipping venue initialization.");
			} else {
				// Build and save initial venues here.
//				venue1 = new Venue();
//				venue.setName("Kilburn");
//				venue.setCapacity(120);
//				venue.setAddress("Manchester Oxford road");
//				venue.setPostcode("M14 6FY");
//				venueService.save(venue);
//				
//				log.info("Created venue " + venue.getName());
				for (String[] s : VENUES) {
					venueService.save(createVenue(s[0], Integer.valueOf(s[1]), s[2], s[3], Double.valueOf(s[4]), Double.valueOf(s[5])));
				}
				venue = ((List<Venue>) venueService.findAll()).get(0);
			}

			if (eventService.count() > 0) {
				log.info("Database already populated with events. Skipping event initialization.");
			} else {
				// Build and save initial events here.
				for (String[] s : EVENTS) {
					eventService.save(createEvent(s[0], s[1], s[2], s[3], venue));
				}
			}
		};
	}
	
	private Event createEvent(String name, String description, String date, String time, Venue venue) {
		Event event = new Event();
		event.setName(name);
		event.setDescription(description);
		event.setDate(LocalDate.parse(date));
		event.setTime(LocalTime.parse(time));
		event.setVenue(venue);
	
		
		log.info("Created event " + name);
		
		return event;
	}
	
	private Venue createVenue(String name, int capacity, String address, String postcode, double latitude, double longitude) {
		Venue venue = new Venue();
		venue.setName(name);
		venue.setCapacity(capacity);
		venue.setAddress(address);
		venue.setPostcode(postcode);
		venue.setLatitude(latitude);
		venue.setLongitude(longitude);
		
		log.info("Created venue " + name);
		
		return venue;
	}
}
