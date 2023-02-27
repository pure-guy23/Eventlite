package uk.ac.man.cs.eventlite.controllers;

import java.io.Console;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

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
//import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;
//import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;
import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;

@Controller
@RequestMapping(value = "/venues", produces = { MediaType.TEXT_HTML_VALUE })
public class VenuesController {

//	@Autowired
//	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@ExceptionHandler(VenueNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String venueNotFoundHandler(VenueNotFoundException ex, Model model) {
		model.addAttribute("not_found_id", ex.getId());

		return "venues/not_found";
	}
	
	@GetMapping("/{id}")
	public String getVenue(@PathVariable("id") long id, Model model) {
		Venue venue = venueService.findById(id).orElseThrow(() -> new VenueNotFoundException(id));
		
		model.addAttribute("venue", venue);
		return "venues/show";
	}


	@GetMapping
	public String getAllVenues(Model model) {
		model.addAttribute("venues", venueService.findAll());

		return "venues/index";
	}
	

	//add
	@GetMapping("/new")
	public String newVenue(Model model) {
		if (!model.containsAttribute("venue")) {
			model.addAttribute("venue", new Venue());
		}
		return "venues/new";
	}
	
	//add
	@PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String createVenue(@RequestBody @Valid @ModelAttribute Venue venue, BindingResult errors, Model model, RedirectAttributes redirectAttrs) {
		if (errors.hasErrors()) {
			model.addAttribute("venue", venue);
			return "venues/new";
		}
		venueService.save(venue);
		redirectAttrs.addFlashAttribute("ok_message", "New Venue Added.");
		return "redirect:/venues";
	}
	
	@GetMapping("/search")
	public String getSearchedEvents(@RequestParam(required=false, value="keyword") String keyword, Model model) {
		if (keyword == null) {
			keyword = "";
		}
		
		// add into different ArrayList based on search string and date
		ArrayList<Venue> matchedVenues = (ArrayList<Venue>) venueService.searchByName(keyword);
		
		// adds lists to model
		model.addAttribute("venues", matchedVenues);
		return "venues/index";
	}
	
	@DeleteMapping("/{id}")
    public String deleteVenue(Model model, @PathVariable("id") long id, RedirectAttributes redirect) {

    	Optional<Venue> optionalVenue = venueService.findById(id);
    	Venue venue = optionalVenue.get();
    	if (!venue.getEvents().isEmpty()) {
    		redirect.addFlashAttribute("deleteVenueError","Cannot delete venue with one or more events.");
    		return "redirect:/venues/" + id;
    	}
    	venueService.deleteById(id);
    	return "redirect:/venues";
	}
	
	@GetMapping("/updateVenue/{id}")
	public String updateLockId(Model model, @PathVariable("id") long id) {
		
		model.addAttribute("venue", venueService.findSingle(id));
		return "venues/updateVenue";
		
	}

	@PostMapping(value = "/updateVenue/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String updateVenues(@PathVariable("id") long id, @RequestBody @Valid @ModelAttribute Venue venue,
			BindingResult result, Model model, RedirectAttributes redirectAttrs) {
		venueService.save(venue);
		return "redirect:/venues";
	}

    

}
