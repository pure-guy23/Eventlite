package uk.ac.man.cs.eventlite.controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.Mockito.never;

import static org.mockito.ArgumentMatchers.any;
import org.mockito.ArgumentCaptor;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.TwitterService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@ExtendWith(SpringExtension.class)
@WebMvcTest(EventsController.class)
@Import(Security.class)
public class EventsControllerTest {
	
	//role for testing
	private final static String BAD_ROLE = "USER";
	
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	String date = format.format(new Date(new Date().getTime() + 3600*48*1000));


	@Autowired
	private MockMvc mvc;

	@Mock
	private Event event;

	@Mock
	private Venue venue;

	@MockBean
	private EventService eventService;

	@MockBean
	private VenueService venueService;
	
	@MockBean 
	private TwitterService twitterService;

//	@Test
//	public void getIndexWhenNoEvents() throws Exception {
//		when(eventService.findAll()).thenReturn(Collections.<Event>emptyList());
//
//		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
//				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));
//
//		verify(eventService).findAll();
//		verifyNoInteractions(event);
//		verifyNoInteractions(venue);
//	}
	

	@Test
	public void getDetailedEventPage() throws Exception {
		// when trying to fetch an event's venue, return a venue for the sake of the mock
		when(event.getVenue()).thenReturn(venue);
		when(eventService.findById(2)).thenReturn(Optional.of(event));
		

		mvc.perform(get("/events/2").accept(MediaType.TEXT_HTML))
			.andExpect(status().isOk())
			.andExpect(view().name("events/show"))
			.andExpect(handler().methodName("getEventDetails"))
			.andExpect(model().attribute("event", event));

		verify(eventService).findById(2);
	}
	

//	@Test
//	public void getIndexWithEvents() throws Exception {
//		when(venue.getName()).thenReturn("Kilburn Building");
//
//		when(event.getVenue()).thenReturn(venue);
//		when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(event));
//
//		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
//				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));
//
//		verify(eventService).findAll();	
//	}

    @Test
	public void getEventNotFound() throws Exception {
		mvc.perform(get("/events/99").accept(MediaType.TEXT_HTML)).andExpect(status().isNotFound())
				.andExpect(view().name("events/not_found")).andExpect(handler().methodName("getEventDetails"));
	}

	
	//unit test for no author
	@Test
	public void getNewEventNoAuth() throws Exception {
		mvc.perform(get("/events/new").accept(MediaType.TEXT_HTML))
		        .andExpect(status().isFound())
				.andExpect(header().string("Location", endsWith("/sign-in")));
	}
	
	//unit test for checking if event being added
	@Test
	public void getNewEvent() throws Exception {
		mvc.perform(get("/events/new").with(user("Rob").roles(Security.ADMIN_ROLE))
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk()).andExpect(view().name("events/new"))
				.andExpect(handler().methodName("newEvent"));
	}
	
	@Test
	public void postEventNoAuth() throws Exception {
				
		mvc.perform(post("/events").contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Test Event")
			    .param("description", "Test description")
			    .param("venue", "" + venue.getId())
			    .param("date", date)
			    .param("time", "00:00:00")
				.accept(MediaType.TEXT_HTML).with(csrf()))
		        .andExpect(status().isFound())
				.andExpect(header().string("Location", endsWith("/sign-in")));

		verify(eventService, never()).save(event);
	}
	
