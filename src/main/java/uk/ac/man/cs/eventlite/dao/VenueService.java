package uk.ac.man.cs.eventlite.dao;

import java.util.Optional;

import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.entities.Event;

public interface VenueService {

	public long count();

	public Iterable<Venue> findAll();
	
	public Venue save(Venue venue);

	public Optional<Venue> findById(long id);

	public Iterable<Venue> searchByName(String s);

	public Iterable<Venue> findAllByOrderByNumOfEvents();
	
	public void deleteById(long id);

	public Venue findSingle(Long id);
}
