package uk.ac.man.cs.eventlite.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.entities.Event;


public interface VenueRepository extends CrudRepository<Venue, Long> {
	@Query(value = "SELECT v FROM Venue v LEFT JOIN v.events ve GROUP BY v ORDER BY COUNT(ve.id) DESC", countQuery = "select count(v.id) from Venue v")
	public Iterable<Venue> findAllByOrderByNumOfEvents();

	public Iterable<Venue> findByNameContainingIgnoreCase(String s);

}
