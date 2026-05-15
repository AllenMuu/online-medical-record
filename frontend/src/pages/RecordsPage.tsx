import { Download, Eye, Filter, Pencil, Plus } from 'lucide-react';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api';
import { PageHeader } from '../components/PageHeader';
import type { MedicalRecord, Page, User } from '../types';

const emptyPage: Page<MedicalRecord> = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 10 };

export function RecordsPage() {
  const navigate = useNavigate();
  const [records, setRecords] = useState<Page<MedicalRecord>>(emptyPage);
  const [doctors, setDoctors] = useState<User[]>([]);
  const [query, setQuery] = useState('');
  const [doctorId, setDoctorId] = useState('');

  const params = useMemo(() => {
    const next = new URLSearchParams({ page: '0', size: '10' });
    if (query) next.set('query', query);
    if (doctorId) next.set('doctorId', doctorId);
    return next;
  }, [query, doctorId]);

  const load = useCallback(async () => setRecords(await api.records(params)), [params]);

  useEffect(() => {
    void api.doctors().then(setDoctors);
    void load();
  }, [load]);

  return (
    <>
      <PageHeader
        title="历史病历列表"
        description="查看、检索及导出系统内所有既往临床诊断记录。"
        action={
          <div className="flex gap-4">
            <button className="btn-secondary"><Download size={19} />导出报表</button>
            <button className="btn-primary" onClick={() => navigate('/records/new')}><Plus size={21} />新建记录</button>
          </div>
        }
      />
      <div className="panel mb-8 grid gap-6 md:grid-cols-[1fr_1fr_180px]">
        <label><span className="form-label">姓名搜索</span><input className="input-field" value={query} onChange={(e) => setQuery(e.target.value)} placeholder="输入患者姓名..." /></label>
        <label><span className="form-label">医生筛选</span><select className="input-field" value={doctorId} onChange={(e) => setDoctorId(e.target.value)}><option value="">全部医生</option>{doctors.map((doctor) => <option key={doctor.id} value={doctor.id}>{doctor.name}</option>)}</select></label>
        <button className="btn-primary self-end justify-center" onClick={() => void load()}><Filter size={21} />应用筛选</button>
      </div>
      <div className="table-card">
        <div className="table-grid grid-cols-[1.2fr_1.4fr_2fr_1.2fr_1fr_1fr] bg-surface-low font-bold text-muted">
          <div>就诊日期</div><div>姓名</div><div>诊断</div><div>医生</div><div>状态</div><div className="text-right">操作</div>
        </div>
        {records.content.map((record) => (
          <div key={record.id} className="table-grid grid-cols-[1.2fr_1.4fr_2fr_1.2fr_1fr_1fr]">
            <div><div className="font-semibold">{record.visitDate}</div><div className="text-xs text-muted">{record.visitTime}</div></div>
            <div><div className="font-headline font-extrabold">{record.patientName}</div><div className="text-xs text-muted">{record.patientGender === 'FEMALE' ? '女' : '男'} · {record.patientAge}岁</div></div>
            <div>{record.diagnosis}</div>
            <div>{record.doctorName}</div>
            <div><span className="badge">{record.status === 'COMPLETED' ? '已完成' : '进行中'}</span></div>
            <div className="flex justify-end gap-5 text-blue-300"><Eye size={21} /><Pencil size={21} /></div>
          </div>
        ))}
        <div className="flex items-center justify-between bg-surface-low px-8 py-5 text-sm font-semibold text-muted">
          <span>共 {records.totalElements} 条记录，显示 {records.content.length} 条</span>
          <span className="rounded-md bg-primary px-4 py-2 text-white">1</span>
        </div>
      </div>
    </>
  );
}
