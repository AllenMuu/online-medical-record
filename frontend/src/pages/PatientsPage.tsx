import { Pencil, Plus, Search, Trash2, UserPlus } from 'lucide-react';
import { FormEvent, useCallback, useEffect, useState } from 'react';
import { api, type PatientPayload } from '../api';
import { PageHeader } from '../components/PageHeader';
import type { Page, Patient } from '../types';

const emptyPage: Page<Patient> = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 10 };

export function PatientsPage() {
  const [query, setQuery] = useState('');
  const [page, setPage] = useState<Page<Patient>>(emptyPage);
  const [modalOpen, setModalOpen] = useState(false);

  const load = useCallback(async () => setPage(await api.patients(query, 0, 10)), [query]);

  useEffect(() => {
    void load();
  }, [load]);

  return (
    <>
      <PageHeader
        title="患者管理"
        description="管理并检索系统内的所有登记患者信息。"
        action={
          <button className="btn-primary" onClick={() => setModalOpen(true)}>
            <UserPlus size={22} />
            新增患者
          </button>
        }
      />

      <div className="panel mb-8 grid gap-6 md:grid-cols-[1fr_1fr_180px]">
        <label>
          <span className="form-label">姓名搜索</span>
          <input className="input-field" value={query} onChange={(e) => setQuery(e.target.value)} placeholder="输入患者姓名关键词..." />
        </label>
        <label>
          <span className="form-label">所属队伍搜索</span>
          <select className="input-field" defaultValue="">
            <option value="">全部队伍</option>
            <option>心脏内科 A组</option>
            <option>外科护理 2队</option>
            <option>急诊科 先锋组</option>
          </select>
        </label>
        <button className="btn-primary self-end justify-center" onClick={() => void load()}>
          <Search size={22} />
          搜索
        </button>
      </div>

      <div className="table-card">
        <div className="table-grid grid-cols-[2fr_1fr_1fr_2fr_1fr] bg-surface-low font-bold text-muted">
          <div>姓名</div>
          <div>性别</div>
          <div>年龄</div>
          <div>所属队伍</div>
          <div className="text-right">操作</div>
        </div>
        {page.content.map((patient) => (
          <div key={patient.id} className="table-grid grid-cols-[2fr_1fr_1fr_2fr_1fr]">
            <div className="flex items-center gap-4">
              <div className={`avatar-chip ${patient.gender === 'FEMALE' ? 'bg-pink-100 text-pink-600' : 'bg-blue-100 text-blue-700'}`}>
                {patient.name.slice(0, 1)}
              </div>
              <div className="font-headline font-extrabold">{patient.name}</div>
            </div>
            <div>{patient.gender === 'FEMALE' ? '女' : patient.gender === 'MALE' ? '男' : '其他'}</div>
            <div>{patient.age}</div>
            <div><span className="badge">{patient.team}</span></div>
            <div className="flex justify-end gap-4 text-slate-400">
              <Pencil size={21} />
              <Trash2 size={21} />
            </div>
          </div>
        ))}
        <div className="flex items-center justify-between bg-surface-low px-8 py-5 text-sm font-semibold text-muted">
          <span>显示 1 到 {page.content.length} 条，共 {page.totalElements} 条记录</span>
          <span className="rounded-md bg-primary px-4 py-2 text-white">1</span>
        </div>
      </div>

      <div className="mt-8 grid gap-6 md:grid-cols-3">
        <div className="rounded-xl bg-blue-600 p-8 text-white shadow-ambient">
          <div className="mb-12 inline-grid h-12 w-12 place-items-center rounded-lg bg-white/20"><Plus /></div>
          <h3 className="font-headline text-2xl font-extrabold">本月新增登记</h3>
          <p className="mt-3 text-blue-100">较上月增长了 12.5%，医疗服务量持续稳定提升。</p>
          <div className="mt-12 font-headline text-5xl font-extrabold">48 <span className="text-lg">人</span></div>
        </div>
        <div className="stat-card">平均周转率 <strong>4.2</strong><span> 天/人</span></div>
        <div className="stat-card">患者满意度 <strong>98%</strong><span className="ml-2 rounded bg-green-100 px-2 py-1 text-sm text-green-700">优秀</span></div>
      </div>

      {modalOpen && <PatientModal onClose={() => setModalOpen(false)} onSaved={() => void load()} />}
    </>
  );
}

function PatientModal({ onClose, onSaved }: { onClose: () => void; onSaved: () => void }) {
  const [form, setForm] = useState<PatientPayload>({ name: '', gender: 'MALE', age: 35, team: '心脏内科 A组', summary: '' });
  const [error, setError] = useState('');

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError('');
    try {
      await api.createPatient(form);
      onSaved();
      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : '保存失败');
    }
  };

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-slate-900/30 p-5 backdrop-blur-sm">
      <form className="w-full max-w-3xl rounded-xl bg-white p-8 shadow-ambient" onSubmit={submit}>
        <div className="mb-8">
          <h2 className="font-headline text-2xl font-extrabold">新增患者档案</h2>
          <p className="mt-1 text-sm font-semibold text-muted">请确保输入真实的临床基本信息以建立医疗记录</p>
        </div>
        <div className="grid gap-5 md:grid-cols-2">
          <label><span className="form-label">姓名</span><input className="input-field" required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} /></label>
          <label><span className="form-label">年龄</span><input className="input-field" type="number" value={form.age} onChange={(e) => setForm({ ...form, age: Number(e.target.value) })} /></label>
          <label><span className="form-label">性别</span><select className="input-field" value={form.gender} onChange={(e) => setForm({ ...form, gender: e.target.value })}><option value="MALE">男</option><option value="FEMALE">女</option><option value="OTHER">其他</option></select></label>
          <label><span className="form-label">所属队伍</span><input className="input-field" value={form.team} onChange={(e) => setForm({ ...form, team: e.target.value })} /></label>
          <label className="md:col-span-2"><span className="form-label">临床摘要</span><textarea className="input-field min-h-28" value={form.summary} onChange={(e) => setForm({ ...form, summary: e.target.value })} /></label>
        </div>
        {error && <div className="mt-5 rounded-lg bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">{error}</div>}
        <div className="mt-8 flex justify-end gap-4"><button type="button" className="btn-secondary" onClick={onClose}>取消</button><button className="btn-primary">保存患者</button></div>
      </form>
    </div>
  );
}
