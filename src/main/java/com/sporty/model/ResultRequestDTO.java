package com.sporty.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class ResultRequestDTO {
    private String sessionKey;
    private String winnerDriverId;

}
