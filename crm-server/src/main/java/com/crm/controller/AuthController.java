package com.crm.controller;

import com.crm.common.Result;
import com.crm.dto.LoginRequest;
import com.crm.service.AuthService;
import com.crm.vo.LoginVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<Map<String, String>> login(@Validated @RequestBody LoginRequest request) {
        LoginVO vo = authService.login(request);
        Map<String, String> data = new HashMap<>();
        data.put("token", vo.getToken());
        return Result.ok(data);
    }

    @GetMapping("/info")
    public Result<LoginVO> info() {
        return Result.ok(authService.getUserInfo());
    }

    @GetMapping("/menus")
    public Result<LoginVO> menus() {
        return Result.ok(authService.getUserInfo());
    }
}
