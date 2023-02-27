package uk.ac.man.cs.eventlite.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

import org.hibernate.mapping.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.entities.Event;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@DirtiesContext
@ActiveProfiles("test")
public class EventServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

	@InjectMocks
	private EventServiceImpl eventService;
	
	@Mock
	private EventRepository eventRepository;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	public void callsFindByNameAndReturnsResult() throws Exception {
		String searchString = "COMP23412 Showcase, group F";
		
		Event event = new Event(); // create an event
		event.setName(searchString);
		ArrayList<Event> events = new ArrayList<Event>(); 
		events.add(event); // add it to an arrayList
		
		// mock the repository method to return the events
		when(eventRepository.findByNameContainingIgnoreCase(searchString)).thenReturn(events);
		
		// assert that the service returns the defined events
		assertEquals(events, eventService.searchByName(searchString));
	}
	
	@Test
	public void callsFindByNameForNonExistingEvent() throws Exception {
		String searchString = "COMP23412 Showcase, group L";
		
		ArrayList<Event> repositoryEvents = new ArrayList<Event>();
		ArrayList<Event> resultEvents = new ArrayList<Event>();
		
		// mock the repository method to return the events
		when(eventRepository.findByNameContainingIgnoreCase(searchString)).thenReturn(repositoryEvents);
		
		// assert that the service returns the defined events
		assertEquals(resultEvents, eventService.searchByName(searchString));
	}
	
	@Test
	public void callsFindByNameForStringThatHasMultipleEvents() throws Exception {
		String searchString = "COMP";
		
		ArrayList<Event> repositoryEvents = new ArrayList<Event>();
		ArrayList<Event> resultEvents = new ArrayList<Event>();
		Event a = new Event();
		a.setName("COMP23412 Showcase, group A");
		a.setDate(LocalDate.of(2022, 10, 18));
		Event b = new Event();
		b.setDate(LocalDate.of(2022, 10, 18));
		b.setName("COMP23412 Showcase, group B");
		repositoryEvents.add(a);
		repositoryEvents.add(b);
		resultEvents.add(a);
		resultEvents.add(b);
		
		// mock the repository method to return the events
		when(eventRepository.findByNameContainingIgnoreCase(searchString)).thenReturn(repositoryEvents);
		
		// assert that the service returns the defined events
		assertEquals(resultEvents, eventService.searchByName(searchString));
	}
	
	@Test
	public void sortedByDateWhenMultipleMatch() {
		String searchString = "COMP";
		
		ArrayList<Event> repositoryEvents = new ArrayList<Event>();
		ArrayList<Event> resultEvents = new ArrayList<Event>();
		Event a = new Event();
		a.setName("COMP23412 Showcase, group A");
		a.setDate(LocalDate.of(2022, 10, 18));
		Event b = new Event();
		b.setDate(LocalDate.of(2022, 10, 19));
		b.setName("COMP23412 Showcase, group B");
		Event c = new Event();
		c.setDate(LocalDate.of(2022, 10, 17));
		c.setName("COMP23412 Showcase, group C");
		repositoryEvents.add(a);
		repositoryEvents.add(b);
		repositoryEvents.add(c);
		resultEvents.add(c);
		resultEvents.add(a);
		resultEvents.add(b);
		
		when(eventRepository.findByNameContainingIgnoreCase(searchString)).thenReturn(repositoryEvents);
		
		assertEquals(resultEvents, eventService.searchByName(searchString));
	}
	
	
	@Test
	public void sortedByNameWhenMultipleMatch() {
		String searchString = "COMP";
		
		ArrayList<Event> repositoryEvents = new ArrayList<Event>();
		ArrayList<Event> resultEvents = new ArrayList<Event>();
		Event a = new Event();
		a.setName("COMP23412 Showcase, group A");
		a.setDate(LocalDate.of(2022, 10, 18));
		Event b = new Event();
		b.setDate(LocalDate.of(2022, 10, 18));
		b.setName("COMP23412 Showcase, group B");
		Event c = new Event();
		c.setDate(LocalDate.of(2022, 10, 18));
		c.setName("COMP23412 Showcase, group C");
		repositoryEvents.add(b);
		repositoryEvents.add(a);
		repositoryEvents.add(c);
		resultEvents.add(a);
		resultEvents.add(b);
		resultEvents.add(c);
		
		when(eventRepository.findByNameContainingIgnoreCase(searchString)).thenReturn(repositoryEvents);
		
		assertEquals(resultEvents, eventService.searchByName(searchString));
	}
	
	@Test
	public void seachForEventById() throws Exception{
		eventService.findById(2);
	}
}
