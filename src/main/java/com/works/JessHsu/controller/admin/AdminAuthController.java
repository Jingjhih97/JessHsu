package com.works.JessHsu.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.works.JessHsu.dto.LoginRequest;
import com.works.JessHsu.dto.LoginResponse;
import com.works.JessHsu.entity.AdminUser;
import com.works.JessHsu.security.AdminSessionStore;
import com.works.JessHsu.security.LoginAttemptService;
import com.works.JessHsu.service.AdminAuthService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthService authService;
    private final AdminSessionStore sessionStore;
    private final LoginAttemptService loginAttemptService;

    public AdminAuthController(
            AdminAuthService authService,
            AdminSessionStore sessionStore,
            LoginAttemptService loginAttemptService
    ) {
        this.authService = authService;
        this.sessionStore = sessionStore;
        this.loginAttemptService = loginAttemptService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest body,
            HttpServletRequest request
    ) {
        final String clientIp = request.getRemoteAddr();

        // 1. 有沒有被鎖
        if (loginAttemptService.isLocked(clientIp)) {
            long remain = loginAttemptService.lockRemainingSeconds(clientIp);
            String msg = "登入失敗次數過多，帳號暫時鎖定，請 " + remain + " 秒後再試";
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(msg);
        }

        // 2. 嘗試登入
        AdminUser user = authService.login(body.getUsername(), body.getPassword());

        if (user == null) {
            // 2-1. 失敗 → 累積失敗次數 + 延遲回應（防爆破）
            loginAttemptService.recordFailure(clientIp);

            // 小延遲（不阻塞執行緒池太久，這邊是同步 sleep，對這個小專案是 OK 的）
            try {
                long delay = loginAttemptService.suggestedDelayMillis(clientIp);
                if (delay > 0) {
                    Thread.sleep(delay);
                }
            } catch (InterruptedException ignore) {
                Thread.currentThread().interrupt();
            }

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("帳號或密碼錯誤");
        }

        // 2-2. 成功 → 清掉錯誤紀錄
        loginAttemptService.recordSuccess(clientIp);

        // 3. 建 token（短壽命 + 自己存在記憶體）
        String token = sessionStore.createSessionFor(user.getId());

        // 4. 回傳登入成功結果
        //    LoginResponse 建議長這樣：
        //    { username: "admin", ok: true, token: "xxx.yyy.zzz" }
        return ResponseEntity.ok(
                new LoginResponse(
                        user.getUsername(),
                        true,
                        token
                )
        );
    }

    /**
     * 管理者手動登出：其實就是讓前端把 token 丟掉就好
     * 但如果你想在伺服器端也無效化該 token，可以在這裡做
     * ex: sessionStore.invalidate(tokenFromHeader)
     *
     * 先留著一個簡單版本
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok().build();
    }

    /**
     * 用來讓前端確認「這個 token 是否還有效」
     * 前端會帶 Authorization: Bearer <token>
     * AdminAuthFilter 會先驗證 token 有效，才會進來這裡
     */
    @GetMapping("/me")
    public ResponseEntity<?> me() {
        // 如果程式碼跑到這裡，表示 filter 已經認證了
        // 你可以在 filter 裡面把 userId 存在 request attribute，然後在這裡取出來回傳
        return ResponseEntity.ok("OK");
    }
}