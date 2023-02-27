package uk.ac.man.cs.eventlite.dao;

import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.man.cs.eventlite.entities.Venue;

@Service
public class VenueServiceImpl implements VenueService {

	private final static Logger log = LoggerFactory.getLogger(VenueServiceImpl.class);

	private final static String DATA = "data/venues.json";

	@Autowired
	private VenueRepository venueRepository;
	
	@Override
	public long count() {
//		long count = 0;
//		Iterator<Venue> i = findAll().iterator();
//
//		for (; i.hasNext(); count++) {
//			i.next();
//		}

		return venueRepository.count();
	}

	@Override
	public Iterable<Venue> findAll() {
//		Iterable<Venue> venues;
//
//		try {
//			ObjectMapper mapper = new ObjectMapper();
//			InputStream in = new ClassPathResource(DATA).getInputStream();
//
//			venues = mapper.readValue(in, mapper.getTypeFactory().constructCollectionType(List.class, Venue.class));
//		} catch (Exception e) {
//			// If we can't read the file, then the event list is empty...
//			log.error("Exception while reading file '" + DATA + "': " + e);
//			venues = Collections.emptyList();
//		}
//
//		return venues;
		return venueRepository.findAll();
	}
	
	@Override
	public Venue save(Venue venue) {
		venueRepository.save(venue);
		return venue;
	}
	
	@Override
	public Optional<Venue> findById(long id){
		return venueRepository.findById(id);
	}

	@Override 
	public Iterable<Venue> searchByName(String s){
		return venueRepository.findByNameContainingIgnoreCase(s);
	}
		
	@Override
	public Iterable<Venue> findAllByOrderByNumOfEvents() {
		return venueRepository.findAllByOrderByNumOfEvents();
	}
	@Override
	  public void deleteById(long id) {
	    venueRepository.deleteById(id);
	  }
	
	@Override
	public Venue findSingle(Long id) {
		return findById(id).orElse(null);
	}
}
