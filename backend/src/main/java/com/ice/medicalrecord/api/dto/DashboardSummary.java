package com.ice.medicalrecord.api.dto;

public record DashboardSummary(long patientCount, long recordCount, long monthlyRecords, long doctorCount) {
}
