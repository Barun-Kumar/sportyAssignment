package com.sporty.model;// Event + Drivers DTO
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter @Setter @NoArgsConstructor
public class DriverEventDTO {
    private String eventId;                // session_key
    private String eventName;              // e.g. "Belgium GP - Sprint"
    private String location;               // Spa-Francorchamps
    private String country;                // BEL
    private String dateStart;              // 2023-07-29T15:05:00+00:00
    private String dateEnd;                // 2023-07-29T15:35:00+00:00
    private List<DriverDTO> drivers;       // drivers participating
}
