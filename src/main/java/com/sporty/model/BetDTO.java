package com.sporty.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter @Setter
public class BetDTO {

    private final String betId;            // UUID
    private String sessionKey;             // OpenF1 session key (event id)
    private String providerDriverId;       // OpenF1 driver_number (as string)
    private BigDecimal stake;              // currency units
    private BigDecimal odds;               // decimal odds, e.g., 3.50
    private BetStatusEnum status;              // PENDING, SETTLED, VOID
    private BetResultEnum result;              // WIN, LOSE, PUSH (null until settled)
    private BigDecimal payout;             // actual payout on settlement
    private BigDecimal potentialPayout;    // stake * odds (for display)
    private Instant placedAt;
    private Instant settledAt;
    private String userId;

    public BetDTO() {
        this.betId = UUID.randomUUID().toString();
        this.status = BetStatusEnum.PENDING;
        this.placedAt = Instant.now();
    }

    public String getBetId() { return betId; }
    public String getSessionKey() { return sessionKey; }
    public void setSessionKey(String sessionKey) { this.sessionKey = sessionKey; }
    public String getProviderDriverId() { return providerDriverId; }
    public void setProviderDriverId(String providerDriverId) { this.providerDriverId = providerDriverId; }
    public BigDecimal getStake() { return stake; }
    public void setStake(BigDecimal stake) { this.stake = stake; }
    public BigDecimal getOdds() { return odds; }
    public void setOdds(BigDecimal odds) { this.odds = odds; }
    public BetStatusEnum getStatus() { return status; }
    public void setStatus(BetStatusEnum status) { this.status = status; }
    public BetResultEnum getResult() { return result; }
    public void setResult(BetResultEnum result) { this.result = result; }
    public BigDecimal getPayout() { return payout; }
    public void setPayout(BigDecimal payout) { this.payout = payout; }
    public BigDecimal getPotentialPayout() { return potentialPayout; }
    public void setPotentialPayout(BigDecimal potentialPayout) { this.potentialPayout = potentialPayout; }
    public Instant getPlacedAt() { return placedAt; }
    public void setPlacedAt(Instant placedAt) { this.placedAt = placedAt; }
    public Instant getSettledAt() { return settledAt; }
    public void setSettledAt(Instant settledAt) { this.settledAt = settledAt; }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BetDTO)) return false;
        BetDTO bet = (BetDTO) o;
        return Objects.equals(betId, bet.betId);
    }
    @Override public int hashCode() { return Objects.hash(betId); }
}
