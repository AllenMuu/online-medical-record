import { Eye, Save } from 'lucide-react';
import { type SubmitEvent, useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { api, type RecordPayload } from '../api';
import { useAuth } from '../AuthContext';
import { PageHeader } from '../components/PageHeader';
import { VisitDateTimeField } from '../components/VisitDateTimeField';
import type { MedicalRecord, Patient, User } from '../types';

function getTodayDate() {
  const now = new Date();
  const localDate = new Date(now.getTime() - now.getTimezoneOffset() * 60_000);
  return localDate.toISOString().slice(0, 10);
}

function createEmptyForm(doctorId = 0): RecordPayload {
  return {
    patientId: 0,
    doctorId,
    visitDate: getTodayDate(),
    visitTime: '10:30:00',
    diagnosis: '',
    complaint: '',
    examination: '',
    treatment: '',
    prognosis: '',
    notes: '',
    status: 'COMPLETED',
    medications: [],
  };
}

function toRecordPayload(record: MedicalRecord): RecordPayload {
  return {
    patientId: record.patientId,
    doctorId: record.doctorId,
    visitDate: record.visitDate,
    visitTime: record.visitTime,
    diagnosis: record.diagnosis,
    complaint: record.complaint ?? '',
    examination: record.examination ?? '',
    treatment: record.treatment ?? '',
    prognosis: record.prognosis ?? '',
    notes: record.notes ?? '',
    status: record.status,
    medications: record.medications.map((medication) => ({
      name: medication.name,
      dosage: medication.dosage,
    })),
  };
}

export function NewRecordPage() {
  const navigate = useNavigate();
  const { recordId } = useParams();
  const { user } = useAuth();
  const [patients, setPatients] = useState<Patient[]>([]);
  const [doctors, setDoctors] = useState<User[]>([]);
  const [patientQuery, setPatientQuery] = useState('');
  const [patientDropdownOpen, setPatientDropdownOpen] = useState(false);
  const [error, setError] = useState('');
  const [loadingRecord, setLoadingRecord] = useState(false);
  const [loadedRecord, setLoadedRecord] = useState<MedicalRecord | null>(null);
  const [form, setForm] = useState<RecordPayload>(() => createEmptyForm());
  const numericRecordId = recordId ? Number(recordId) : null;
  const isEdit = recordId !== undefined;
  const isRecordIdValid = numericRecordId !== null && Number.isFinite(numericRecordId);

  useEffect(() => {
    let cancelled = false;
    void api.doctors()
      .then((doctorList) => {
        if (!cancelled) {
          setDoctors(doctorList);
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : '医生列表加载失败');
        }
      });

    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    const timeoutId = window.setTimeout(() => {
      void api.patients(patientQuery, '', 0, 20)
        .then((patientPage) => {
          setPatients(patientPage.content);
        })
        .catch(() => {
          setPatients([]);
        });
    }, 200);

    return () => window.clearTimeout(timeoutId);
  }, [patientQuery]);

  useEffect(() => {
    if (isEdit || !user?.id) {
      return;
    }
    setForm((current) => (current.doctorId ? current : { ...current, doctorId: user.id }));
  }, [isEdit, user?.id]);

  useEffect(() => {
    if (isEdit) {
      return;
    }
    setLoadedRecord(null);
    setPatientDropdownOpen(false);
    setPatientQuery('');
    setForm(createEmptyForm(user?.id ?? 0));
    setError('');
  }, [isEdit, user?.id]);

  useEffect(() => {
    if (!isEdit) {
      return;
    }
    if (!isRecordIdValid) {
      setLoadingRecord(false);
      setLoadedRecord(null);
      setError('病历编号无效');
      return;
    }

    let cancelled = false;
    setLoadingRecord(true);
    setError('');
    void api.record(numericRecordId)
      .then((record) => {
        if (cancelled) {
          return;
        }
        setLoadedRecord(record);
        setPatientQuery(record.patientName);
        setPatientDropdownOpen(false);
        setForm(toRecordPayload(record));
      })
      .catch((err) => {
        if (!cancelled) {
          setLoadedRecord(null);
          setError(err instanceof Error ? err.message : '病历加载失败');
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoadingRecord(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [isEdit, isRecordIdValid, numericRecordId]);

  const doctorOptions = useMemo(() => {
    if (!user || doctors.some((doctor) => doctor.id === user.id)) {
      return doctors;
    }
    return [user, ...doctors];
  }, [doctors, user]);

  const selectPatient = (patient: Patient) => {
    setPatientQuery(patient.name);
    setPatientDropdownOpen(false);
    setForm((current) => ({ ...current, patientId: patient.id }));
  };

  const resetForm = () => {
    setError('');
    setPatientDropdownOpen(false);
    if (loadedRecord) {
      setPatientQuery(loadedRecord.patientName);
      setForm(toRecordPayload(loadedRecord));
      return;
    }
    setPatientQuery('');
    setForm(createEmptyForm(user?.id ?? 0));
  };

  const submit = async (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError('');
    if (isEdit && !isRecordIdValid) {
      setError('病历编号无效');
      return;
    }
    if (!form.patientId) {
      setError('请选择患者姓名');
      return;
    }
    if (!form.doctorId) {
      setError('请选择医生');
      return;
    }
    try {
      if (isEdit && numericRecordId !== null) {
        await api.updateRecord(numericRecordId, form);
        navigate(`/records/${numericRecordId}`);
      } else {
        await api.createRecord(form);
        navigate('/records');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : isEdit ? '保存失败' : '提交失败');
    }
  };

  const action = isEdit && isRecordIdValid
    ? <button type="button" className="btn-secondary" onClick={() => navigate(`/records/${numericRecordId}`)}><Eye size={20} />查看当前病历</button>
    : <button type="button" className="btn-secondary" onClick={() => navigate('/records')}>查看历史病历</button>;

  return (
    <form onSubmit={submit}>
      <PageHeader
        title={isEdit ? '编辑病历' : '录入新病历'}
        description={isEdit ? '更新既有病历内容并保留临床字段的一致性。' : '请按照临床标准如实填写患者诊疗信息。'}
        action={action}
      />
      {loadingRecord && <div className="panel mb-8 text-sm font-semibold text-muted">正在加载病历内容...</div>}
      <div className="panel mb-8 grid gap-6 md:grid-cols-3">
        <VisitDateTimeField
          id="visit-datetime"
          label="就诊时间"
          visitDate={form.visitDate}
          visitTime={form.visitTime}
          onChange={({ visitDate, visitTime }) => setForm({ ...form, visitDate, visitTime })}
          required
        />
        <div className="relative">
          <label htmlFor="patient-name"><span className="form-label required">姓名</span></label>
          <input
            id="patient-name"
            className="input-field"
            value={patientQuery}
            onBlur={() => window.setTimeout(() => setPatientDropdownOpen(false), 120)}
            onChange={(event) => {
              setPatientQuery(event.target.value);
              setPatientDropdownOpen(true);
              setForm((current) => ({ ...current, patientId: 0 }));
            }}
            onFocus={() => setPatientDropdownOpen(true)}
            placeholder="输入姓名搜索患者"
            required
          />
          {patientDropdownOpen && (
            <div className="absolute z-20 mt-2 max-h-64 w-full overflow-y-auto rounded-lg border border-outline bg-white p-2 shadow-lg">
              {patients.length > 0 ? patients.map((patient) => (
                <button
                  type="button"
                  className="w-full rounded-md px-3 py-2 text-left text-sm font-semibold text-ink hover:bg-surface-low"
                  key={patient.id}
                  onMouseDown={(event) => {
                    event.preventDefault();
                    selectPatient(patient);
                  }}
                >
                  <span>{patient.name}</span>
                  <span className="ml-2 text-xs text-muted">{patient.gender === 'FEMALE' ? '女' : patient.gender === 'MALE' ? '男' : '其他'} · {patient.age}岁 · {patient.team}</span>
                </button>
              )) : (
                <div className="px-3 py-2 text-sm font-semibold text-muted">未找到匹配患者</div>
              )}
            </div>
          )}
        </div>
        <label><span className="form-label required">医生</span><select className="input-field" value={form.doctorId} onChange={(e) => setForm({ ...form, doctorId: Number(e.target.value) })}><option value={0} disabled>请选择医生</option>{doctorOptions.map((doctor) => <option key={doctor.id} value={doctor.id}>{doctor.name}{doctor.title ? ` (${doctor.title})` : ''}</option>)}</select></label>
      </div>

      <div className="grid gap-8 xl:grid-cols-[1fr_360px]">
        <div className="space-y-8">
          <section className="record-section blue">
            <h2>临床评估</h2>
            <label><span className="form-label">身体状况/主诉</span><textarea className="input-field min-h-28" value={form.complaint} onChange={(e) => setForm({ ...form, complaint: e.target.value })} placeholder="描述患者当前的主要症状、发病时间及演变过程..." /></label>
            <label><span className="form-label">查体</span><textarea className="input-field min-h-28" value={form.examination} onChange={(e) => setForm({ ...form, examination: e.target.value })} placeholder="记录生命体征、专科检查等体格检查结果..." /></label>
            <label><span className="form-label">诊断</span><textarea className="input-field min-h-20" required value={form.diagnosis} onChange={(e) => setForm({ ...form, diagnosis: e.target.value })} placeholder="填写初步诊断或确诊结论..." /></label>
          </section>
          <section className="record-section slate">
            <h2>治疗方案与预后</h2>
            <label><span className="form-label">处置</span><textarea className="input-field min-h-28" value={form.treatment} onChange={(e) => setForm({ ...form, treatment: e.target.value })} placeholder="记录采取的医疗措施、手术、护理要求等..." /></label>
            <label><span className="form-label">预后</span><textarea className="input-field min-h-24" value={form.prognosis} onChange={(e) => setForm({ ...form, prognosis: e.target.value })} placeholder="评估患者预后情况及风险点..." /></label>
          </section>
        </div>
        <aside className="space-y-6">
          <div className="side-card">
            <h3>备注</h3>
            <textarea className="input-field min-h-32" value={form.notes} onChange={(e) => setForm({ ...form, notes: e.target.value })} placeholder="其他需要补充的临床细节、过敏史提醒等..." />
          </div>
        </aside>
      </div>
      {error && <div className="mt-6 rounded-lg bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">{error}</div>}
      <div className="mt-10 flex justify-end gap-4 border-t border-outline/60 pt-8">
        <button type="button" className="btn-secondary" onClick={resetForm}>重置</button>
        <button className="btn-primary"><Save size={20} />{isEdit ? '保存修改' : '提交档案'}</button>
      </div>
    </form>
  );
}
