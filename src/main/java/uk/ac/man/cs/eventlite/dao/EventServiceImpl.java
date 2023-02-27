package uk.ac.man.cs.eventlite.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.man.cs.eventlite.entities.Event;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Service
public class EventServiceImpl implements EventService {

	@Autowired
	private EventRepository eventRepository;

	@Override
	public long count() {
		return eventRepository.count();
	}

	@Override
	public Iterable<Event> findAll() {
		return eventRepository.findAllByOrderByDateAscTimeAsc();
	}
	
	@Override
	public Event save(Event event) {
		eventRepository.save(event);
		return event;
	}

//	@Override
//	public Iterable<Event> findAllOrderByDateAndTime() {
//		return eventRepository.findAllByOrderByDateAscTimeAsc();
//	}
	
	@Override
	public Iterable<Event> searchByName(String s){
		ArrayList<Event> events = (ArrayList<Event>) eventRepository.findByNameContainingIgnoreCase(s);
		Comparator<Event> compareDate = Comparator.comparing(Event::getDate);
		Comparator<Event> compareAlphabetically = Comparator.comparing(Event::getName);
		Comparator<Event> fullCompare = compareDate.thenComparing(compareAlphabetically);
		events = (ArrayList<Event>) events.stream().sorted(fullCompare).collect(Collectors.toList());
		return events;
	}

	@Override
	public Optional<Event> findById(long id){
		return eventRepository.findById(id);
	}
	
	@Override
	public Iterable<Event> searchByDateBefore(LocalDate date, LocalTime time){
		return eventRepository.findAllByDateAndTimeBeforeOrDateBeforeOrderByDateAscNameAsc(date, time, date);
	}
	
	@Override
	public Iterable<Event> searchByDateEqualAndAfter(LocalDate date, LocalTime time){
		return eventRepository.findAllByDateAndTimeAfterOrDateAfterOrderByDateAscNameAsc(date, time, date);
	}

	
	@Override
	  public void deleteById(long id) {
	    eventRepository.deleteById(id);
	  }
	
	
	@Override
	public Event findSingle(Long id) {
		return findById(id).orElse(null);
	}

	
}