package com.works.JessHsu.security;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * 管理後台用的 Bearer token，
 * 保存在記憶體，不進資料庫。
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
     * 建立 token，回傳 token 字串
     */
    public String createSessionFor(Long adminId) {
        String token = UUID.randomUUID().toString().replace("-", "");

        SessionRecord r = new SessionRecord();
        r.adminId = adminId;
        r.expiresAt = Instant.now().plusSeconds(TTL_SECONDS);

        sessions.put(token, r);

        return token;
    }

    /**
     * 檢查 token 是否存在且未過期
     * 回傳 adminId；如果失效回 null
     */
    public Long validate(String token) {
        if (token == null || token.isBlank()) return null;

        SessionRecord r = sessions.get(token);
        if (r == null) return null;

        // 過期了 -> 移除
        if (Instant.now().isAfter(r.expiresAt)) {
            sessions.remove(token);
            return null;
        }

        return r.adminId;
    }

    /**
     * 主動登出：讓 token 失效
     */
    public void invalidate(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }
}