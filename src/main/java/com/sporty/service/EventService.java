package com.sporty.service;

import com.sporty.client.F1Client;
import com.sporty.model.DriverDTO;
import com.sporty.model.DriverEventDTO;
import com.sporty.model.EventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service is responsible for retrieving for events and drivers events
 */
@Service
public class EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    @Autowired
    private F1Client client;
    @Autowired
    OddsService oddsService;

    public List<EventDTO> getEvent(String sessionType, int year, String country ){
        logger.info("Getting the events for request, Year :{} , sessionType :{}, country: {}:",year, sessionType, country);
        return client.getEvents(sessionType, year, country);
    }


    public DriverEventDTO getDriversForSession(String sessionKey){
        logger.info("Getting the drivers for the sessionKey : {}",sessionKey);
        DriverEventDTO driverEventDTO = client.getDriversForSession(sessionKey);
        logger.info("Getting/Generating the odd for players");
        if(driverEventDTO !=null){
            for(DriverDTO driver : driverEventDTO.getDrivers()){
                driver.setOdd(oddsService.getOddsForDriver(sessionKey,driver.getProviderDriverId()));
            }
        }
        return driverEventDTO;
    }
}
