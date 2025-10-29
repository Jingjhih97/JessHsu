package com.works.JessHsu.security;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * 管理後台用的 Bearer token。
 * 記憶體版：重啟伺服器就清空。
 */
@Component
public class AdminSessionStore {

    /** token -> session record */
    private final Map<String, SessionRecord> sessions = new ConcurrentHashMap<>();

    /** token 有效多久 (秒)；例如 2 小時 = 7200 秒 */
    private static final long TTL_SECONDS = 7200L;

    public static class SessionRecord {
        public Long adminId;
        public Instant expiresAt;
    }

    /**
     * 建立一個新的 Bearer token，並存起來。
     * @return 產生的 token 字串
     */
    public String issueToken(Long adminId) {
        String token = UUID.randomUUID().toString().replace("-", "");

        SessionRecord r = new SessionRecord();
        r.adminId = adminId;
        r.expiresAt = Instant.now().plusSeconds(TTL_SECONDS);

        sessions.put(token, r);

        return token;
    }

    /**
     * 用 token 換 adminId。
     * - token 不存在 → 回 null
     * - token 過期 → 刪掉並回 null
     */
    public Long resolve(String token) {
        if (token == null || token.isBlank()) return null;

        SessionRecord r = sessions.get(token);
        if (r == null) return null;

        if (Instant.now().isAfter(r.expiresAt)) {
            // 過期就清掉，避免記憶體一直長
            sessions.remove(token);
            return null;
        }

        return r.adminId;
    }

    /**
     * 主動讓某個 token 失效 (登出)
     */
    public void invalidate(String token) {
        if (token != null && !token.isBlank()) {
            sessions.remove(token);
        }
    }

    /**
     * 回傳目前 TTL（秒），看你要不要用在回應給前端。
     */
    public long getTtlSeconds() {
        return TTL_SECONDS;
    }
}