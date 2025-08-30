package com.sporty.controller;

import com.sporty.model.BetDTO;
import com.sporty.model.BetResultEnum;
import com.sporty.model.PlaceBetRequestDTO;
import com.sporty.service.BettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/bets")
public class BettingController {

    @Autowired
    private final BettingService bettingService;

    public BettingController(BettingService bettingService) {
        this.bettingService = bettingService;
    }

    @PostMapping("/driver")
    public BetDTO placeBet(@RequestBody PlaceBetRequestDTO placeBetRequest) throws Exception {
        return bettingService.placeBetOnDriver(placeBetRequest);
    }

    @GetMapping("/{betId}")
    public BetDTO get(@PathVariable String betId) {
        return bettingService.getBet(betId);
    }

    @GetMapping
    public List<BetDTO> list(@RequestParam String sessionKey) {
        return bettingService.listBetsForSession(sessionKey);
    }

    @PostMapping("/{betId}/settle")
    public BetDTO settle(@PathVariable String betId, @RequestParam BetResultEnum result) {
        return bettingService.settleBet(betId, result);
    }

    @PostMapping("/{betId}/void")
    public BetDTO voidBet(@PathVariable String betId) {
        return bettingService.voidBet(betId);
    }
}
