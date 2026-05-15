package com.aura.medicalrecord;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.aura.medicalrecord.api.dto.AuthDtos.LoginRequest;
import com.aura.medicalrecord.api.dto.MedicalRecordDtos.CreateMedicalRecordRequest;
import com.aura.medicalrecord.api.dto.PatientDtos.UpsertPatientRequest;
import com.aura.medicalrecord.domain.Gender;
import com.aura.medicalrecord.domain.RecordStatus;
import com.aura.medicalrecord.domain.Role;
import com.aura.medicalrecord.domain.User;
import com.aura.medicalrecord.repository.UserRepository;
import tools.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SmokeIntegrationTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Test
    void loginAndCreatePatientAndRecordFlowWorks() throws Exception {
        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin@aura.local", "Admin123!"))))
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession(false);

        org.assertj.core.api.Assertions.assertThat(session).isNotNull();

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/patients").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpsertPatientRequest(
                                "测试患者",
                                Gender.MALE,
                                40,
                                "心脏内科 A组",
                                "13800000000",
                                LocalDate.of(1986, 1, 1),
                                "单元测试新增"))))
                .andExpect(status().isOk());

        User doctor = userRepository.findByEmailIgnoreCase("doctor@aura.local").orElseThrow();
        long patientId = 1L;

        mockMvc.perform(post("/api/medical-records").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateMedicalRecordRequest(
                                patientId,
                                doctor.getId(),
                                LocalDate.of(2023, 11, 21),
                                LocalTime.of(9, 0),
                                "测试诊断",
                                "主诉",
                                "查体",
                                "处置",
                                "预后",
                                "备注",
                                RecordStatus.COMPLETED,
                                java.util.List.of()))))
                .andExpect(status().isOk());
    }
}
