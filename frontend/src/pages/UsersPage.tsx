import { Plus, ShieldCheck } from 'lucide-react';
import { type SubmitEvent, useEffect, useState } from 'react';
import { api, type CreateUserPayload } from '../api';
import { PageHeader } from '../components/PageHeader';
import { formatDateTime } from '../format';
import type { User } from '../types';

export function UsersPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [form, setForm] = useState<CreateUserPayload>({ name: '', email: '', password: 'Doctor123!', role: 'DOCTOR', title: '主治医师', department: '全科门诊' });
  const [error, setError] = useState('');

  const load = async () => setUsers(await api.users());

  useEffect(() => {
    void load();
  }, []);

  const submit = async (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError('');
    try {
      await api.createUser(form);
      setForm({ ...form, name: '', email: '' });
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : '创建失败');
    }
  };

  return (
    <>
      <PageHeader title="医生账号" description="管理员创建和停用医生账号。" />
      <div className="grid gap-8 xl:grid-cols-[420px_1fr]">
        <form className="panel" onSubmit={submit}>
          <div className="mb-6 flex items-center gap-3 font-headline text-xl font-extrabold"><ShieldCheck className="text-primary" />新增医生</div>
          <label><span className="form-label">姓名</span><input className="input-field" required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} /></label>
          <label><span className="form-label">邮箱</span><input className="input-field" type="email" required value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} /></label>
          <label><span className="form-label">初始密码</span><input className="input-field" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} /></label>
          <label><span className="form-label">职称</span><input className="input-field" value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} /></label>
          <label><span className="form-label">科室</span><input className="input-field" value={form.department} onChange={(e) => setForm({ ...form, department: e.target.value })} /></label>
          {error && <div className="rounded-lg bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">{error}</div>}
          <button className="btn-primary mt-4 w-full justify-center"><Plus size={19} />创建医生账号</button>
        </form>
        <div className="table-card">
          <div className="table-grid grid-cols-[1.2fr_1.8fr_0.9fr_0.8fr_1.1fr_1.1fr] bg-surface-low font-bold text-muted"><div>姓名</div><div>邮箱</div><div>角色</div><div>状态</div><div>创建时间</div><div>更新时间</div></div>
          {users.map((user) => (
            <div className="table-grid grid-cols-[1.2fr_1.8fr_0.9fr_0.8fr_1.1fr_1.1fr]" key={user.id}>
              <div className="font-headline font-extrabold">{user.name}</div>
              <div>{user.email}</div>
              <div>{user.role === 'ADMIN' ? '管理员' : user.title || '医生'}</div>
              <div><span className="badge">{user.active ? '启用' : '停用'}</span></div>
              <div className="text-xs font-semibold text-muted">{formatDateTime(user.createdAt)}</div>
              <div className="text-xs font-semibold text-muted">{formatDateTime(user.updatedAt)}</div>
            </div>
          ))}
        </div>
      </div>
    </>
  );
}
