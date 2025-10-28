package com.works.JessHsu.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * 以 IP 為 key 的爆破防護：
 * - 5 次錯誤就鎖 10 分鐘
 * - 鎖定中直接擋
 * - 每次失敗有一點點 delay
 */
@Component
public class LoginAttemptService {

    private static final int MAX_FAILS = 5;            // 允許最多 5 次錯誤
    private static final long LOCK_SECONDS = 10 * 60;  // 鎖 10 分鐘

    private static class AttemptInfo {
        int failCount = 0;
        Instant lockUntil = null;
        Instant lastFailAt = null;
    }

    private final Map<String, AttemptInfo> attempts = new ConcurrentHashMap<>();

    /** 呼叫於「嘗試登入之前」，檢查這個 IP 是否被鎖 */
    public long checkLockedAndGetRemainingSeconds(String ip) {
        AttemptInfo info = attempts.get(ip);
        if (info == null || info.lockUntil == null) return 0L;

        Instant now = Instant.now();
        if (now.isBefore(info.lockUntil)) {
            return Duration.between(now, info.lockUntil).getSeconds();
        } else {
            // 鎖已過期 → 自動解鎖歸零
            attempts.remove(ip);
            return 0L;
        }
    }

    /** 登入失敗時呼叫：計數 + 視情況上鎖 */
    public void recordFail(String ip) {
        AttemptInfo info = attempts.computeIfAbsent(ip, k -> new AttemptInfo());
        info.failCount++;
        info.lastFailAt = Instant.now();

        if (info.failCount >= MAX_FAILS) {
            info.lockUntil = Instant.now().plusSeconds(LOCK_SECONDS);
        }

        // 簡單阻斷爆破: 隨失敗次數增加一點點延遲 (0.5s ~ 2s)
        try {
            long sleepMillis = Math.min(info.failCount * 500L, 2000L);
            Thread.sleep(sleepMillis);
        } catch (InterruptedException ignored) {}
    }

    /** 登入成功就重置這個 IP 的紀錄 */
    public void recordSuccess(String ip) {
        attempts.remove(ip);
    }

    /** 回傳目前錯誤次數（0~5+） */
    public int getFailCount(String ip) {
        AttemptInfo info = attempts.get(ip);
        return (info == null ? 0 : info.failCount);
    }

    /** 回傳鎖定門檻（目前是 5） */
    public int getMaxFails() {
        return MAX_FAILS;
    }
}