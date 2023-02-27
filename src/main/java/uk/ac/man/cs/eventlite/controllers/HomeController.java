package uk.ac.man.cs.eventlite.controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;

@Controller
@RequestMapping(value = "/", produces = { MediaType.TEXT_HTML_VALUE })
public class HomeController {

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@GetMapping("/")
	public String getHome(Model model) {
		Event event1,event2,event3;
		Iterable<Event> events = eventService.findAll();
		if(iterableCount(events)<3) {
			model.addAttribute("firstThree", events);
			}else {

		event1 = ((List<Event>) eventService.findAll()).get(0);
		event2 = ((List<Event>) eventService.findAll()).get(1);
		event3 = ((List<Event>) eventService.findAll()).get(2);
		ArrayList<Event> first_three = new ArrayList<>();
		first_three.add(event1);
		first_three.add(event2);
		first_three.add(event3);
		model.addAttribute("firstThree", first_three);}
		
//		Iterable<Venue> venues = venueService.findAll();
//		model.addAttribute("popularVenues", venues);
		model.addAttribute("popularVenues", venueService.findAllByOrderByNumOfEvents());

		return "home/index";

	}
	
	public static int iterableCount(Iterable<?> i) {
        int count = 0;
        Iterator<?> it = i.iterator();
        while (it.hasNext()) {
            it.next();
            count++;
        }
        return count;
    }

}
