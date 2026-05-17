import { Pencil, Search, Trash2, UserPlus } from 'lucide-react';
import { type SubmitEvent, useCallback, useEffect, useState } from 'react';
import { api, type PatientPayload } from '../api';
import { PageHeader } from '../components/PageHeader';
import { formatDateTime, genderLabel } from '../format';
import type { Gender, Page, Patient } from '../types';

const emptyPage: Page<Patient> = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 10 };
type EditingPatient = Patient | 'new' | null;
type PatientFormState = {
  name: string;
  gender: Gender | '';
  age: string;
  team: string;
  phone: string;
  birthDate: string;
  summary: string;
};

export function PatientsPage() {
  const [nameQuery, setNameQuery] = useState('');
  const [teamQuery, setTeamQuery] = useState('');
  const [page, setPage] = useState<Page<Patient>>(emptyPage);
  const [editingPatient, setEditingPatient] = useState<EditingPatient>(null);
  const [error, setError] = useState('');

  const load = useCallback(
    async () => setPage(await api.patients(nameQuery, teamQuery, 0, 10)),
    [nameQuery, teamQuery],
  );

  useEffect(() => {
    void load();
  }, [load]);

  const handleDelete = async (patient: Patient) => {
    if (!window.confirm(`确认删除患者“${patient.name}”吗？`)) {
      return;
    }
    setError('');
    try {
      await api.deletePatient(patient.id);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : '删除失败');
    }
  };

  return (
    <>
      <PageHeader
        title="患者管理"
        description="管理并检索系统内的所有登记患者信息。"
        action={
          <button className="btn-primary" onClick={() => setEditingPatient('new')}>
            <UserPlus size={22} />
            新增患者
          </button>
        }
      />

      <div className="panel mb-8 grid gap-6 md:grid-cols-[1fr_1fr_180px]">
        <label>
          <span className="form-label">姓名搜索</span>
          <input
            className="input-field"
            value={nameQuery}
            onChange={(e) => setNameQuery(e.target.value)}
            placeholder="输入患者姓名关键词..."
          />
        </label>
        <label>
          <span className="form-label">所属队伍搜索</span>
          <input
            className="input-field"
            value={teamQuery}
            onChange={(e) => setTeamQuery(e.target.value)}
            placeholder="输入所属队伍关键词..."
          />
        </label>
        <button className="btn-primary self-end justify-center" onClick={() => void load()}>
          <Search size={22} />
          搜索
        </button>
      </div>
      {error && <div className="mb-6 rounded-lg bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">{error}</div>}

      <div className="table-card">
        <div className="table-grid grid-cols-[1.7fr_0.8fr_0.7fr_1.4fr_1.2fr_1.2fr_0.8fr] bg-surface-low font-bold text-muted">
          <div>姓名</div>
          <div>性别</div>
          <div>年龄</div>
          <div>所属队伍</div>
          <div>创建时间</div>
          <div>更新时间</div>
          <div className="text-right">操作</div>
        </div>
        {page.content.map((patient) => (
          <div key={patient.id} className="table-grid grid-cols-[1.7fr_0.8fr_0.7fr_1.4fr_1.2fr_1.2fr_0.8fr]">
            <div className="flex items-center gap-4">
              <div className={`avatar-chip ${patient.gender === 'FEMALE' ? 'bg-pink-100 text-pink-600' : 'bg-blue-100 text-blue-700'}`}>
                {patient.name.slice(0, 1)}
              </div>
              <div className="font-headline font-extrabold">{patient.name}</div>
            </div>
            <div>{genderLabel(patient.gender)}</div>
            <div>{patient.age}</div>
            <div><span className="badge">{patient.team}</span></div>
            <div className="text-xs font-semibold text-muted">{formatDateTime(patient.createdAt)}</div>
            <div className="text-xs font-semibold text-muted">{formatDateTime(patient.updatedAt)}</div>
            <div className="flex justify-end gap-4 text-slate-400">
              <button
                type="button"
                className="text-slate-400 transition hover:text-blue-600"
                aria-label={`编辑患者 ${patient.name}`}
                onClick={() => setEditingPatient(patient)}
              >
                <Pencil size={21} />
              </button>
              <button
                type="button"
                className="text-slate-400 transition hover:text-red-600"
                aria-label={`删除患者 ${patient.name}`}
                onClick={() => void handleDelete(patient)}
              >
                <Trash2 size={21} />
              </button>
            </div>
          </div>
        ))}
        <div className="flex items-center justify-between bg-surface-low px-8 py-5 text-sm font-semibold text-muted">
          <span>显示 1 到 {page.content.length} 条，共 {page.totalElements} 条记录</span>
          <span className="rounded-md bg-primary px-4 py-2 text-white">1</span>
        </div>
      </div>

      {editingPatient && (
        <PatientModal
          patient={editingPatient === 'new' ? undefined : editingPatient}
          onClose={() => setEditingPatient(null)}
          onSaved={async () => {
            await load();
            setEditingPatient(null);
          }}
        />
      )}
    </>
  );
}

