package com.sporty.client;

import com.sporty.model.DriverDTO;
import com.sporty.model.DriverEventDTO;
import com.sporty.model.EventDTO;

import java.util.List;

/**
 * Abstraction for fetching Formula 1 sessions (events) and drivers
 * from an external data provider.
 * <p>
 * <strong>Contract</strong>
 * <ul>
 *   <li>All methods return non-{@code null} values (use empty lists when no data is found).</li>
 *   <li>Required arguments are validated; {@link IllegalArgumentException} is thrown on invalid input.</li>
 *   <li>I/O/HTTP failures may be propagated as unchecked {@link RuntimeException RuntimeExceptions} by implementations.</li>
 *   <li>Implementations should be thread-safe.</li>
 * </ul>
 */
public interface F1Client {

    /**
     * Retrieves sessions/events matching optional filters.
     *
     * @param sessionName  optional filter for the session name (e.g., {@code "Race"}, {@code "Sprint"},
     *                     {@code "Qualifying"}). Pass {@code null} or blank to ignore.
     * @param year         optional season year. Pass {@code <= 0} to ignore.
     * @param countryName  optional country name filter (e.g., {@code "Belgium"}). Pass {@code null} or blank to ignore.
     * @return a non-{@code null} list of {@link EventDTO} instances; may be empty if no sessions match.
     * @throws RuntimeException if the underlying data source cannot be reached or returns an error.
     */
    List<EventDTO> getEvents(String sessionName, int year, String countryName);

    /**
     * Retrieves the drivers participating in the session identified by {@code sessionKey}.
     *
     * @param sessionKey provider-specific session identifier; must be non-blank.
     * @return a non-{@code null} list of {@link DriverDTO} instances; may be empty if no drivers are available.
     * @throws IllegalArgumentException if {@code sessionKey} is {@code null} or blank.
     * @throws RuntimeException if the underlying data source cannot be reached or returns an error.
     */
    List<DriverDTO> getDrivers(String sessionKey);

    /**
     * Fetches drivers for the session along with minimal session metadata.
     * If only the driver list is needed, prefer {@link #getDrivers(String)}.
     *
     * @param sessionKey provider-specific session identifier; must be non-blank.
     * @return a non-{@code null} {@link DriverEventDTO} aggregating session info and its drivers.
     * @throws IllegalArgumentException if {@code sessionKey} is {@code null} or blank.
     * @throws RuntimeException if the underlying data source cannot be reached or returns an error.
     */
    DriverEventDTO getDriversForSession(String sessionKey);
}