	@Test
	public void postEventBadRole() throws Exception {
		mvc.perform(post("/events").with(user("Rob").roles(BAD_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Test Event")
			    .param("description", "Test description")
			    .param("venue", "" + venue.getId())
			    .param("date", date)
			    .param("time", "00:00:00")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isForbidden());

		verify(eventService, never()).save(event);
	}
	
	@Test
	public void postEventNoCsrf() throws Exception {
		mvc.perform(post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Test Event")
			    .param("description", "Test description")
			    .param("venue", "" + venue.getId())
			    .param("date", date)
			    .param("time", "00:00:00")
				.accept(MediaType.TEXT_HTML)).andExpect(status().isForbidden());

		verify(eventService, never()).save(event);
	}
	
	//test for name length which shouldn't exceed 256 characters
//	@Disabled
	@Test
	public void postLongEventName() throws Exception {
		mvc.perform(post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "KadjfsbdlajsbdalsdajhsdbfajlhsfgaiuebauivrbvadfhbviadfbvajshdbviasbdhvavkdcibWvksnDcvildviadbvakdhsjvakhfvaidvbkjshdvbkjhzxvkjhdfljvhsvdhsdlfvhafdbvaldjfhvlahklajsbdvlavdladhvbaljdfhvbadljvaljfbvaerufgaleurfbadjhfbvajdhfKadjfsbdlajsbdalsdajhsdbfajlhsfgaiuebauivrbvadfhbviadfbvajshdbviasbdhvavkdcibWvksnDcvildviadbvakdhsjvakhfvaidvbkjshdvbkjhzxvkjhdfljvhsvdhsdlfvhafdbvaldjfhvlahklajsbdvlavdladhvbaljdfhvbadljvaljfbvaerufgaleurfbadjhfbvajdhfKadjfsbdlajsbdalsdajhsdbfajlhsfgaiuebauivrbvadfhbviadfbvajshdbviasbdhvavkdcibWvksnDcvildviadbvakdhsjvakhfvaidvbkjshdvbkjhzxvkjhdfljvhsvdhsdlfvhafdbvaldjfhvlahklajsbdvlavdladhvbaljdfhvbadljvaljfbvaerufgaleurfbadjhfbvajdhf")
				.param("description", "Test description")
			    .param("venue", "" + venue.getId())
			    .param("date", date)
			    .param("time", "00:00:00")
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
				.andExpect(view().name("events/new"))
				.andExpect(model().attributeHasFieldErrors("event", "name"))
				.andExpect(handler().methodName("createEvent")).andExpect(flash().attributeCount(0));

		verify(eventService, never()).save(event);
	}
	
	//test for description length which shouldn't exceed 500 characters
//	@Disabled
	@Test
	public void postLongEventDescription() throws Exception {
		mvc.perform(post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("description", "KadjfsbdlajsbdalsdajhsdbfajlhsfgaiuebauivrbvadfhbviadfbvajshdbviasbdhvavkdcibWvksnDcvildviadbvakdhsjvakhfvaidvbkjshdvbkjhzxvkjhdfljvhsvdhsdlfvhafdbvaldjfhvlahklajsbdvlavdladhvbaljdfhvbadljvaljfbvaerufgaleurfbadjhfbvajdhfbvljadhfgajlrhfvbadjfhbvlajsbvljadhvbalieuvajlhvbadljfhvbalufvaldjhfbvjlashbdvlajhrvfjlndsfkbvsdbvsdlfbvjbsfdvbsfkvjbsifuvbejkfbliuegjsdbfjlvhadfkaljdfajdbfvlhaueibviebvjhdfbvdfbvjbdfjbeouiebrvjsdfhviadbvijbdfvjhdbfvhdbfjvdkfuehrifbefjhbveifvbdfvsdfbsgbdfgbrtbsgbsbgsrtbrthbtrhtdhsfgbwr")
				.param("name", "Test Event")
				.param("venue", "" + venue.getId())
			    .param("date", date)
			    .param("time", "00:00:00")
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
				.andExpect(view().name("events/new"))
				.andExpect(handler().methodName("createEvent")).andExpect(flash().attributeCount(0));

		verify(eventService, never()).save(event);
	}
	
	//test for the case that event name is null
	@Test
	public void postNullEventName() throws Exception {
		mvc.perform(post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "")
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
				.andExpect(view().name("events/new"))
				.andExpect(handler().methodName("createEvent"));

		verify(eventService, never()).save(event);
	}
	
	//test for the case that event venue is null
	@Test
	public void postNullEventVenue() throws Exception {
		mvc.perform(post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("venue", "")
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
				.andExpect(view().name("events/new"))
				.andExpect(handler().methodName("createEvent"));

		verify(eventService, never()).save(event);
	}
	
	//test for the case that event date is null
	@Test
	public void postNullEventDate() throws Exception {
		mvc.perform(post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("date", "")
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
				.andExpect(view().name("events/new"))
				.andExpect(handler().methodName("createEvent"));

		verify(eventService, never()).save(event);
	}
	
	//test for the case that event date is not in the future
	@Test
	public void postEventNotInFuture() throws Exception {
		
		String date1 = format.format(new Date(new Date().getTime() - 3600*48*1000));
		
		mvc.perform(get("/events/new").with(user("Rob").roles(Security.ADMIN_ROLE))
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk()).andExpect(view().name("events/new"))
				.andExpect(handler().methodName("newEvent"));

		mvc.perform(post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("date", date1)
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
				.andExpect(view().name("events/new"))
				.andExpect(handler().methodName("createEvent"));

		verify(eventService, never()).save(event);
	}

	@Test
	public void getIndexWhenNoEventsPast() throws Exception {
		LocalDate date = LocalDate.now();
		LocalTime time = LocalTime.now();
		DateTimeFormatter formatType = DateTimeFormatter.ofPattern("HH:mm");
		String timeFormat = time.format(formatType);
		when(eventService.searchByDateBefore(date, LocalTime.parse(timeFormat))).thenReturn(Collections.<Event>emptyList());

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(eventService).searchByDateBefore(date, LocalTime.parse(timeFormat));
		verifyNoInteractions(event);
		verifyNoInteractions(venue);
	}
	
	@Test
	public void getIndexWhenNoEventsUpcoming() throws Exception {
		LocalDate date = LocalDate.now();
		LocalTime time = LocalTime.now();
		DateTimeFormatter formatType = DateTimeFormatter.ofPattern("HH:mm");
		String timeFormat = time.format(formatType);
		when(eventService.searchByDateEqualAndAfter(date, LocalTime.parse(timeFormat))).thenReturn(Collections.<Event>emptyList());

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(eventService).searchByDateEqualAndAfter(date, LocalTime.parse(timeFormat));
		verifyNoInteractions(event);
		verifyNoInteractions(venue);
	}
	
	@Test
	public void getIndexWithEventsPast() throws Exception {
		LocalDate date = LocalDate.now();
		LocalTime time = LocalTime.now();
		DateTimeFormatter formatType = DateTimeFormatter.ofPattern("HH:mm");
		String timeFormat = time.format(formatType);
		when(venue.getName()).thenReturn("Kilburn Building");

		when(event.getVenue()).thenReturn(venue);
		when(eventService.searchByDateBefore(date, LocalTime.parse(timeFormat))).thenReturn(Collections.<Event>singletonList(event));

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(eventService).searchByDateBefore(date, LocalTime.parse(timeFormat));	
	}
	
	@Test
	public void getIndexWithEventsUpcoming() throws Exception {
		LocalDate date = LocalDate.now();
		LocalTime time = LocalTime.now();
		DateTimeFormatter formatType = DateTimeFormatter.ofPattern("HH:mm");
		String timeFormat = time.format(formatType);
		when(venue.getName()).thenReturn("Kilburn Building");

		when(event.getVenue()).thenReturn(venue);
		when(eventService.searchByDateBefore(date, LocalTime.parse(timeFormat))).thenReturn(Collections.<Event>singletonList(event));

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(eventService).searchByDateEqualAndAfter(date, LocalTime.parse(timeFormat));	
	}
	
	
	@Test
	public void getUpdateEvent() throws Exception {
		when(event.getVenue()).thenReturn(venue);
		when(eventService.findById(1)).thenReturn(Optional.of(event));

		mvc.perform(get("/events/updateEvent/1").with(user("Rob").roles(Security.ADMIN_ROLE)).accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk()).andExpect(view().name("events/updateEvent"))
				.andExpect(handler().methodName("updateLockId"));
	}
	
	@Test
	public void postUpdateEvent() throws Exception {
		ArgumentCaptor<Event> arg = ArgumentCaptor.forClass(Event.class);
		when(eventService.save(any(Event.class))).then(returnsFirstArg());

		mvc.perform(post("/events/updateEvent/1").with(user("Rob").roles(Security.ADMIN_ROLE)).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Event Test").param("date", LocalDate.now().plusYears(1).toString()).param("venue.id", "1")
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound()).andExpect(content().string(""))
				.andExpect(view().name("redirect:/events")).andExpect(model().hasNoErrors())
				.andExpect(handler().methodName("updateEvents")).andExpect(flash().attributeExists("ok_message"));

		verify(eventService).save(arg.capture());
	}
	

	@Test
	public void postUpdateEventBadRole() throws Exception {
		mvc.perform(post("/events/updateEvent/1").with(user("Rob").roles(BAD_ROLE)).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Event Test").param("date", LocalDate.now().plusYears(1).toString()).param("venue.id", "1")
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isForbidden());

		verify(eventService, never()).save(any(Event.class));
	}

	
	@Test
	public void postUpdateEventWithOptional() throws Exception {
		ArgumentCaptor<Event> arg = ArgumentCaptor.forClass(Event.class);
		when(eventService.save(any(Event.class))).then(returnsFirstArg());

		mvc.perform(post("/events/updateEvent/1").with(user("Rob").roles(Security.ADMIN_ROLE)).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Event Test").param("date", LocalDate.now().plusYears(1).toString()).param("time", "18:00")
				.param("venue.id", "1").param("description", "Description of the event")
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound()).andExpect(content().string(""))
				.andExpect(view().name("redirect:/events")).andExpect(model().hasNoErrors())
				.andExpect(handler().methodName("updateEvents")).andExpect(flash().attributeExists("ok_message"));

		verify(eventService).save(arg.capture());
	}


	
	@Test
	public void getUpdateEventNotFound() throws Exception {
		mvc.perform(get("/events/updateEvent/8").with(user("Rob").roles(Security.ADMIN_ROLE)).accept(MediaType.TEXT_HTML))
				.andExpect(status().isNotFound()).andExpect(view().name("events/not_found")).andExpect(handler().methodName("updateLockId"));
	}


	@Test
	public void ItShouldCallTwitterService() throws Exception {
		String message = "twitter message";
		mvc.perform(post("/events/1").with(user("Mustafa").roles(Security.ADMIN_ROLE))
				.accept(MediaType.TEXT_HTML)
				.with(csrf())
				.param("message", message))
				.andExpect(status().isFound())
				.andExpect(view().name("redirect:/events/1"))
				.andExpect(handler().methodName("createTweet"));
		
		verify(twitterService).updateStatus(message);
	}
}
