package com.sporty.controller;

import com.sporty.model.DriverEventDTO;
import com.sporty.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;


@RestController
@RequestMapping("/v1")
public class DriverController {

    @Autowired
    private EventService eventService;

    /**
     * UI uses this to render the event header + full driver list for the session.
     * Example: GET /v1/events/9140/drivers
     */
    @GetMapping("/events/{sessionKey}/drivers")
    public ResponseEntity<DriverEventDTO> getDriversForEvent(@PathVariable String sessionKey) {
        DriverEventDTO driverEventDTO = eventService.getDriversForSession(sessionKey);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(2)))
                .body(driverEventDTO);
    }

}
