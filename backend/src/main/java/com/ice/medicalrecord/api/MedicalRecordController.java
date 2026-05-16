package com.ice.medicalrecord.api;

import com.ice.medicalrecord.api.dto.MedicalRecordDtos.CreateMedicalRecordRequest;
import com.ice.medicalrecord.api.dto.MedicalRecordDtos.MedicalRecordResponse;
import com.ice.medicalrecord.service.MedicalRecordService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 病历接口。
 * 提供病历检索、详情查询和新增病历能力。
 */
@RestController
@RequestMapping("/api/medical-records")
public class MedicalRecordController {
    private final MedicalRecordService medicalRecordService;

    public MedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    /**
     * 分页检索病历。
     * 支持按关键字、医生和日期范围组合过滤。
     */
    @GetMapping
    public Page<MedicalRecordResponse> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Pageable pageable) {
        return medicalRecordService.search(query, doctorId, fromDate, toDate, pageable);
    }

    /**
     * 查询单条病历详情。
     */
    @GetMapping("/{id}")
    public MedicalRecordResponse get(@PathVariable Long id) {
        return medicalRecordService.get(id);
    }

    /**
     * 新增病历与关联用药记录。
     */
    @PostMapping
    public MedicalRecordResponse create(@Valid @RequestBody CreateMedicalRecordRequest request, Principal principal) {
        return medicalRecordService.create(request, principal.getName());
    }
}
