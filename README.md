# sportyAssignment

# Formula 1 Betting Service — High-Level Design & API Spec (v0.1)

> Goal: A simple-but-solid backend for single-winner bets on F1 sessions ("events"), with user gift balance, deterministic odds (2/3/4), event settlement, and a pluggable data-provider layer.

---

## 1) Scope & Assumptions

* Users are pre-registered and each starts with **€100** gift balance.
* Only **single bets** on **one driver to win** a specific F1 event (race/qualy/etc.).
* Odds allowed: **2**, **3**, **4** (treated as decimal odds multiplier).
* We must support multiple provider APIs in the future; initial provider: **OpenF1** ("Sessions").
* We will expose a minimal REST API for: list events (with driver market), place bet, settle event, query balances/bets.
* Currency: EUR. We will use **decimal** arithmetic.

---

## 2) Architecture (Hexagonal / Clean)

* **API Layer** (Spring controllers / handlers)
* **Application Services** (use-cases):

  * `ListEventsService`
  * `PlaceBetService`
  * `SettleEventService`
  * `GetUserBalanceService`
  * `ListBetsService`
* **Domain**:

  * Entities: `User`, `Bet`, `EventRef`, `Odds`
  * Value Objects: `Money(EUR)`, `Odds(2|3|4)`, `BetStatus(PENDING|WON|LOST)`
* **Ports** (interfaces):

  * `F1ProviderPort` (get sessions, drivers per session)
  * `RandomOddsPort` (2/3/4 generator, deterministic per event+driver)
  * `BetRepository`, `UserRepository`, `LedgerRepository`, `OutcomeRepository`
* **Adapters**:

  * Provider: `OpenF1Adapter` (maps provider fields → domain)
  * Persistence: JPA/SQL (Postgres/MySQL/SQL Server – ANSI SQL where possible)
  * Odds: deterministic PRNG seeded from `(provider, eventKey, driverKey)`
* **Storage**: Relational DB + optional Redis cache for event/session lists.

---

## 3) Domain IDs & Provider-Agnostic Model

* **EventRef**: `{provider:"OPENF1", providerEventId:"<session_key>"}`
* **DriverRef**: `{provider:"OPENF1", providerDriverId:"<driver_number or driver_id>"}`
* **Internal IDs**:

  * `event_id` (UUID) ↔ unique `(provider, provider_event_id)`
  * `driver_id` (UUID) ↔ unique `(provider, provider_driver_id)`
* **Bet** keeps snapshots: `odds_at_placement`, `driver_name_at_placement`, `event_label_at_placement` to avoid inconsistent future changes.

---

## 4) API Design

### 4.1 List Events + Driver Market

**GET** `/v1/events`
**Query params** (all optional):

* `sessionType` (e.g., RACE, QUALIFYING, PRACTICE)
* `year` (e.g., 2024)
* `country` (e.g., ITALY)
* `page`, `size`

**Response (200)**

```json
{
  "events": [
    {
      "eventId": "9d479f8e-...",            // internal UUID
      "label": "2024 Italian GP — Race",     
      "provider": "OPENF1",
      "providerEventId": "9159",           // example session_key
      "sessionType": "RACE",
      "country": "ITALY",
      "scheduledAt": "2024-09-01T13:00:00Z",
      "driverMarket": [
        {
          "driverId": "8be3370a-...",      // internal UUID
          "driverName": "Max Verstappen",
          "providerDriverId": "33",
          "odds": 3                          // deterministic 2|3|4 for this event+driver
        }
      ]
    }
  ],
  "page": {"number": 0, "size": 50, "total": 120}
}
```

**Behaviour**

* Events are fetched from `F1ProviderPort` and mapped to domain.
* For **each driver** in the session, we compute **deterministic odds** via `RandomOddsPort` (see §6) and include it in `driverMarket`.
* Caching: 5–15 min cache for event lists; driver roster cached per session. On cache miss we hydrate from provider.

---

### 4.2 Place a Bet

**POST** `/v1/bets`

```json
{
  "userId": "u-123",
  "eventId": "9d479f8e-...",
  "driverId": "8be3370a-...",
  "stake": "25.00",
  "clientOdds": 3
}
```

**Rules**

* Validate `clientOdds ∈ {2,3,4}` and must equal our computed odds for `(eventId, driverId)` at the moment of placement.
* User must have sufficient balance.

**Side effects (transactional)**

1. Insert `Bet(PENDING)` with `odds_at_placement`.
2. Deduct `stake` from `User.balance`.
3. Append ledger entry: `DEBIT_BET_PLACED`.

