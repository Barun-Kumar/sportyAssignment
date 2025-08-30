package com.sporty.client.openf1;

import com.sporty.client.F1Client;
import com.sporty.model.DriverDTO;
import com.sporty.model.EventDTO;
import com.sporty.model.DriverEventDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component("OPENF1")
public class OpenF1Client implements F1Client {
    // baseUrl configured as https://api.openf1.org/v1
    private final RestClient rest;

    public OpenF1Client(RestClient openF1RestClient) {
        this.rest = openF1RestClient;
    }

    @Override
    public List<EventDTO> getEvents(String sessionName, int year, String countryName) {
        var typeRef = new ParameterizedTypeReference<List<OpenF1SessionDTO>>() {
        };
        List<OpenF1SessionDTO> raw = rest.get().uri(uri -> uri.path("/sessions").queryParam("year", year).queryParamIfPresent("country_name", opt(countryName)).queryParamIfPresent("session_name", opt(sessionName)).build()).retrieve().body(typeRef);
        if (raw == null || raw.isEmpty()) return List.of();
        List<EventDTO> events = new ArrayList<>(raw.size());
        for (OpenF1SessionDTO s : raw) {
            EventDTO e = new EventDTO();
            e.setSessionKey(s.session_key);
            e.setMeetingKey(s.meeting_key);
            e.setSessionName(s.session_name);
            e.setSessionType(s.session_type);
            e.setYear(s.year);
            e.setCountryName(s.country_name);
            e.setCountryCode(s.country_code);
            e.setCountryKey(s.country_key);
            e.setCircuitKey(s.circuit_key);
            e.setCircuitShortName(s.circuit_short_name);
            e.setLocation(s.location);
            e.setDateStart(s.date_start);
            e.setDateEnd(s.date_end);
            e.setGmtOffset(s.gmt_offset);
            events.add(e);
        }
        return events;
    }

    /**
     * Returns an event-with-drivers bundle for a given session_key. * If you only need the drivers list, call getDrivers(sessionKey) below.
     */
    @Override
    public DriverEventDTO getDriversForSession(String sessionKey) {
        // 1) drivers of this session
        List<OpenF1DriverDTO> openDrivers = rest.get().uri(uri -> uri.path("/drivers").queryParam("session_key", sessionKey).build()).retrieve().body(new ParameterizedTypeReference<List<OpenF1DriverDTO>>() {
        });
        List<DriverDTO> driverList = new ArrayList<>();
        if (openDrivers != null) {
            for (OpenF1DriverDTO od : openDrivers) {
                DriverDTO d = new DriverDTO();
                d.setProviderDriverId(od.getDriver_number());
                d.setName(od.getFull_name());
                // you can switch to broadcast_name if you like
                driverList.add(d);
            }
        }
        // 2) (optional) hydrate event meta via /sessions?session_key=...
        OpenF1SessionDTO session = fetchSingleSession(sessionKey);
        DriverEventDTO event = new DriverEventDTO();
        event.setEventId(sessionKey);
        if (session != null) {
            event.setEventName(session.session_name); // e.g., "Sprint", "Race", "Qualifying"
            event.setLocation(session.location);
            event.setCountry(session.country_code);
            event.setDateStart(session.date_start);
            event.setDateEnd(session.date_end);
        }
        event.setDrivers(driverList);
        return event;
    }

    /**
     * Returns only the simplified drivers list for a session.
     */
    @Override
    public List<DriverDTO> getDrivers(String sessionKey) {
        List<OpenF1DriverDTO> openDrivers = rest.get().uri(uri -> uri.path("/drivers").queryParam("session_key", sessionKey).build()).retrieve().body(new ParameterizedTypeReference<List<OpenF1DriverDTO>>() {
        });
        if (openDrivers == null || openDrivers.isEmpty()) return List.of();
        List<DriverDTO> drivers = new ArrayList<>(openDrivers.size());
        for (OpenF1DriverDTO od : openDrivers) {
            DriverDTO d = new DriverDTO();
            d.setProviderDriverId(od.getDriver_number());
            d.setName(od.getFull_name());
            drivers.add(d);
        }
        return drivers;
    } // --- helpers ---

    private OpenF1SessionDTO fetchSingleSession(String sessionKey) {
        List<OpenF1SessionDTO> sessions = rest.get().uri(uri -> uri.path("/sessions").queryParam("session_key", sessionKey).build()).retrieve().body(new ParameterizedTypeReference<List<OpenF1SessionDTO>>() {
        });
        return (sessions == null || sessions.isEmpty()) ? null : sessions.get(0);
    }

    private Optional<String> opt(String v) {
        return (v == null || v.isBlank()) ? Optional.empty() : Optional.of(v);
    }
}
