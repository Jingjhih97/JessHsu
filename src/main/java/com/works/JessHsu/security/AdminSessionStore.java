package com.works.JessHsu.security;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * 管理後台用的 Bearer token。
 * token -> username, 並帶有過期時間。
 */
@Component
public class AdminSessionStore {

    // token -> session record
    private final Map<String, SessionRecord> sessions = new ConcurrentHashMap<>();

    // token 有效時間（秒），例如 2 小時
    private static final long TTL_SECONDS = 2 * 60 * 60;

    public static class SessionRecord {
        public String username;
        public Instant expiresAt;
    }

    /** 登入成功後發 token */
    public String issueToken(String username) {
        String token = UUID.randomUUID().toString().replace("-", "");

        SessionRecord r = new SessionRecord();
        r.username = username;
        r.expiresAt = Instant.now().plusSeconds(TTL_SECONDS);

        sessions.put(token, r);
        return token;
    }

    /** 用 token 換 username；過期會自動清掉並回 null */
    public String resolve(String token) {
        if (token == null || token.isBlank()) return null;

        SessionRecord r = sessions.get(token);
        if (r == null) return null;

        if (Instant.now().isAfter(r.expiresAt)) {
            sessions.remove(token);
            return null;
        }
        return r.username;
    }

    /** 登出：令 token 失效 */
    public void invalidate(String token) {
        if (token != null && !token.isBlank()) {
            sessions.remove(token);
        }
    }

    public long getTtlSeconds() {
        return TTL_SECONDS;
    }
}