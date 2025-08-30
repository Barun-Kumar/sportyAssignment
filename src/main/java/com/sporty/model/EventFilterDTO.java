package com.sporty.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class EventFilterDTO {
    private String sessionType; // RACE | QUALIFYING | PRACTICE
    private Integer year;
    private String country;

    public EventFilterDTO(String sessionType, Integer year, String country) {
        this.sessionType = sessionType;
        this.year = year;
        this.country = country;
    }
}