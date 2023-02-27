package uk.ac.man.cs.eventlite.controllers;

import javax.validation.Valid;

import java.io.Console;
import java.util.ArrayList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import twitter4j.TwitterException;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.TwitterService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javax.validation.Valid;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@Autowired
	private TwitterService twitterService;

	@ExceptionHandler(EventNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String eventNotFoundHandler(EventNotFoundException ex, Model model) {
		model.addAttribute("not_found_id", ex.getId());

		return "events/not_found";
	}


	@GetMapping("/{id}")
	public String getEventDetails(@PathVariable("id") long id, Model model) {
		Event event = eventService.findById(id).orElseThrow(() -> new EventNotFoundException(id));

		model.addAttribute("event", event);

		return "events/show";
	}


//	@GetMapping
//	public String getAllEvents(Model model) {
//		model.addAttribute("events", eventService.findAll());
//
//		return "events/index";
//	}

	@GetMapping("/new")
	public String newEvent(Model model) {
		if (!model.containsAttribute("event")) {
			model.addAttribute("event", new Event());
			model.addAttribute("venue", venueService.findAll());
		}
		return "events/new";
	}

	@PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String createEvent(@RequestBody @Valid @ModelAttribute Event event, BindingResult errors, Model model,
			RedirectAttributes redirectAttrs) {

		if (errors.hasErrors()) {
			model.addAttribute("event", event);
			model.addAttribute("venue", venueService.findAll());
			return "events/new";
		}

		eventService.save(event);
		redirectAttrs.addFlashAttribute("ok_message", "New event added.");

		return "redirect:/events";
	}

	@GetMapping("/search")
	public String getSearchedEvents(@RequestParam(required = false, value = "keyword") String keyword, Model model) {
		if (keyword == null) {
			keyword = "";
		}
		// sample date and time to sort events into upcoming and past
		LocalDate date = LocalDate.now();
		LocalTime time = LocalTime.now();
		DateTimeFormatter formatType = DateTimeFormatter.ofPattern("HH:mm");
		String timeFormat = time.format(formatType);

		// add into different ArrayList based on search string and date
		ArrayList<Event> matchedEvents = (ArrayList<Event>) eventService.searchByName(keyword);
		ArrayList<Event> beforeEvents = (ArrayList<Event>) eventService.searchByDateBefore(date,
				LocalTime.parse(timeFormat));
		ArrayList<Event> afterEvents = (ArrayList<Event>) eventService.searchByDateEqualAndAfter(date,
				LocalTime.parse(timeFormat));

		// filters out events that were not searched for
		beforeEvents.retainAll(matchedEvents);
		afterEvents.retainAll(matchedEvents);

		// adds lists to model
		model.addAttribute("eventsPast", beforeEvents);
		model.addAttribute("eventsUpcoming", afterEvents);
		model.addAttribute("tweets", twitterService.getPreviousFiveTweets());

		return "events/index";
	}

	@GetMapping
	public String getAllEvents(Model model) {
		LocalDate date = LocalDate.now();
		LocalTime time = LocalTime.now();
		DateTimeFormatter formatType = DateTimeFormatter.ofPattern("HH:mm");
		String timeFormat = time.format(formatType);
		model.addAttribute("eventsPast", eventService.searchByDateBefore(date, LocalTime.parse(timeFormat)));
		model.addAttribute("eventsUpcoming", eventService.searchByDateEqualAndAfter(date, LocalTime.parse(timeFormat)));
		model.addAttribute("tweets", twitterService.getPreviousFiveTweets());

		return "events/index";
	}

	@DeleteMapping("/{id}")
	  public String deleteEvents(@PathVariable("id") long id, RedirectAttributes redirectAttrs) {

	
//	    if (!eventService.existsById(id)) {
//	      throw new eventNotFoundException(id);
//	    }

		eventService.deleteById(id);
		redirectAttrs.addFlashAttribute("ok_message", "Event deleted.");

		return "redirect:/events";
	}

	@GetMapping("/updateEvent/{id}")
	public String updateLockId(Model model, @PathVariable("id") long id) {

		model.addAttribute("event", eventService.findSingle(id));
		
		Event event = eventService.findById(id).orElseThrow(() -> new EventNotFoundException(id));
		if (!model.containsAttribute("event")) {
			model.addAttribute("event", event);
		}
		model.addAttribute("venues", venueService.findAll());
		return "events/updateEvent";

	}

	@PostMapping(value = "/updateEvent/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String updateEvents(@PathVariable("id") long id, @RequestBody @Valid @ModelAttribute Event event,
			BindingResult result, Model model, RedirectAttributes redirectAttrs) {

		eventService.save(event);
		redirectAttrs.addFlashAttribute("ok_message", "Event updated.");

		return "redirect:/events";

	}

	@PostMapping(value = "/{id}")
	public String createTweet(@PathVariable("id") long id, String message, RedirectAttributes redirect) {
		try {
			twitterService.updateStatus(message);
			redirect.addFlashAttribute("twitter_success", "You have sent the following: " + message);
		} catch (TwitterException e) {
			redirect.addFlashAttribute("twitter_error", "An error has occurred");
		}

		return "redirect:/events/"+id;
	}
}

