package com.ice.medicalrecord.api;

import com.ice.medicalrecord.api.dto.UserDtos.UserResponse;
import com.ice.medicalrecord.service.UserService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 医生列表接口。
 * 供病历创建等场景获取可选医生。
 */
@RestController
@RequestMapping("/api/doctors")
public class DoctorController {
    private final UserService userService;

    public DoctorController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 返回所有医生账号，按姓名升序。
     */
    @GetMapping
    public List<UserResponse> list() {
        return userService.listDoctors();
    }
}
