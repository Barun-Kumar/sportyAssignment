package com.sporty.service;

import com.sporty.model.BetDTO;
import com.sporty.model.BetResultEnum;
import com.sporty.model.ResultRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EventResultService {

    Logger logger = LoggerFactory.getLogger(EventResultService.class);

    @Autowired
    private BettingService bettingService;

    public List<BetDTO> updateEventResult(ResultRequestDTO requestDTO){
        List<BetDTO> sessionBets = bettingService.getAllBetsForSession(requestDTO.getSessionKey());
        if(sessionBets == null || sessionBets.isEmpty()){
            logger.info("No session bet found to settle for sessionKey : {}",requestDTO.getSessionKey());
        }
        List<BetDTO> winnerBets = new ArrayList<>();
        for(BetDTO bet: sessionBets){
            if(bet.getProviderDriverId().equalsIgnoreCase(requestDTO.getWinnerDriverId())){
                bettingService.settleBet(bet.getBetId(), BetResultEnum.WIN);
                winnerBets.add(bet);
            }
        }
        return winnerBets;
    }
}
