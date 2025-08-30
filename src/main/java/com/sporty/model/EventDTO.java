package com.sporty.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EventDTO {
    private Integer sessionKey;
    private Integer meetingKey;
    private String  sessionName;     // e.g., "Sprint", "Practice 1", "Race"
    private String  sessionType;     // e.g., "Race", "Practice", "Qualifying"
    private Integer year;

    private String  countryName;     // "Belgium"
    private String  countryCode;     // "BEL"
    private Integer countryKey;

    private Integer circuitKey;
    private String  circuitShortName; // "Spa-Francorchamps"
    private String  location;         // "Spa-Francorchamps"

    private String  dateStart;        // ISO 8601 UTC
    private String  dateEnd;          // ISO 8601 UTC
    private String  gmtOffset;        // "02:00:00"


}
