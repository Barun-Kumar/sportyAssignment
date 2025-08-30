package com.sporty.controller;


import com.sporty.model.BetDTO;
import com.sporty.model.EventDTO;
import com.sporty.model.ResultRequestDTO;
import com.sporty.service.EventResultService;
import com.sporty.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/v1/event")
public class EventController {

    @Autowired
    EventService eventService;
    @Autowired
    EventResultService eventResultService;

    @GetMapping
    public List<EventDTO> getEvents(@RequestParam(required = false) String sessionType,
                                    @RequestParam(required = false) Integer year,
                                    @RequestParam(required = false) String country,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "50") int size){

        return eventService.getEvent(sessionType, year,country);
    }

    @PostMapping("/result")
    public List<BetDTO> settleResultForEvent(@RequestBody ResultRequestDTO request){
        return eventResultService.updateEventResult(request);
    }

}
