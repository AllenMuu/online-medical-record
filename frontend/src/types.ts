export type Role = 'ADMIN' | 'DOCTOR';
export type Gender = 'MALE' | 'FEMALE' | 'OTHER';
export type RecordStatus = 'IN_PROGRESS' | 'COMPLETED';

export interface User {
  id: number;
  name: string;
  email: string;
  role: Role;
  active: boolean;
  department?: string;
  avatarUrl?: string;
  createdAt: string;
  updatedAt: string;
}

export interface Patient {
  id: number;
  name: string;
  gender: Gender;
  age: number;
  team: string;
  phone?: string;
  birthDate?: string;
  summary?: string;
  createdAt: string;
  updatedAt: string;
}

export interface MedicalRecord {
  id: number;
  patientId: number;
  patientName: string;
  patientGender: string;
  patientAge: number;
  doctorId: number;
  doctorName: string;
  visitDate: string;
  visitTime: string;
  diagnosis: string;
  complaint?: string;
  examination?: string;
  treatment?: string;
  prognosis?: string;
  notes?: string;
  status: RecordStatus;
  createdAt: string;
  updatedAt: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface DashboardSummary {
  patientCount: number;
  recordCount: number;
  monthlyRecords: number;
  doctorCount: number;
}
