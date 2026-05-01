# 🛡️ SkyBanking: Security Architecture & Hardening

This document outlines the security framework and enhancements implemented to ensure **SkyBanking** meets enterprise financial standards.

---

## 🔒 Credentials & Secrets Management
> [!IMPORTANT]
> Zero hardcoded secrets policy.

- **Implementation**: Leverages `io.github.cdimascio.dotenv.java`.
- **Enforcement**: All API keys, DB credentials, and SMTP secrets are injected at runtime via environment variables.
- **Safety**: `.env` is globally ignored to prevent accidental leaks.

---

## 🔑 Identity & Access Management (IAM)
### Modern Password Hashing
- **Algorithm**: `BCrypt` with adaptive salting.
- **Migration Strategy**: Implemented a **Seamless Dual-Check Migration**. Old `SHA-256` hashes are automatically upgraded to `BCrypt` upon first successful login, ensuring no user friction while upgrading legacy security.

### Brute-Force Protection
- **Rate Limiting**: `RateLimitFilter.java` monitors `/login` and `/verifyotp`.
- **Action**: Temporary IP blocking after 10 failed attempts to thwart automated attacks.

---

## 💸 Financial Integrity (ACID)
To prevent race conditions and "double-spending", the system employs:
1. **Row-Level Locking**: `SELECT ... FOR UPDATE` ensuring atomic ledger updates.
2. **Idempotency Keys**: UUID-based tracking to prevent duplicate transactions during network retries.
3. **Deadlock Avoidance**: Consistent resource ordering (smaller Account ID first) during transfers.

---

## 💳 Payment Security
- **Stripe Integration**: Solely relies on **Cryptographic Webhook Signatures** (`STRIPE_WEBHOOK_SECRET`).
- **QR Security**: Signed with `HMAC-SHA256` and strictly time-bound (15-minute expiry).

---

## 🌐 Web Layer Defense
- **Session Security**: `HttpOnly` and `Secure` flags enforced on all cookies.
- **CSRF Defense**: `CsrfFilter.java` enforces secure nonces for all state-changing requests.
- **Security Headers**: Standard headers (X-Frame-Options, X-XSS-Protection) are injected globally via `SecurityHeadersFilter.java`.

---

## 📝 Audit & Compliance
Every critical action is logged into dedicated PostgreSQL tables for forensic analysis:
- `security_logs`: Authentication and access events.
- `transaction_logs`: Complete financial audit trail.
- `admin_logs`: Administrative overrides and configuration changes.

---

<div align="center">
<b>SkyBanking Security Team</b> | <i>Built for Trust</i>
</div>
