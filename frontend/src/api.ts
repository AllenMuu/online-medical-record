import type { DashboardSummary, MedicalRecord, Page, Patient, RecordStatus, Role, User } from './types';

const jsonHeaders = { 'Content-Type': 'application/json' };

async function request<T>(url: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(url, {
    credentials: 'include',
    headers: options.body ? jsonHeaders : undefined,
    ...options,
  });
  if (!response.ok) {
    const body = await response.json().catch(() => ({ message: '请求失败' }));
    throw new Error(body.message || '请求失败');
  }
  return response.json() as Promise<T>;
}

async function multipartRequest<T>(url: string, body: FormData, options: RequestInit = {}): Promise<T> {
  const response = await fetch(url, {
    credentials: 'include',
    body,
    ...options,
  });
  if (!response.ok) {
    const result = await response.json().catch(() => ({ message: '请求失败' }));
    throw new Error(result.message || '请求失败');
  }
  return response.json() as Promise<T>;
}

export interface PatientPayload {
  name: string;
  gender: string;
  age: number;
  team: string;
  phone?: string;
  birthDate?: string;
  summary?: string;
}

export interface RecordPayload {
  patientId: number;
  doctorId: number;
  visitDate: string;
  visitTime: string;
  diagnosis: string;
  complaint?: string;
  examination?: string;
  treatment?: string;
  prognosis?: string;
  notes?: string;
  status?: RecordStatus;
}

export interface CreateUserPayload {
  name: string;
  email: string;
  password: string;
  role: Role;
  department?: string;
}

export interface UpdateUserPayload {
  name?: string;
  active?: boolean;
  department?: string;
  avatarUrl?: string;
}

export interface UserListParams {
  role?: Role;
  nameQuery?: string;
  page?: number;
  size?: number;
}

export const api = {
  login: (email: string, password: string) =>
    request<User>('/api/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) }),
  logout: () => request<{ message: string }>('/api/auth/logout', { method: 'POST' }),
  me: () => request<User>('/api/auth/me'),
  updateMyAvatar: (file: File) => {
    const formData = new FormData();
    formData.append('avatar', file);
    return multipartRequest<{ avatarUrl: string }>('/api/auth/me/avatar', formData, { method: 'PATCH' });
  },
  requestReset: (email: string) =>
    request<{ message: string; resetToken?: string }>('/api/auth/password-reset/request', {
      method: 'POST',
      body: JSON.stringify({ email }),
    }),
  resetPassword: (token: string, newPassword: string) =>
    request<{ message: string }>('/api/auth/password-reset/confirm', {
      method: 'POST',
      body: JSON.stringify({ token, newPassword }),
    }),
  changePassword: (currentPassword: string, newPassword: string) =>
    request<{ message: string }>('/api/auth/change-password', {
      method: 'POST',
      body: JSON.stringify({ currentPassword, newPassword }),
    }),
  doctors: () => request<User[]>('/api/doctors'),
  users: ({ role, nameQuery = '', page = 0, size = 10 }: UserListParams = {}) => {
    const params = new URLSearchParams({
      page: String(page),
      size: String(size),
    });
    if (role) {
      params.set('role', role);
    }
    if (nameQuery.trim()) {
      params.set('nameQuery', nameQuery.trim());
    }
    return request<Page<User>>(`/api/admin/users?${params.toString()}`);
  },
  createUser: (payload: CreateUserPayload) =>
    request<User>('/api/admin/users', { method: 'POST', body: JSON.stringify(payload) }),
  updateUser: (id: number, payload: UpdateUserPayload) =>
    request<User>(`/api/admin/users/${id}`, { method: 'PATCH', body: JSON.stringify(payload) }),
  deleteUser: (id: number) =>
    request<{ message: string }>(`/api/admin/users/${id}`, { method: 'DELETE' }),
  patients: (nameQuery = '', teamQuery = '', page = 0, size = 10) => {
    const params = new URLSearchParams({
      page: String(page),
      size: String(size),
    });
    if (nameQuery.trim()) {
      params.set('nameQuery', nameQuery.trim());
    }
    if (teamQuery.trim()) {
      params.set('teamQuery', teamQuery.trim());
    }
    return request<Page<Patient>>(`/api/patients?${params.toString()}`);
  },
  createPatient: (payload: PatientPayload) =>
    request<Patient>('/api/patients', { method: 'POST', body: JSON.stringify(payload) }),
  updatePatient: (id: number, payload: PatientPayload) =>
    request<Patient>(`/api/patients/${id}`, { method: 'PATCH', body: JSON.stringify(payload) }),
  deletePatient: (id: number) =>
    request<{ message: string }>(`/api/patients/${id}`, { method: 'DELETE' }),
  records: (params: URLSearchParams) => request<Page<MedicalRecord>>(`/api/medical-records?${params.toString()}`),
  record: (id: number) => request<MedicalRecord>(`/api/medical-records/${id}`),
  createRecord: (payload: RecordPayload) =>
    request<MedicalRecord>('/api/medical-records', { method: 'POST', body: JSON.stringify(payload) }),
  updateRecord: (id: number, payload: RecordPayload) =>
    request<MedicalRecord>(`/api/medical-records/${id}`, { method: 'PATCH', body: JSON.stringify(payload) }),
  deleteRecord: (id: number) =>
    request<{ message: string }>(`/api/medical-records/${id}`, { method: 'DELETE' }),
  dashboard: () => request<DashboardSummary>('/api/dashboard/summary'),
};
