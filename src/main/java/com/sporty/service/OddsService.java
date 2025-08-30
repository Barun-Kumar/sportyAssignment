// com/sporty/service/OddsService.java
package com.sporty.service;

public interface OddsService {
    /** Returns 2, 3, or 4 (default implementation may be random). */
    int getOddsForDriver(String sessionKey, Integer providerDriverId);
}
