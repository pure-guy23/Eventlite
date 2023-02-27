package uk.ac.man.cs.eventlite.config.data;

import java.time.LocalDate;
import java.time.LocalTime;
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
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Configuration
@Profile("test")
public class TestDataLoader {

	private final static Logger log = LoggerFactory.getLogger(TestDataLoader.class);

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;
	
	private final static String VENUE = "Kilburn Building";
	private final static String[][] EVENTS = { {"COMP23412 Showcase, group F", "2022-05-17", "16:00"},
			{"COMP23412 Showcase, group G", "2022-05-19", "16:00"},
			{"COMP23412 Showcase, group H", "2022-05-20", "16:00"}};

	@Bean
	CommandLineRunner initDatabase() {
		return args -> {
			Venue venue = new Venue();
			venue.setName(VENUE);
			venue.setCapacity(120);
			venueService.save(venue);
			
			log.info("Created venue " + VENUE);
			
			for (String[] s : EVENTS) {
				eventService.save(createEvent(s[0], s[1], s[2], venue));
			}
		};
	}
	
	private Event createEvent(String name, String date, String time, Venue venue) {
		Event event = new Event();
		event.setName(name);
		event.setDate(LocalDate.parse(date));
		event.setTime(LocalTime.parse(time));
		event.setVenue(venue);
		
		log.info("Created event " + name);
		
		return event;
	}
	
	
}