**Response (201)**

```json
{
  "betId": "b-789",
  "status": "PENDING",
  "stake": "25.00",
  "odds": 3,
  "potentialPayout": "75.00"
}
```

> *Note:* We treat **decimal odds**; net profit if won = `stake * odds - stake`. The system credits **gross payout** on win; stake was already deducted at placement.

---

### 4.3 Get User Balance

**GET** `/v1/users/{userId}/balance`

```json
{ "userId": "u-123", "balance": "68.50" }
```

### 4.4 List Bets (by user)

**GET** `/v1/bets?userId=u-123&status=PENDING|WON|LOST`

```json
{
  "bets": [
    {
      "betId": "b-789",
      "event": { "label": "2024 Italian GP — Race" },
      "driver": { "name": "Max Verstappen" },
      "stake": "25.00",
      "odds": 3,
      "status": "PENDING",
      "placedAt": "2025-08-30T09:21:00Z"
    }
  ]
}
```

### 4.5 Settle Event Outcome

**POST** `/v1/events/{eventId}/settle`

```json
{ "winnerDriverId": "8be3370a-..." }
```

**Behaviour (idempotent, transactional)**

1. Write/Upsert `Outcome(eventId, winnerDriverId)`.
2. For all `PENDING` bets of the event:

   * If bet.driverId == winner → `status=WON`, credit `stake*odds` to user balance, ledger `CREDIT_PAYOUT`.
   * Else → `status=LOST`.

**Response**

```json
{ "eventId": "9d479f8e-...", "settled": {"won": 42, "lost": 213} }
```

---

## 5) Persistence Model (ANSI SQL)

