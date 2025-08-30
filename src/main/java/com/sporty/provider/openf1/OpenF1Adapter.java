package com.sporty.provider.openf1;


import com.sporty.model.DriverDTO;
import com.sporty.model.EventDTO;
import com.sporty.model.EventFilterDTO;
import com.sporty.provider.*;
import org.springframework.stereotype.Component;

import java.util.*;


//@Component("OPENF1")
public class OpenF1Adapter implements F1Provider {

    @Override
    public String code() {
        return "OPENF1";
    }

    @Override
    public List<EventDTO> listEvents(EventFilterDTO f, int page, int size) {
        /* stub */
        return List.of();
    }

    @Override
    public List<DriverDTO> listDriversForEvent(String providerEventId) {
        /* stub */
        return List.of();
    }
}