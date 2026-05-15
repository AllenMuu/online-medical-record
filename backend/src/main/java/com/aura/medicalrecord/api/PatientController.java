package com.aura.medicalrecord.api;

import com.aura.medicalrecord.api.dto.PatientDtos.PatientResponse;
import com.aura.medicalrecord.api.dto.PatientDtos.UpsertPatientRequest;
import com.aura.medicalrecord.service.PatientService;
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

@RestController
@RequestMapping("/api/patients")
public class PatientController {
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public Page<PatientResponse> list(@RequestParam(required = false) String query, Pageable pageable) {
        return patientService.list(query, pageable);
    }

    @PostMapping
    public PatientResponse create(@Valid @RequestBody UpsertPatientRequest request, Principal principal) {
        return patientService.create(request, principal.getName());
    }

    @PatchMapping("/{id}")
    public PatientResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpsertPatientRequest request,
            Principal principal) {
        return patientService.update(id, request, principal.getName());
    }
}
