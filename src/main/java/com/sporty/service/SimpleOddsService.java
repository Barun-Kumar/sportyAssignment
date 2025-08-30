// com/sporty/service/SimpleOddsService.java
package com.sporty.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class SimpleOddsService implements OddsService {

    Logger logger = LoggerFactory.getLogger(SimpleOddsService.class);

    private static final int[] ALLOWED = {2, 3, 4};

    @Override
    public int getOddsForDriver(String sessionKey, Integer providerDriverId) {
        return pickStable(sessionKey,providerDriverId);
    }

  /*  private int pickRandom() {
        return ALLOWED[ThreadLocalRandom.current().nextInt(ALLOWED.length)];
    }*/

    // --- Optional: stable odds for a (session, driver) pair. Uncomment if you prefer stability. ---
     private int pickStable(String sessionKey, Integer providerDriverId) {
         int idx = Math.floorMod((sessionKey + ":" + providerDriverId).hashCode(), ALLOWED.length);
         return ALLOWED[idx];
     }
}
