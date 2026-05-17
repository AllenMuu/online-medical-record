package com.ice.medicalrecord;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.ice.medicalrecord.api.dto.AuthDtos.LoginRequest;
import com.ice.medicalrecord.api.dto.MedicalRecordDtos.UpsertMedicalRecordRequest;
import com.ice.medicalrecord.api.dto.PatientDtos.UpsertPatientRequest;
import com.ice.medicalrecord.domain.Gender;
import com.ice.medicalrecord.domain.RecordStatus;
import com.ice.medicalrecord.domain.User;
import com.ice.medicalrecord.repository.UserRepository;
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

    private MockHttpSession loginAsAdmin() throws Exception {
        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin@aura.local", "Admin123!"))))
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession(false);

        org.assertj.core.api.Assertions.assertThat(session).isNotNull();
        return session;
    }

    @Test
    void loginAndCreatePatientAndRecordFlowWorks() throws Exception {
        MockHttpSession session = loginAsAdmin();

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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());

        User doctor = userRepository.findByEmailIgnoreCase("doctor@aura.local").orElseThrow();
        long patientId = 1L;

        mockMvc.perform(post("/api/medical-records").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpsertMedicalRecordRequest(
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

        mockMvc.perform(get("/api/medical-records").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.content[0].createdAt").isNotEmpty())
                .andExpect(jsonPath("$.content[0].updatedAt").isNotEmpty());

        mockMvc.perform(get("/api/medical-records").session(session).param("query", "测试诊断"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].diagnosis").value(hasItem("测试诊断")));

        mockMvc.perform(get("/api/patients").session(session).param("teamQuery", "心脏内科"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].team").value(hasItem("心脏内科 A组")))
                .andExpect(jsonPath("$.content[*].createdAt").isNotEmpty())
                .andExpect(jsonPath("$.content[*].updatedAt").isNotEmpty());

        mockMvc.perform(get("/api/admin/users").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].createdAt").isNotEmpty())
                .andExpect(jsonPath("$[*].updatedAt").isNotEmpty());
    }

    @Test
    void medicalRecordViewAndUpdateFlowWorks() throws Exception {
        MockHttpSession session = loginAsAdmin();
        User doctor = userRepository.findByEmailIgnoreCase("doctor@aura.local").orElseThrow();

        String createdPatient = mockMvc.perform(post("/api/patients").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpsertPatientRequest(
                                "病历编辑患者",
                                Gender.MALE,
                                28,
                                "神经内科 B组",
                                "13600000000",
                                LocalDate.of(1998, 6, 1),
                                "病历编辑流程测试"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long patientId = objectMapper.readTree(createdPatient).get("id").asLong();

        String createdRecord = mockMvc.perform(post("/api/medical-records").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpsertMedicalRecordRequest(
                                patientId,
                                doctor.getId(),
                                LocalDate.of(2026, 5, 16),
                                LocalTime.of(9, 30),
                                "编辑前诊断",
                                "编辑前主诉",
                                "编辑前查体",
                                "编辑前处置",
                                "编辑前预后",
                                "编辑前备注",
                                RecordStatus.IN_PROGRESS,
                                java.util.List.of()))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long recordId = objectMapper.readTree(createdRecord).get("id").asLong();

        mockMvc.perform(get("/api/medical-records/{id}", recordId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diagnosis").value("编辑前诊断"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(patch("/api/medical-records/{id}", recordId).session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpsertMedicalRecordRequest(
                                patientId,
                                doctor.getId(),
                                LocalDate.of(2026, 5, 17),
                                LocalTime.of(10, 15),
                                "编辑后诊断",
                                "编辑后主诉",
                                "编辑后查体",
                                "编辑后处置",
                                "编辑后预后",
                                "编辑后备注",
                                RecordStatus.COMPLETED,
                                java.util.List.of()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visitDate").value("2026-05-17"))
                .andExpect(jsonPath("$.visitTime").value("10:15:00"))
                .andExpect(jsonPath("$.diagnosis").value("编辑后诊断"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        mockMvc.perform(get("/api/medical-records/{id}", recordId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("编辑后备注"));
    }

    @Test
    void patientUpdateAndDeleteFlowWorks() throws Exception {
        MockHttpSession session = loginAsAdmin();

        String createdPatient = mockMvc.perform(post("/api/patients").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpsertPatientRequest(
                                "待编辑患者",
                                Gender.FEMALE,
                                31,
                                "外科护理 2队",
                                "13900000000",
                                LocalDate.of(1995, 3, 15),
                                "编辑前摘要"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long patientId = objectMapper.readTree(createdPatient).get("id").asLong();

        mockMvc.perform(patch("/api/patients/{id}", patientId).session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpsertPatientRequest(
                                "已编辑患者",
                                Gender.FEMALE,
                                32,
                                "急诊科 先锋组",
                                "13900000001",
                                LocalDate.of(1994, 3, 15),
                                "编辑后摘要"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("已编辑患者"))
                .andExpect(jsonPath("$.age").value(32))
                .andExpect(jsonPath("$.team").value("急诊科 先锋组"));

        mockMvc.perform(delete("/api/patients/{id}", patientId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("患者已删除"));

        mockMvc.perform(get("/api/patients").session(session).param("nameQuery", "已编辑患者"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void patientCreateRejectsDuplicateNameWithinSameTeam() throws Exception {
        MockHttpSession session = loginAsAdmin();
        UpsertPatientRequest request = new UpsertPatientRequest(
                "重复患者",
                Gender.MALE,
                45,
                "肿瘤科 一队",
                "13700000000",
                LocalDate.of(1981, 2, 2),
                "首次建档");

        mockMvc.perform(post("/api/patients").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/patients").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpsertPatientRequest(
                                "  重复患者  ",
                                Gender.FEMALE,
                                46,
                                "  肿瘤科 一队  ",
                                "13700000001",
                                LocalDate.of(1980, 2, 2),
                                "重复提交"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("同一队伍下患者姓名不能重复"));
    }

    @Test
    void patientUpdateRejectsDuplicateNameWithinSameTeam() throws Exception {
        MockHttpSession session = loginAsAdmin();

        String firstPatient = mockMvc.perform(post("/api/patients").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpsertPatientRequest(
                                "原始患者",
                                Gender.MALE,
                                34,
                                "康复科 三组",
                                "13500000000",
                                LocalDate.of(1992, 4, 4),
                                "原始数据"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String secondPatient = mockMvc.perform(post("/api/patients").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpsertPatientRequest(
                                "待更新患者",
                                Gender.FEMALE,
                                29,
                                "康复科 四组",
                                "13500000001",
                                LocalDate.of(1997, 5, 5),
                                "待更新数据"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long secondPatientId = objectMapper.readTree(secondPatient).get("id").asLong();
        long firstPatientId = objectMapper.readTree(firstPatient).get("id").asLong();

        mockMvc.perform(patch("/api/patients/{id}", secondPatientId).session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpsertPatientRequest(
                                " 原始患者 ",
                                Gender.FEMALE,
                                30,
                                " 康复科 三组 ",
                                "13500000002",
                                LocalDate.of(1996, 5, 5),
                                "尝试改成重复"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("同一队伍下患者姓名不能重复"));

        mockMvc.perform(get("/api/patients").session(session).param("nameQuery", "原始患者"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id").value(hasItem((int) firstPatientId)));
    }
}
