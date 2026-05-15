package com.aura.medicalrecord.config;

import com.aura.medicalrecord.domain.DoctorProfile;
import com.aura.medicalrecord.domain.Gender;
import com.aura.medicalrecord.domain.MedicalRecord;
import com.aura.medicalrecord.domain.Medication;
import com.aura.medicalrecord.domain.Patient;
import com.aura.medicalrecord.domain.RecordStatus;
import com.aura.medicalrecord.domain.Role;
import com.aura.medicalrecord.domain.User;
import com.aura.medicalrecord.repository.MedicalRecordRepository;
import com.aura.medicalrecord.repository.PatientRepository;
import com.aura.medicalrecord.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements CommandLineRunner {
    private static final String DOCTOR_AVATAR = "https://lh3.googleusercontent.com/aida-public/AB6AXuA2SvgHuI_MCXg-jIRL2kNz7rC6jxnIRaNy6X3Dm-tgwFEkR9DAFzCSMVAy75wC7P_yBkFtl2KY4GTcJIMqEQ-Y2ZFT_Zqhi9lcQhI_fk4LqrUAKKaZOgywTk1UiHoieAzqd0p9l06OjN09jU4pvY6t0r3UQFQ0HlKQ7-RsLDsMImLSvPVOGqPz4rKGB1rntPS688oX0LNnsrAOms6M3-A_s4S9JcgLGJyO69GUHNyzMhuRN6_0BXtyu-Iq9y1h3IxqrZ8-NH45cxs";

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final MedicalRecordRepository recordRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            UserRepository userRepository,
            PatientRepository patientRepository,
            MedicalRecordRepository recordRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.recordRepository = recordRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        User admin = createUser("系统管理员", "admin@aura.local", "Admin123!", Role.ADMIN, null, null);
        User doctor = createUser("张医生", "doctor@aura.local", "Doctor123!", Role.DOCTOR, "主治医师", "心脏内科 A组");
        User doctorTwo = createUser("王志远", "wang@aura.local", "Doctor123!", Role.DOCTOR, "副主任医师", "内分泌科");
        userRepository.save(admin);
        userRepository.save(doctor);
        userRepository.save(doctorTwo);

        Patient zhang = createPatient("张三丰", Gender.MALE, 68, "心脏内科 A组", "原发性高血压随访");
        Patient li = createPatient("李美丽", Gender.FEMALE, 45, "外科护理 2队", "2型糖尿病复查");
        Patient zhao = createPatient("赵忠贤", Gender.MALE, 52, "急诊科 先锋组", "急性上呼吸道感染");
        patientRepository.save(zhang);
        patientRepository.save(li);
        patientRepository.save(zhao);

        createRecord(zhang, doctor, LocalDate.of(2023, 11, 20), "原发性高血压 (Grade 3)", "阿莫西林", "0.5g tid 口服");
        createRecord(li, doctorTwo, LocalDate.of(2023, 11, 19), "2型糖尿病复查", "二甲双胍", "0.5g bid 口服");
        createRecord(zhao, doctor, LocalDate.of(2023, 11, 19), "急性上呼吸道感染", "布洛芬", "0.2g prn 口服");
    }

    private User createUser(String name, String email, String password, Role role, String title, String department) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        if (role == Role.DOCTOR) {
            DoctorProfile profile = new DoctorProfile();
            profile.setUser(user);
            profile.setTitle(title);
            profile.setDepartment(department);
            profile.setAvatarUrl(DOCTOR_AVATAR);
            user.setDoctorProfile(profile);
        }
        return user;
    }

    private Patient createPatient(String name, Gender gender, int age, String team, String summary) {
        Patient patient = new Patient();
        patient.setName(name);
        patient.setGender(gender);
        patient.setAge(age);
        patient.setTeam(team);
        patient.setSummary(summary);
        return patient;
    }

    private void createRecord(Patient patient, User doctor, LocalDate date, String diagnosis, String medName, String dosage) {
        MedicalRecord record = new MedicalRecord();
        record.setPatient(patient);
        record.setDoctor(doctor);
        record.setVisitDate(date);
        record.setVisitTime(LocalTime.of(10, 30));
        record.setDiagnosis(diagnosis);
        record.setComplaint("患者主诉与既往史已记录，当前生命体征平稳。");
        record.setExamination("体格检查未见明显急性风险，建议继续随访。");
        record.setTreatment("按临床路径给予药物治疗并进行健康宣教。");
        record.setPrognosis("预后良好，需关注复诊指标变化。");
        record.setNotes("无特殊过敏史提醒。");
        record.setStatus(RecordStatus.COMPLETED);
        Medication medication = new Medication();
        medication.setMedicalRecord(record);
        medication.setName(medName);
        medication.setDosage(dosage);
        record.getMedications().add(medication);
        recordRepository.save(record);
    }
}
