package uk.ac.man.cs.eventlite.dao;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import uk.ac.man.cs.eventlite.entities.Event;
import java.time.LocalDate;
import java.time.LocalTime;


public interface EventRepository extends CrudRepository<Event, Long>{

	public Iterable<Event> findAllByOrderByDateAscTimeAsc();

	public Iterable<Event> findByNameContainingIgnoreCase(String s);
	
	public Iterable<Event> findAllByDateAndTimeBeforeOrDateBeforeOrderByDateAscNameAsc(LocalDate date, LocalTime time, LocalDate date2);
	
	public Iterable<Event> findAllByDateAndTimeAfterOrDateAfterOrderByDateAscNameAsc(LocalDate date, LocalTime time, LocalDate date2);
	
	public Optional<Event> findById(long id);

	
}
