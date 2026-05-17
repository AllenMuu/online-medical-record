package com.ice.medicalrecord.api;

import com.ice.medicalrecord.api.dto.UserDtos.CreateUserRequest;
import com.ice.medicalrecord.api.dto.PageResponse;
import com.ice.medicalrecord.api.dto.UserDtos.UpdateUserRequest;
import com.ice.medicalrecord.api.dto.UserDtos.UserResponse;
import com.ice.medicalrecord.domain.Role;
import com.ice.medicalrecord.service.UserService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Map;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员用户管理接口。
 * 仅管理员可访问，用于维护系统账号和医生资料。
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 查询全部系统账号。
     */
    @GetMapping
    public PageResponse<UserResponse> list(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String nameQuery,
            Pageable pageable) {
        return PageResponse.from(userService.listUsers(role, nameQuery, pageable));
    }

    /**
     * 创建系统账号；医生角色会同步初始化医生扩展资料。
     */
    @PostMapping
    public UserResponse create(@Valid @RequestBody CreateUserRequest request, Principal principal) {
        return userService.create(request, principal.getName());
    }

    /**
     * 更新账号基本信息、启用状态或医生扩展资料。
     */
    @PatchMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @RequestBody UpdateUserRequest request, Principal principal) {
        return userService.update(id, request, principal.getName());
    }

    /**
     * 删除指定账号。
     */
    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id, Principal principal) {
        userService.delete(id, principal.getName());
        return Map.of("message", "医生账号已删除");
    }
}
