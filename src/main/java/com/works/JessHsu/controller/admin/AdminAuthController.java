package com.works.JessHsu.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.works.JessHsu.dto.LoginRequest;
import com.works.JessHsu.dto.LoginResponse;
import com.works.JessHsu.entity.AdminUser;
import com.works.JessHsu.security.AdminSessionStore;
import com.works.JessHsu.security.LoginAttemptService;
import com.works.JessHsu.service.AdminAuthService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/admin/auth") // ✅ 後台專用 namespace
public class AdminAuthController {

    private final AdminAuthService authService;
    private final LoginAttemptService attemptService;
    private final AdminSessionStore sessionStore; // 產 token ⬅ 很重要

    public AdminAuthController(
            AdminAuthService authService,
            LoginAttemptService attemptService,
            AdminSessionStore sessionStore
    ) {
        this.authService = authService;
        this.attemptService = attemptService;
        this.sessionStore = sessionStore;
    }

    /**
     * 後台登入：
     * - 防爆破：5 次錯 -> 鎖 10 分鐘
     * - 成功就回 token（Bearer ...）
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest body,
            HttpServletRequest request
    ) {
        String ip = request.getRemoteAddr();

        // 1. 先檢查鎖定
        long remainSeconds = attemptService.checkLockedAndGetRemainingSeconds(ip);
        if (remainSeconds > 0) {
            String msg = "登入失敗次數過多，帳號暫時鎖定，請 " + remainSeconds + " 秒後再試";
            return ResponseEntity.status(429).body(msg); // 429 Too Many Requests
        }

        // 2. 嘗試登入（驗密碼）
        AdminUser user = authService.login(body.getUsername(), body.getPassword());

        if (user == null) {
            // 登入失敗 → 累加
            attemptService.recordFail(ip);

            int failCount = attemptService.getFailCount(ip);
            int maxFails = attemptService.getMaxFails();

            // 再檢查一次鎖定（這次失敗後可能剛好到 5 次）
            long remainAfter = attemptService.checkLockedAndGetRemainingSeconds(ip);
            if (remainAfter > 0) {
                String msg = "登入失敗已達上限，帳號鎖定，請 " + remainAfter + " 秒後再試";
                return ResponseEntity.status(429).body(msg);
            }

            // 還沒鎖，但錯了 -> 告知剩餘嘗試數
            int left = Math.max(maxFails - failCount, 0);
            String msg;
            if (left > 0) {
                msg = "帳號或密碼錯誤（已失敗 " + failCount + " 次，剩餘 " + left + " 次嘗試機會）";
            } else {
                msg = "帳號或密碼錯誤";
            }

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED) // 401
                    .body(msg);
        }

        // 3. 成功 -> 清除紀錄
        attemptService.recordSuccess(ip);

        // 4. 簽發 token（會內含過期時間）
        String token = sessionStore.issueToken(user.getId());

        // 5. 回傳 JSON 給前端，帶 token
        //    remainingTries 對成功登入來說其實沒意義，塞 -1 即可
        LoginResponse resp = new LoginResponse(
                user.getUsername(),
                true,
                token,
                -1
        );

        return ResponseEntity.ok(resp);
    }

    /**
     * 驗證目前 token 是否有效
     * 前端可在打開後台頁面時 call 來確認還在登入狀態
     *
     * 這裡我們用 Authorization header，所以 GET /me 不再用 session。
     * 我們手動讀 token，重用 AdminSessionStore。
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(
            @RequestHeader(name = "Authorization", required = false) String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登入");
        }

        String token = authHeader.substring("Bearer ".length()).trim();
        Long adminId = sessionStore.resolve(token);

        if (adminId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登入或登入已過期");
        }

        // 這裡你也可以回更多 admin 資料，例如 username
        return ResponseEntity.ok(adminId);
    }

    /**
     * 登出：
     * 其實在純 token 模式，最簡單的做法是前端直接把 token 從 localStorage 拿掉。
     * 如果你想做「後端也失效」就讓前端傳 token，我們從 map 裡刪掉。
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader(name = "Authorization", required = false) String authHeader
    ) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring("Bearer ".length()).trim();
            sessionStore.invalidate(token); // 你需要在 AdminSessionStore 補這個方法
        }
        return ResponseEntity.ok().build();
    }
}