function PatientModal({ patient, onClose, onSaved }: { patient?: Patient; onClose: () => void; onSaved: () => Promise<void> }) {
  const [form, setForm] = useState<PatientFormState>({
    name: patient?.name ?? '',
    gender: patient?.gender ?? '',
    age: patient ? String(patient.age) : '',
    team: patient?.team ?? '',
    phone: patient?.phone ?? '',
    birthDate: patient?.birthDate ?? '',
    summary: patient?.summary ?? '',
  });
  const [error, setError] = useState('');
  const isEdit = Boolean(patient);

  const submit = async (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError('');
    const payload: PatientPayload = {
      name: form.name.trim(),
      gender: form.gender as Gender,
      age: Number(form.age),
      team: form.team.trim(),
      phone: form.phone.trim() || undefined,
      birthDate: form.birthDate || undefined,
      summary: form.summary.trim() || undefined,
    };
    try {
      if (patient) {
        await api.updatePatient(patient.id, payload);
      } else {
        await api.createPatient(payload);
      }
      await onSaved();
    } catch (err) {
      setError(err instanceof Error ? err.message : '保存失败');
    }
  };

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-slate-900/30 p-5 backdrop-blur-sm">
      <form className="w-full max-w-3xl rounded-xl bg-white p-8 shadow-ambient" onSubmit={submit}>
        <div className="mb-8">
          <h2 className="font-headline text-2xl font-extrabold">{isEdit ? '编辑患者档案' : '新增患者档案'}</h2>
          <p className="mt-1 text-sm font-semibold text-muted">请确保输入真实的临床基本信息以建立医疗记录</p>
        </div>
        <div className="grid gap-5 md:grid-cols-2">
          <label><span className="form-label required">姓名</span><input className="input-field" required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} /></label>
          <label><span className="form-label required">年龄</span><input className="input-field" type="number" min="0" required value={form.age} onChange={(e) => setForm({ ...form, age: e.target.value })} /></label>
          <label><span className="form-label required">性别</span><select className="input-field" required value={form.gender} onChange={(e) => setForm({ ...form, gender: e.target.value as Gender | '' })}><option value="">请选择性别</option><option value="MALE">男</option><option value="FEMALE">女</option><option value="OTHER">其他</option></select></label>
          <label><span className="form-label required">所属队伍</span><input className="input-field" required value={form.team} onChange={(e) => setForm({ ...form, team: e.target.value })} /></label>
          <label><span className="form-label">联系电话</span><input className="input-field" value={form.phone ?? ''} onChange={(e) => setForm({ ...form, phone: e.target.value })} /></label>
          <label><span className="form-label">出生日期</span><input className="input-field" type="date" value={form.birthDate ?? ''} onChange={(e) => setForm({ ...form, birthDate: e.target.value })} /></label>
          <label className="md:col-span-2"><span className="form-label">临床摘要</span><textarea className="input-field min-h-28" value={form.summary} onChange={(e) => setForm({ ...form, summary: e.target.value })} /></label>
        </div>
        {error && <div className="mt-5 rounded-lg bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">{error}</div>}
        <div className="mt-8 flex justify-end gap-4"><button type="button" className="btn-secondary" onClick={onClose}>取消</button><button className="btn-primary">{isEdit ? '保存修改' : '保存患者'}</button></div>
      </form>
    </div>
  );
}
