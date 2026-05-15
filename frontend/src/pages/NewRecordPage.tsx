import { Calendar, Plus, Save } from 'lucide-react';
import { FormEvent, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, type RecordPayload } from '../api';
import { PageHeader } from '../components/PageHeader';
import type { Patient, User } from '../types';

export function NewRecordPage() {
  const navigate = useNavigate();
  const [patients, setPatients] = useState<Patient[]>([]);
  const [doctors, setDoctors] = useState<User[]>([]);
  const [error, setError] = useState('');
  const [form, setForm] = useState<RecordPayload>({
    patientId: 0,
    doctorId: 0,
    visitDate: '2023-11-20',
    visitTime: '10:30',
    diagnosis: '',
    complaint: '',
    examination: '',
    treatment: '',
    prognosis: '',
    notes: '',
    status: 'COMPLETED',
    medications: [{ name: '阿莫西林', dosage: '0.5g tid 口服' }],
  });

  useEffect(() => {
    void Promise.all([api.patients('', 0, 100), api.doctors()]).then(([patientPage, doctorList]) => {
      setPatients(patientPage.content);
      setDoctors(doctorList);
      setForm((current) => ({
        ...current,
        patientId: patientPage.content[0]?.id ?? 0,
        doctorId: doctorList[0]?.id ?? 0,
      }));
    });
  }, []);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError('');
    try {
      await api.createRecord(form);
      navigate('/records');
    } catch (err) {
      setError(err instanceof Error ? err.message : '提交失败');
    }
  };

  return (
    <form onSubmit={submit}>
      <PageHeader
        title="录入新病历"
        description="请按照临床标准如实填写患者诊疗信息。"
        action={<button type="button" className="btn-secondary" onClick={() => navigate('/records')}>查看历史草稿</button>}
      />
      <div className="panel mb-8 grid gap-6 md:grid-cols-3">
        <label><span className="form-label required">就诊日期</span><input className="input-field" type="date" value={form.visitDate} onChange={(e) => setForm({ ...form, visitDate: e.target.value })} /></label>
        <label><span className="form-label required">姓名</span><select className="input-field" value={form.patientId} onChange={(e) => setForm({ ...form, patientId: Number(e.target.value) })}>{patients.map((patient) => <option key={patient.id} value={patient.id}>{patient.name}</option>)}</select></label>
        <label><span className="form-label required">医生</span><select className="input-field" value={form.doctorId} onChange={(e) => setForm({ ...form, doctorId: Number(e.target.value) })}>{doctors.map((doctor) => <option key={doctor.id} value={doctor.id}>{doctor.name} ({doctor.title})</option>)}</select></label>
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
          <div className="side-card amber">
            <h3>用药情况</h3>
            {form.medications.map((med, index) => (
              <div className="rounded-lg bg-surface-low p-5" key={`${med.name}-${index}`}>
                <label><span className="form-label">药品名称</span><input className="input-field" value={med.name} onChange={(e) => {
                  const next = [...form.medications]; next[index] = { ...med, name: e.target.value }; setForm({ ...form, medications: next });
                }} /></label>
                <label><span className="form-label">用法用量</span><input className="input-field" value={med.dosage} onChange={(e) => {
                  const next = [...form.medications]; next[index] = { ...med, dosage: e.target.value }; setForm({ ...form, medications: next });
                }} /></label>
              </div>
            ))}
            <button type="button" className="btn-dashed" onClick={() => setForm({ ...form, medications: [...form.medications, { name: '', dosage: '' }] })}><Plus size={18} />添加用药</button>
          </div>
          <div className="side-card">
            <h3>备注</h3>
            <textarea className="input-field min-h-32" value={form.notes} onChange={(e) => setForm({ ...form, notes: e.target.value })} placeholder="其他需要补充的临床细节、过敏史提醒等..." />
          </div>
          <div className="rounded-xl bg-blue-50 p-6 text-blue-800">
            <div className="mb-2 flex items-center gap-2 font-headline font-extrabold"><Calendar size={20} />临床指南提醒</div>
            <p className="text-sm font-semibold leading-6">请确保诊断编码符合 ICD-10 标准。</p>
          </div>
        </aside>
      </div>
      {error && <div className="mt-6 rounded-lg bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">{error}</div>}
      <div className="mt-10 flex justify-end gap-4 border-t border-outline/60 pt-8">
        <button type="reset" className="btn-secondary">重置</button>
        <button className="btn-primary"><Save size={20} />提交档案</button>
      </div>
    </form>
  );
}
