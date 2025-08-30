package com.sporty.repository;

import com.sporty.model.BetDTO;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class InMemoryBetRepository {

    private final Map<String, BetDTO> store = new ConcurrentHashMap<>();

    //TODO later we can enhance the key to be sessionKey-userId-betId
    public BetDTO save(BetDTO bet) {
        store.put(bet.getBetId(), bet);
        return bet;
    }

    public Optional<BetDTO> findById(String betId) {
        return Optional.ofNullable(store.get(betId));
    }

    public List<BetDTO> findByUserId(String emailId) {
        List<BetDTO> userBets = new ArrayList<>();
        for (BetDTO bet : store.values()) {
            if (bet.getUserId().equalsIgnoreCase(emailId)) {
                userBets.add(bet);
            }
        }
        return userBets;
    }

    public List<BetDTO> findBySessionKey(String sessionKey) {
        return store.values().stream().filter(b -> sessionKey.equals(b.getSessionKey())).collect(Collectors.toList());
    }
}
