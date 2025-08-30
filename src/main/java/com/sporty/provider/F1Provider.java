package com.sporty.provider;


import com.sporty.model.DriverDTO;
import com.sporty.model.EventDTO;
import com.sporty.model.EventFilterDTO;

import java.util.*;


public interface F1Provider {
    List<EventDTO> listEvents(EventFilterDTO filter, int page, int size);
    List<DriverDTO> listDriversForEvent(String providerEventId);
    String code();
}