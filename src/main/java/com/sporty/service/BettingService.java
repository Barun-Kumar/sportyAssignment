package com.sporty.service;

import com.sporty.client.F1Client;
import com.sporty.model.*;
import com.sporty.repository.InMemoryBetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class BettingService {
    private static final Logger logger = LoggerFactory.getLogger(BettingService.class);
    private static final BigDecimal MIN_STAKE = new BigDecimal("1.00");

    @Autowired
    private F1Client openF1Client;
    @Autowired
    private InMemoryBetRepository betRepository;
    @Autowired
    UserService userService;


    public List<BetDTO> getUserBets(String emailId){
        return betRepository.findByUserId(emailId);
    }

    public List<BetDTO> getAllBetsForSession(String sessionKey){
        List<BetDTO> betsBySessionList = new ArrayList<>();
        if(sessionKey == null || sessionKey.isEmpty()){
            logger.info("session key is null or empty, returning empty bet list");
            return betsBySessionList;
        }
        return betRepository.findBySessionKey(sessionKey);
    }
    /**
     * Place a bet on a driver to win (or any market you later extend).
     * Odds are decimal (e.g., 3.50). Validates that the driver belongs to the session.
     */
    public BetDTO placeBetOnDriver(PlaceBetRequestDTO placeBetRequest) throws Exception {
        //Validate inputs
        validateInputs(placeBetRequest);
        //Validate user balance if sufficient to place the bets
        BigDecimal bal = userService.getUserBalance(placeBetRequest.getUserId());

        if(placeBetRequest.getStake().compareTo(bal) >0){
            throw new IllegalStateException("You can not place bet, as your balance is low, current balance is :"+bal);
        }

        // Ensure the driver is in the session
        List<DriverDTO> drivers =  openF1Client.getDrivers(placeBetRequest.getSessionKey());
        if(drivers == null || drivers.isEmpty()){
            //Later we can add custom exception
            throw new Exception("No Driver found for the session, can not place bet");
        }
        boolean exists = drivers.stream()
                .anyMatch(d -> Objects.equals(d.getProviderDriverId(), d.getProviderDriverId()));
        if (!exists) {
            throw new IllegalArgumentException("Driver " + placeBetRequest.getProviderDriverId() + " is not part of session " + placeBetRequest.getSessionKey());
        }

        BetDTO bet = new BetDTO();
        bet.setSessionKey(placeBetRequest.getSessionKey());
        bet.setProviderDriverId(placeBetRequest.getProviderDriverId());
        bet.setStake(safeScale(placeBetRequest.getStake()));
        bet.setOdds(safeScale(placeBetRequest.getOdds()));
        bet.setUserId(placeBetRequest.getUserId());

        BigDecimal potential = bet.getStake().multiply(bet.getOdds());
        bet.setPotentialPayout(safeScale(potential));
        userService.updateUserBalance(bet);
        return betRepository.save(bet);
    }

    public BetDTO getBet(String betId) {
        return betRepository.findById(betId).orElseThrow(() -> new IllegalArgumentException("Bet not found: " + betId));
    }

    public List<BetDTO> listBetsForSession(String sessionKey) {
        return betRepository.findBySessionKey(sessionKey);
    }

    /**
     * Settle a bet.
     *
     * @param betId  bet id
     * @param result WIN/LOSE/PUSH
     */
    public BetDTO settleBet(String betId, BetResultEnum result) {
        BetDTO bet = getBet(betId);
        if (bet.getStatus() != BetStatusEnum.PENDING) {
            logger.info("Bet already settled/void: " + betId);
            return null;
        }

        bet.setResult(result);
        bet.setSettledAt(Instant.now());
        bet.setStatus(BetStatusEnum.SETTLED);

        BigDecimal payout;
        switch (result) {
            case WIN:
                payout = bet.getStake().multiply(bet.getOdds());
                break;
            case PUSH:
                payout = bet.getStake(); // refund
                break;
            case LOSE:
            default:
                payout = BigDecimal.ZERO;
        }
        bet.setPayout(safeScale(payout));
        Optional<UserDTO> userOpt = userService.getUser(bet.getUserId());
        if(userOpt.isPresent()){
            UserDTO user = userOpt.get();
            user.setBalance(user.getBalance().add(payout));
            logger.info("User : {} win :{}",user.getUserId(),payout);
        }
        return betRepository.save(bet);
    }

    /**
     * Void bet (e.g., race cancelled, driver DNS and market rules refund).
     */
    public BetDTO voidBet(String betId) {
        BetDTO bet = getBet(betId);
        bet.setStatus(BetStatusEnum.VOID);
        bet.setResult(null);
        bet.setPayout(safeScale(bet.getStake())); // refund by default
        bet.setSettledAt(Instant.now());
        return betRepository.save(bet);
    }


    private static void validateInputs(PlaceBetRequestDTO placeBetRequest) {
        if (placeBetRequest.getSessionKey() == null || placeBetRequest.getSessionKey().isBlank()) {
            throw new IllegalArgumentException("sessionKey is required");
        }
        if (placeBetRequest.getProviderDriverId() == null || placeBetRequest.getProviderDriverId().isBlank()) {
            throw new IllegalArgumentException("providerDriverId is required");
        }
        if (placeBetRequest.getStake() == null || placeBetRequest.getStake().compareTo(MIN_STAKE) < 0) {
            throw new IllegalArgumentException("stake must be >= " + MIN_STAKE);
        }
    }

    private static BigDecimal safeScale(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}
