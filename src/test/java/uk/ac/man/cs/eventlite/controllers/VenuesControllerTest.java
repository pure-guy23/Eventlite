package uk.ac.man.cs.eventlite.controllers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.endsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
//import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VenuesController.class)
@Import(Security.class)
public class VenuesControllerTest {
	private final static String BAD_ROLE = "USER";
	
	@Autowired
	private MockMvc mvc;

	@Mock
	private Venue venue;
	
	@MockBean
	private VenueService venueService;

	//unit test for finding no venues
	@Test
	public void getIndexWhenNoVenues() throws Exception {
		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());

		mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllVenues"));

		verify(venueService).findAll();
		verifyNoInteractions(venue);
 	}
	
	//unit test for venue not found
	
	@Test
	public void getVenueNotFound() throws Exception {
		mvc.perform(get("/venues/99").accept(MediaType.TEXT_HTML))
				.andExpect(status().isNotFound())
				.andExpect(view().name("venues/not_found"))
				.andExpect(handler().methodName("getVenue"));
	}
	
	//unit test for finding all venues
	@Test
	public void getIndexWithVenues() throws Exception {

		mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllVenues"));

		verify(venueService).findAll();
	}
	
	//unit test for find specific venue
	
	@Test
	public void getDetailedVenuePage() throws Exception {
		when(venueService.findById((long) 1)).thenReturn(Optional.of(venue));

		mvc.perform(get("/venues/1").accept(MediaType.TEXT_HTML))
			.andExpect(status().isOk())
			.andExpect(view().name("venues/show"))
			.andExpect(handler().methodName("getVenue"));

		verify(venueService).findById((long) 1);
	}


	//unit test for having no author
	@Test
	public void getNewVenueNoAuth() throws Exception {
		mvc.perform(get("/venues/new").accept(MediaType.TEXT_HTML))
		        .andExpect(status().isFound())
				.andExpect(header().string("Location", endsWith("/sign-in")));
	}
	
	//unit test checking if the venue have been added
	@Test
	public void getNewVenue() throws Exception {
		mvc.perform(get("/venues/new").with(user("Rob").roles(Security.ADMIN_ROLE))
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk()).andExpect(view().name("venues/new"))
				.andExpect(handler().methodName("newVenue"));
	}
	
	@Test
	public void postVenueNoAuth() throws Exception {
				
		mvc.perform(post("/venues").contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Test Venue")
			    .param("capacity", "Test Capacity")
			    .param("address", "Test Address")
			    .param("postcode", "M13 9PL")
				.accept(MediaType.TEXT_HTML).with(csrf()))
		        .andExpect(status().isFound())
				.andExpect(header().string("Location", endsWith("/sign-in")));

		verify(venueService, never()).save(venue);
	}
	
	@Test
	public void postVenueBadRole() throws Exception {
		mvc.perform(post("/venues").with(user("Rob").roles(BAD_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Test Venue")
			    .param("capacity", "Test Capacity")
			    .param("address", "Test Address")
			    .param("postcode", "M13 9PL")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isForbidden());

		verify(venueService, never()).save(venue);
	}

	@Test
	public void postVenueNoCsrf() throws Exception {
		mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "Test Venue")
			    .param("capacity", "Test Capacity")
			    .param("address", "Test Address")
			    .param("postcode", "M13 9PL")
				.accept(MediaType.TEXT_HTML)).andExpect(status().isForbidden());

		verify(venueService, never()).save(venue);
	}

	//test for name length exceeding limit
	@Test
	public void postLongVenueName() throws Exception {
		mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "KadjfsbdlajsbdalsdajhsdbfajlhsfgaiuebauivrbvadfhbviadfbvajshdbviasbdhvavkdcibWvksnDcvildviadbvakdhsjvakhfvaidvbkjshdvbkjhzxvkjhdfljvhsvdhsdlfvhafdbvaldjfhvlahklajsbdvlavdladhvbaljdfhvbadljvaljfbvaerufgaleurfbadjhfbvajdhfKadjfsbdlajsbdalsdajhsdbfajlhsfgaiuebauivrbvadfhbviadfbvajshdbviasbdhvavkdcibWvksnDcvildviadbvakdhsjvakhfvaidvbkjshdvbkjhzxvkjhdfljvhsvdhsdlfvhafdbvaldjfhvlahklajsbdvlavdladhvbaljdfhvbadljvaljfbvaerufgaleurfbadjhfbvajdhfKadjfsbdlajsbdalsdajhsdbfajlhsfgaiuebauivrbvadfhbviadfbvajshdbviasbdhvavkdcibWvksnDcvildviadbvakdhsjvakhfvaidvbkjshdvbkjhzxvkjhdfljvhsvdhsdlfvhafdbvaldjfhvlahklajsbdvlavdladhvbaljdfhvbadljvaljfbvaerufgaleurfbadjhfbvajdhf")
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
				.andExpect(view().name("venues/new"))
				.andExpect(model().attributeHasFieldErrors("venue", "name"))
				.andExpect(handler().methodName("createVenue"));

		verify(venueService, never()).save(venue);
	}
	
	//test for address length exceeding limit
	@Test
	public void postLongVenueAddress() throws Exception {
		mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("address", "KadjfsbdlajsbdalsdajhsdbfajlhsfgaiuebauivrbvadfhbviadfbvajshdbviasbdhvavkdcibWvksnDcvildviadbvakdhsjvakhfvaidvbkjshdvbkjhzxvkjhdfljvhsvdhsdlfvhafdbvaldjfhvlahklajsbdvlavdladhvbaljdfhvbadljvaljfbvaerufgaleurfbadjhfbvajdhfKadjfsbdlajsbdalsdajhsdbfajlhsfgaiuebauivrbvadfhbviadfbvajshdbviasbdhvavkdcibWvksnDcvildviadbvakdhsjvakhfvaidvbkjshdvbkjhzxvkjhdfljvhsvdhsdlfvhafdbvaldjfhvlahklajsbdvlavdladhvbaljdfhvbadljvaljfbvaerufgaleurfbadjhfbvajdhfKadjfsbdlajsbdalsdajhsdbfajlhsfgaiuebauivrbvadfhbviadfbvajshdbviasbdhvavkdcibWvksnDcvildviadbvakdhsjvakhfvaidvbkjshdvbkjhzxvkjhdfljvhsvdhsdlfvhafdbvaldjfhvlahklajsbdvlavdladhvbaljdfhvbadljvaljfbvaerufgaleurfbadjhfbvajdhf")
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
				.andExpect(view().name("venues/new"))
				.andExpect(model().attributeHasFieldErrors("venue", "address"))
				.andExpect(handler().methodName("createVenue"));

		verify(venueService, never()).save(venue);
	}
	
	//test for no name entered
	@Test
	public void postNullVenueName() throws Exception {
		mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "")
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
				.andExpect(view().name("venues/new"))
				.andExpect(handler().methodName("createVenue"));

		verify(venueService, never()).save(venue);
	}
	
	
	//test for no capacity entered
	@Test
	public void postNullVenueCapacity() throws Exception {
		mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("capacity", "")
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
				.andExpect(view().name("venues/new"))
				.andExpect(handler().methodName("createVenue"));

		verify(venueService, never()).save(venue);
	}
	
	//test for no address entered
	@Test
	public void postNullVenueAddress() throws Exception {
		mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("address", "")
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
				.andExpect(view().name("venues/new"))
				.andExpect(handler().methodName("createVenue"));

		verify(venueService, never()).save(venue);
	}

	//test for positive constraint of capacity
	@Test
	public void postNotPositiveIntegerForCapacity() throws Exception {
		int i = -1;
		
		mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("capacity", "" + i)
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
				.andExpect(view().name("venues/new"))
				.andExpect(handler().methodName("createVenue"));

		verify(venueService, never()).save(venue);
	}
	
	//test for no postcode
	@Test
	public void postNullVenuePostcode() throws Exception {
		mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("postcode", "")
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
				.andExpect(view().name("venues/new"))
				.andExpect(handler().methodName("createVenue"));

		verify(venueService, never()).save(venue);
	}
	
	//test for illegal postcode
	@Test
	public void postInvalidPostcode() throws Exception {
		mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("postcode", "1234567")
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk())
				.andExpect(view().name("venues/new"))
				.andExpect(handler().methodName("createVenue"));

		verify(venueService, never()).save(venue);
	}
	
	@Test
	public void getVenueLongitude() throws Exception {
		
		Venue venueTest = new Venue();
		
		venueTest.setName("Kilburn Building");	
		venueTest.setAddress("Kilburn Building University of Manchester, Oxford Rd, Manchester");
		venueTest.setPostcode("M13 9PL");
		
		double longitude = venueTest.getLongitude();
		
		assertEquals(Math.round(longitude * 10)/10.0, -2.2);
	}
	
	@Test
	public void getVenueLatitude() throws Exception {
		Venue venueTest = new Venue();
		
		venueTest.setName("Kilburn Building");
		venueTest.setAddress("Kilburn Building University of Manchester, Oxford Rd, Manchester");
		venueTest.setPostcode("M13 9PL");
		
		double latitude = venueTest.getLatitude();
		
		assertEquals(Math.round(latitude * 10)/10.0, 53.5);
	
	}
	
	@Test
	public void getNullVenueLongitude() throws Exception {
		
		Venue venueTest = new Venue();
		
		venueTest.setName("Kilburn Building");	
		venueTest.setAddress("");
		venueTest.setPostcode("");
		
		double longitude = venueTest.getLongitude();
		
		assertEquals(Math.round(longitude * 10)/10.0, 0.0);
	}
	
	@Test
	public void getNullVenueLatitude() throws Exception {
		Venue venueTest = new Venue();
		
		venueTest.setName("Kilburn Building");
		venueTest.setAddress("");
		venueTest.setPostcode("");
		
		double latitude = venueTest.getLatitude();
		
		assertEquals(Math.round(latitude * 10)/10.0, 0.0);
	
	}
	
	@Test
	public void deleteExistingVenue() throws Exception {
		when(venueService.findById((long)2)).thenReturn(Optional.of(venue));
		when(venue.getEvents()).thenReturn(Collections.emptyList());

		mvc.perform(delete("/venues/2")
			.with(user("Rob").roles(Security.ADMIN_ROLE))
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound())
			.andExpect(handler().methodName("deleteVenue"))
			.andExpect(redirectedUrl("/venues"))
			.andExpect(flash().attribute("deleteVenueError", org.hamcrest.Matchers.nullValue()));

		verify(venueService).deleteById(2);
	}
	
	@Test
	public void deletingExitingVenueWithEvent () throws Exception {
		when(venueService.findById((long)1)).thenReturn(Optional.of(venue));
		when(venue.getEvents()).thenReturn(Collections.singletonList(new Event()));

		mvc.perform(delete("/venues/1")
			.with(user("Rob").roles(Security.ADMIN_ROLE))
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound())
			.andExpect(handler().methodName("deleteVenue"))
			.andExpect(status().isFound())
			.andExpect(redirectedUrl("/venues/venue_show/1"))
			.andExpect(flash().attribute("deleteVenueError","Cannot delete venue with one or more events."));
		verify(venueService).findById((long)1);

	}
	

	@Test
	public void testingUpdateVenue() throws Exception {
		when(venueService.findSingle((long)1)).thenReturn(venue);
		mvc.perform(post("/venues/updateVenue/1").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name", "testVenue").param("postcode", "M13 9PL")			
				.param("address", "test address")
				.param("capacity", "200")
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound())
				.andExpect(handler().methodName("updateVenues"))
				.andExpect(redirectedUrl("/venues"));

		verify(venueService, never()).save(venue);
	}
	
	@Test
	public void getUpdateVenue() throws Exception {
		when(venueService.findSingle((long)1)).thenReturn(venue);
		mvc.perform(get("/venues/updateVenue/1").with(user("Rob").roles(Security.ADMIN_ROLE))
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk()).andExpect(view().name("venues/updateVenue"))
				.andExpect(handler().methodName("updateLockId"));

	}

}
