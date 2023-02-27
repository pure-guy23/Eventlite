package uk.ac.man.cs.eventlite.controllers;

import static java.util.stream.Collectors.toCollection;

import java.util.ArrayList;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import uk.ac.man.cs.eventlite.assemblers.VenueModelAssembler;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;

@RestController
@RequestMapping(value = "/api/venues", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
public class VenuesControllerApi {
	
	@Autowired
	private VenueService venueService;
	
	@Autowired
	private VenueModelAssembler venueAssembler;
	
	@GetMapping
	public CollectionModel<Venue> getAllVenues() {
		return venueCollection(venueService.findAll());
	}
	
	@GetMapping("/{id}")
	public EntityModel<Venue> getVenue(@PathVariable("id") long id) {
		return venueAssembler.toModel(venueService.findById(id).orElseThrow(() -> new EventNotFoundException(id)));
	}
	
	@GetMapping("/{id}/events")
	public CollectionModel<Event> getVenueEvents(@PathVariable("id") long id) {
		Venue venue = venueService.findById(id).orElseThrow(() -> new EventNotFoundException(id));
		return CollectionModel.of(venue.getEvents());
	}
	
	@GetMapping("/{id}/next3events")
	public CollectionModel<Event> getVenueNext3Events(@PathVariable("id") long id) {
		Venue venue = venueService.findById(id).orElseThrow(() -> new EventNotFoundException(id));
		ArrayList<Event> events = new ArrayList<Event>(venue.getEvents());
		return CollectionModel.of(events.subList(0, 3));
	}
	
	private CollectionModel<Venue> venueCollection(Iterable<Venue> venues) {
		Link selfLink = linkTo(methodOn(VenuesControllerApi.class).getAllVenues()).withSelfRel();
		return CollectionModel.of(venues, selfLink);
	}
}
