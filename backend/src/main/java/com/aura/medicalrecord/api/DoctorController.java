package com.aura.medicalrecord.api;

import com.aura.medicalrecord.api.dto.UserDtos.UserResponse;
import com.aura.medicalrecord.service.UserService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {
    private final UserService userService;

    public DoctorController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> list() {
        return userService.listDoctors();
    }
}