```sql
CREATE TABLE users (
  user_id        VARCHAR(64) PRIMARY KEY,
  balance_cents  BIGINT NOT NULL DEFAULT 10000,   -- €100.00
  currency       CHAR(3) NOT NULL DEFAULT 'EUR',
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE providers (
  provider_id SERIAL PRIMARY KEY,
  code        VARCHAR(32) UNIQUE NOT NULL -- e.g., 'OPENF1'
);

CREATE TABLE events (
  event_id           UUID PRIMARY KEY,
  provider           VARCHAR(32) NOT NULL,
  provider_event_id  VARCHAR(64) NOT NULL,
  label              VARCHAR(255) NOT NULL,
  session_type       VARCHAR(32) NOT NULL,
  country            VARCHAR(64),
  scheduled_at       TIMESTAMP,
  UNIQUE(provider, provider_event_id)
);

CREATE TABLE drivers (
  driver_id           UUID PRIMARY KEY,
  provider            VARCHAR(32) NOT NULL,
  provider_driver_id  VARCHAR(64) NOT NULL,
  name                VARCHAR(128) NOT NULL,
  UNIQUE(provider, provider_driver_id)
);

CREATE TABLE bets (
  bet_id        UUID PRIMARY KEY,
  user_id       VARCHAR(64) NOT NULL REFERENCES users(user_id),
  event_id      UUID NOT NULL REFERENCES events(event_id),
  driver_id     UUID NOT NULL REFERENCES drivers(driver_id),
  stake_cents   BIGINT NOT NULL,
  odds          SMALLINT NOT NULL CHECK (odds IN (2,3,4)),
  status        VARCHAR(16) NOT NULL CHECK (status IN ('PENDING','WON','LOST')),
  placed_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  settled_at    TIMESTAMP NULL
);

CREATE INDEX idx_bets_event_pending ON bets(event_id) WHERE status='PENDING';
CREATE INDEX idx_bets_user ON bets(user_id);

CREATE TABLE outcomes (
  event_id       UUID PRIMARY KEY REFERENCES events(event_id),
  winner_driver  UUID NOT NULL REFERENCES drivers(driver_id),
  settled_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_ledger (
  entry_id     BIGSERIAL PRIMARY KEY,
  user_id      VARCHAR(64) NOT NULL,
  amount_cents BIGINT NOT NULL,
  type         VARCHAR(32) NOT NULL, -- DEBIT_BET_PLACED | CREDIT_PAYOUT | ADJUSTMENT
  ref_bet_id   UUID NULL,
  created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

> Money handling: store in **cents** (BIGINT). Convert to/from string at API edges.

---

## 6) Odds Engine (Deterministic 2/3/4)

We want stable odds per `(event, driver)` to avoid UI flicker between calls while satisfying the "random 2/3/4" constraint.

**Algorithm:**

```
seed = SHA-256( provider + ":" + providerEventId + ":" + providerDriverId )
num  = (first_4_bytes_of_seed as unsigned) % 3
odds = [2,3,4][num]
```

* Deterministic = same odds for the same event+driver.
* Still “random” across pairs.
* If you prefer true-random each call, switch to `SecureRandom` at request time and **require** client to echo `clientOdds` in POST to avoid mismatch.

---

## 7) Provider Port & Adapter (OpenF1 example)

**Port**

```java
interface F1ProviderPort {
  List<EventDTO> listEvents(EventFilter f, PageReq p);
  List<DriverDTO> listDriversForEvent(EventDTO e);
}
```

**Adapter responsibilities**

* Map provider fields to domain (`session_key` → `providerEventId`, driver number/id → `providerDriverId`).
* Normalize session types (e.g., RACE/QUALIFYING/PRACTICE).
* Cache defensive: 5–15 minutes for list endpoints.

**EventFilter**

```
{ sessionType?:RACE|QUALIFYING|PRACTICE, year?:int, country?:string }
```

---

## 8) Use‑Case Flows (happy paths)

### 8.1 List Events

1. API → `ListEventsService`
2. Service → `F1ProviderPort.listEvents(filter)`
3. For each event → `listDriversForEvent`
4. Compute odds per driver (deterministic) and return.

### 8.2 Place Bet

1. Validate input + recompute odds for `(event,driver)` → must equal `clientOdds` (anti-race condition contract).
2. TX: create bet (PENDING) + deduct stake from user + ledger.

### 8.3 Settle Event

1. Upsert `Outcome(eventId)` with winner.
2. In a single TX:

   * Query all PENDING bets for `eventId` with `FOR UPDATE SKIP LOCKED` batching if large.
   * For each: set WON/LOST; credit payout for wins; ledger; set `settled_at`.

**Idempotency:** If outcome exists, re-run settlement should be a no-op (no double crediting). Optionally keep `bets.status != PENDING` guard.

---

## 9) Error Handling & Status Codes

* 400: invalid odds, stake <= 0, bad filters
* 402: insufficient balance
* 404: event/driver not found
* 409: settlement already performed with different winner
* 422: provider unavailable (retry later)

---

## 10) Non‑Functional

* **Perf**: list events < 300–500ms (provider + cache); place bet/settle < 50ms (DB-bound).
* **Scale**: Bets table indexed by (event, status). Settlement batches in pages of 1–5k with `SKIP LOCKED`.
* **Audit**: Immutable `user_ledger` for all balance changes.
* **Observability**: counters for bets placed, wins, losses; histograms for latency; gauge of outstanding PENDING per event.
* **Security**: Auth via API key/JWT (userId asserted by upstream). Input validation, rate limiting.

---

## 11) Minimal Spring Boot Skeleton (interfaces)

```java
@RestController
class EventsController {
  @GetMapping("/v1/events") ListEventsResponse list(...)
  @PostMapping("/v1/events/{eventId}/settle") SettleResponse settle(...)
}

@RestController
class BetsController {
  @PostMapping("/v1/bets") PlaceBetResponse place(...)
  @GetMapping("/v1/bets") ListBetsResponse list(...)
}

@RestController
class UsersController {
  @GetMapping("/v1/users/{id}/balance") BalanceResponse get(...)
}
```

Service Interfaces:

```java
interface PlaceBetService { PlaceBetResponse place(PlaceBetCommand cmd); }
interface ListEventsService { ListEventsResponse list(Filter f, Page p); }
interface SettleEventService { SettleResponse settle(UUID eventId, UUID winnerDriverId); }
```

---

## 12) Test Plan

* **Unit**: odds function (distribution, determinism), money math, domain transitions.
* **Integration**: happy-path bet and settlement with H2/Postgres. Provider adapter with WireMock fixtures.
* **Load**: 1k bets/sec; settlement batch of 100k PENDING in < 60s.
* **Contract**: snapshot tests for JSON payloads.

---

## 13) Future Work

* Multiple providers (add `Jolpica-F1/Ergast-compatible` adapter).
* Caching layer with Redis.
* Idempotency-Key on `/v1/bets` to avoid duplicate submissions.
* User bet limits, per-event limits, and responsible gaming checks.
* Multi-currency and wallet service abstraction.
* Pricing service (real odds), risk controls.

---

**Done.** This document is implementation-ready: create entities, repositories, service flows, and the provider adapter. Ping to expand any section into code.
