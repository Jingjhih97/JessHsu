package com.works.JessHsu.controller.admin;

import com.works.JessHsu.dto.LoginRequest;
import com.works.JessHsu.dto.LoginResponse;
import com.works.JessHsu.entity.AdminUser;
import com.works.JessHsu.security.LoginAttemptService;
import com.works.JessHsu.service.AdminAuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // ← 依你現在的路徑
public class AdminAuthController {

    public static final String SESSION_KEY = "ADMIN_AUTH";

    private final AdminAuthService authService;
    private final LoginAttemptService attemptService;

    public AdminAuthController(AdminAuthService authService,
                               LoginAttemptService attemptService) {
        this.authService = authService;
        this.attemptService = attemptService;
    }

    /** 登入 */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest body,
                                   HttpServletRequest request,
                                   HttpSession session) {

        String ip = request.getRemoteAddr();

        // 1. 先檢查是否被鎖
        long remain = attemptService.checkLockedAndGetRemainingSeconds(ip);
        if (remain > 0) {
            String msg = "登入失敗次數過多，帳號暫時鎖定，請 " + remain + " 秒後再試";
            return ResponseEntity.status(429).body(msg); // 429 Too Many Requests
        }

        // 2. 嘗試登入
        AdminUser user = authService.login(body.getUsername(), body.getPassword());

        if (user == null) {
            // 登入失敗
            attemptService.recordFail(ip);

            int failCount = attemptService.getFailCount(ip);
            int maxFails  = attemptService.getMaxFails();

            // 檢查是否剛好鎖住（第5次之後再問一次）
            long remainAfter = attemptService.checkLockedAndGetRemainingSeconds(ip);
            if (remainAfter > 0) {
                String msg = "登入失敗已達上限，帳號鎖定，請 " + remainAfter + " 秒後再試";
                return ResponseEntity.status(429).body(msg);
            }

            // 還沒鎖，但錯了
            int left = Math.max(maxFails - failCount, 0);
            String msg;
            if (left > 0) {
                msg = "帳號或密碼錯誤（已失敗 " + failCount + " 次，剩餘 " + left + " 次嘗試機會）";
            } else {
                // 理論上進不到這裡，因為 lock 會先 return 429
                msg = "帳號或密碼錯誤";
            }

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED) // 401
                    .body(msg);
        }

        // 3. 成功登入 → 清掉失敗記錄
        attemptService.recordSuccess(ip);

        // 4. 寫入 session 代表已登入
        session.setAttribute(SESSION_KEY, user.getId());

        // 5. 回傳簡單成功 JSON
        return ResponseEntity.ok(new LoginResponse(user.getUsername(), true));
    }

    /** 登出 */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().build();
    }

    /** 檢查是否登入 */
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Object uid = session.getAttribute(SESSION_KEY);
        if (uid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登入");
        }
        return ResponseEntity.ok(uid);
    }
}