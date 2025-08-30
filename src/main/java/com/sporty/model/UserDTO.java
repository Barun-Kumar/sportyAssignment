package com.sporty.model;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;


@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class UserDTO {

    // as of now this class is not treated as Entity, Later we can save this in DB directly
    @Id
    public String id;
    @Column(name = "userId", length = 64)
    private String userId;

    @Column(name = "name", length = 64)
    private String name;

    @Column(name = "balance_cents", nullable = false)
    private BigDecimal balance;
    // Default currency is EUR
    @Column(name = "currency", length = 3, nullable = false)
    private String currency = "EUR";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}