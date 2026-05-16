package com.ice.medicalrecord.api;

import com.ice.medicalrecord.api.dto.PatientDtos.PatientResponse;
import com.ice.medicalrecord.api.dto.PatientDtos.UpsertPatientRequest;
import com.ice.medicalrecord.service.PatientService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 患者档案接口。
 * 提供患者列表、新增和更新能力。
 */
@RestController
@RequestMapping("/api/patients")
public class PatientController {
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    /**
     * 分页查询患者。
     * query 为空时返回全部，否则按姓名或团队模糊检索。
     */
    @GetMapping
    public Page<PatientResponse> list(@RequestParam(required = false) String query, Pageable pageable) {
        return patientService.list(query, pageable);
    }

    /**
     * 新增患者档案。
     */
    @PostMapping
    public PatientResponse create(@Valid @RequestBody UpsertPatientRequest request, Principal principal) {
        return patientService.create(request, principal.getName());
    }

    /**
     * 更新指定患者档案。
     */
    @PatchMapping("/{id}")
    public PatientResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpsertPatientRequest request,
            Principal principal) {
        return patientService.update(id, request, principal.getName());
    }
}
