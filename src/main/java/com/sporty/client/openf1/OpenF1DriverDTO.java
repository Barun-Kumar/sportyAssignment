package com.sporty.client.openf1;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class OpenF1DriverDTO {
    // identifiers / keys
    private Integer driver_number;   // unique number on car (string in UI, numeric in payload)
    private Integer session_key;     // which session this row belongs to
    private Integer meeting_key;     // meeting (GP weekend) key

    // names
    private String broadcast_name;   // e.g., "M VERSTAPPEN"
    private String full_name;        // e.g., "Max VERSTAPPEN"
    private String first_name;       // e.g., "Max"
    private String last_name;        // e.g., "Verstappen"
    private String name_acronym;     // e.g., "VER"

    // team / nationality / visuals
    private String team_name;        // e.g., "Red Bull Racing"
    private String team_colour;      // e.g., "3671C6"
    private String country_code;     // e.g., "NED"
    private String headshot_url;     // image URL
}
