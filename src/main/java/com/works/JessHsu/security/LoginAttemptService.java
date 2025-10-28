package com.works.JessHsu.security;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * 管理「同一個 IP 嘗試登入」的狀態：
 * - 連續失敗次數
 * - 是否被鎖住
 * - 什麼時候解鎖
 */
@Component
public class LoginAttemptService {

    // 允許錯幾次
    private static final int MAX_FAILS = 5;

    // 鎖多久（秒）：這裡 10 分鐘
    private static final long LOCK_SECONDS = 10 * 60;

    // 用 IP 當 key 的記錄表
    private final Map<String, AttemptInfo> attempts = new ConcurrentHashMap<>();

    private static class AttemptInfo {
        int failCount; // 累積失敗次數
        Instant lockUntil; // 若被鎖，鎖到什麼時刻
    }

    /** 該 IP 是否目前被鎖 */
    public boolean isLocked(String ip) {
        AttemptInfo info = attempts.get(ip);
        return info != null
                && info.lockUntil != null
                && Instant.now().isBefore(info.lockUntil);
    }

    /** 取得還要鎖多久（秒），沒鎖就回 0 */
    public long lockRemainingSeconds(String ip) {
        AttemptInfo info = attempts.get(ip);
        if (info == null || info.lockUntil == null)
            return 0;
        long diff = info.lockUntil.getEpochSecond() - Instant.now().getEpochSecond();
        return Math.max(diff, 0);
    }

    /** 登入失敗：+1，必要時進入鎖定狀態 */
    public void recordFailure(String ip) {
        AttemptInfo info = attempts.computeIfAbsent(ip, _k -> new AttemptInfo());
        info.failCount++;

        if (info.failCount >= MAX_FAILS) {
            info.lockUntil = Instant.now().plusSeconds(LOCK_SECONDS);
        }
    }

    /** 登入成功：清空這個 IP 的紀錄（歸零） */
    public void recordSuccess(String ip) {
        attempts.remove(ip);
    }

    /**
     * 根據目前 fail 次數計算一個「延遲毫秒數」。
     * 目的：讓爆力嘗試很慢，但正常使用者體感還可以。
     *
     * 設計：
     * - 0~1次失敗：0ms
     * - 2次失敗：500ms
     * - 3次失敗：1000ms
     * - 4次失敗以上：2000ms
     *
     * 你可以自由調整。
     */
    public long suggestedDelayMillis(String ip) {
        AttemptInfo info = attempts.get(ip);
        if (info == null)
            return 0;

        int c = info.failCount;
        if (c <= 1)
            return 0;
        if (c == 2)
            return 500;
        if (c == 3)
            return 1000;
        return 2000;
    }
}