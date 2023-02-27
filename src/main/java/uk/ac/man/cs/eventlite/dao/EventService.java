package uk.ac.man.cs.eventlite.dao;

import java.util.Optional;

import uk.ac.man.cs.eventlite.entities.Event;
import java.util.Optional;

import java.time.LocalDate;
import java.time.LocalTime;

public interface EventService {

	public long count();

	public Iterable<Event> findAll();
	
	//public Iterable<Event> findAllOrderByDateAndTime();
	
	public Iterable<Event> searchByName(String s);
	
	public Event save(Event event);

	public Optional<Event> findById(long id);
	
	public Iterable<Event> searchByDateBefore(LocalDate date, LocalTime time);
	
	public Iterable<Event> searchByDateEqualAndAfter(LocalDate date, LocalTime time);
	
	public void deleteById(long id);
	
	public Event findSingle(Long id);
	
}
