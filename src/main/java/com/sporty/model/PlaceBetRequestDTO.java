package com.sporty.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor
public class PlaceBetRequestDTO {
    private String userId;
    private String sessionKey;
    public String providerDriverId;
    public BigDecimal stake;
    public BigDecimal odds;
}