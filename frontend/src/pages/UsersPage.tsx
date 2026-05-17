import { Pencil, PauseCircle, PlayCircle, Plus, Search, ShieldCheck, Trash2, X } from 'lucide-react';
import { type SubmitEvent, useCallback, useEffect, useState } from 'react';
import { api, type CreateUserPayload } from '../api';
import { PageHeader } from '../components/PageHeader';
import { formatDateTime } from '../format';
import type { Page, User } from '../types';

const pageSize = 10;
const emptyPage: Page<User> = { content: [], totalElements: 0, totalPages: 0, number: 0, size: pageSize };

type UserFormState = {
  name: string;
  department: string;
};

export function UsersPage() {
  const [users, setUsers] = useState<Page<User>>(emptyPage);
  const [nameQuery, setNameQuery] = useState('');
  const [appliedNameQuery, setAppliedNameQuery] = useState('');
  const [form, setForm] = useState<CreateUserPayload>({ name: '', email: '', password: 'Doctor123!', role: 'DOCTOR', department: '' });
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [error, setError] = useState('');
  const [actionError, setActionError] = useState('');

  const load = useCallback(
    async (nextPage = 0, nextNameQuery = '') =>
      setUsers(
        await api.users({
          role: 'DOCTOR',
          nameQuery: nextNameQuery,
          page: nextPage,
          size: pageSize,
        }),
      ),
    [],
  );

  useEffect(() => {
    void load(0, '');
  }, [load]);

  const search = async () => {
    setActionError('');
    setAppliedNameQuery(nameQuery);
    await load(0, nameQuery);
  };

  const submit = async (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError('');
    try {
      await api.createUser(form);
      setForm({ ...form, name: '', email: '' });
      await load(0, appliedNameQuery);
    } catch (err) {
      setError(err instanceof Error ? err.message : '创建失败');
    }
  };

  const handleToggleActive = async (user: User) => {
    setActionError('');
    try {
      await api.updateUser(user.id, { active: !user.active });
      await load(users.number, appliedNameQuery);
    } catch (err) {
      setActionError(err instanceof Error ? err.message : '状态更新失败');
    }
  };

  const handleDelete = async (user: User) => {
    if (!window.confirm(`确认删除医生账号“${user.name}”吗？`)) {
      return;
    }
    setActionError('');
    try {
      await api.deleteUser(user.id);
      const fallbackPage = users.content.length === 1 && users.number > 0 ? users.number - 1 : users.number;
      await load(fallbackPage, appliedNameQuery);
    } catch (err) {
      setActionError(err instanceof Error ? err.message : '删除失败');
    }
  };

  const start = users.totalElements === 0 ? 0 : users.number * users.size + 1;
  const end = users.totalElements === 0 ? 0 : users.number * users.size + users.content.length;

  return (
    <>
      <PageHeader title="医生账号" description="管理员创建、检索、编辑、启停和删除医生账号。" />
      <div className="grid gap-8 xl:grid-cols-[420px_1fr]">
        <form className="panel" onSubmit={submit}>
          <div className="mb-6 flex items-center gap-3 font-headline text-xl font-extrabold">
            <ShieldCheck className="text-primary" />
            新增医生
          </div>
          <label>
            <span className="form-label">姓名</span>
            <input className="input-field" required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
          </label>
          <label>
            <span className="form-label">邮箱</span>
            <input className="input-field" type="email" required value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
          </label>
          <label>
            <span className="form-label">初始密码</span>
            <input className="input-field" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
          </label>
          <label>
            <span className="form-label">科室</span>
            <input className="input-field" value={form.department ?? ''} onChange={(e) => setForm({ ...form, department: e.target.value })} />
          </label>
          {error && <div className="rounded-lg bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">{error}</div>}
          <button className="btn-primary mt-4 w-full justify-center">
            <Plus size={19} />
            创建医生账号
          </button>
        </form>
        <div className="space-y-6">
          <div className="panel grid gap-4 md:grid-cols-[1fr_180px]">
            <label>
              <span className="form-label">姓名搜索</span>
              <input
                className="input-field"
                value={nameQuery}
                onChange={(e) => setNameQuery(e.target.value)}
                placeholder="输入医生姓名关键词..."
              />
            </label>
            <button type="button" className="btn-primary self-end justify-center" onClick={() => void search()}>
              <Search size={18} />
              搜索
            </button>
          </div>
          {actionError && <div className="rounded-lg bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">{actionError}</div>}
          <div className="table-card">
            <div className="table-grid grid-cols-[0.8fr_1.1fr_1.8fr_1.1fr_0.9fr_1fr_1.1fr] bg-surface-low font-bold text-muted">
              <div>账号 ID</div>
              <div>姓名</div>
              <div>邮箱</div>
              <div>科室</div>
              <div>状态</div>
              <div>更新时间</div>
              <div className="text-right">操作</div>
            </div>
            {users.content.map((user) => (
              <div className="table-grid grid-cols-[0.8fr_1.1fr_1.8fr_1.1fr_0.9fr_1fr_1.1fr]" key={user.id}>
                <div className="font-mono text-xs font-bold text-muted">{user.id}</div>
                <div className="font-headline font-extrabold">{user.name}</div>
                <div>{user.email}</div>
                <div>{user.department || '-'}</div>
                <div>
                  <span className="badge">{user.active ? '启用' : '停用'}</span>
                </div>
                <div className="text-xs font-semibold text-muted">{formatDateTime(user.updatedAt)}</div>
                <div className="flex justify-end gap-4 text-slate-400">
                  <button
                    type="button"
                    className="text-slate-400 transition hover:text-blue-600"
                    aria-label={`编辑医生 ${user.name}`}
                    onClick={() => setEditingUser(user)}
                  >
                    <Pencil size={21} />
                  </button>
                  <button
                    type="button"
                    className="text-slate-400 transition hover:text-blue-600"
                    aria-label={`${user.active ? '停用' : '启用'}医生 ${user.name}`}
                    onClick={() => void handleToggleActive(user)}
                  >
                    {user.active ? <PauseCircle size={21} /> : <PlayCircle size={21} />}
                  </button>
                  <button
                    type="button"
                    className="text-slate-400 transition hover:text-red-600"
                    aria-label={`删除医生 ${user.name}`}
                    onClick={() => void handleDelete(user)}
                  >
                    <Trash2 size={21} />
                  </button>
                </div>
              </div>
            ))}
            <div className="flex items-center justify-between gap-4 bg-surface-low px-8 py-5 text-sm font-semibold text-muted">
              <span>显示 {start} 到 {end} 条，共 {users.totalElements} 条记录</span>
              <div className="flex items-center gap-3">
                <button
                  type="button"
                  className="btn-secondary px-4 py-2"
                  disabled={users.number === 0}
                  onClick={() => {
                    const nextPage = Math.max(0, users.number - 1);
                    void load(nextPage, appliedNameQuery);
                  }}
                >
                  上一页
                </button>
                <span className="rounded-md bg-primary px-4 py-2 text-white">
                  {users.totalPages === 0 ? 0 : users.number + 1} / {users.totalPages}
                </span>
                <button
                  type="button"
                  className="btn-secondary px-4 py-2"
                  disabled={users.number + 1 >= users.totalPages}
                  onClick={() => {
                    const nextPage = users.number + 1;
                    void load(nextPage, appliedNameQuery);
                  }}
                >
                  下一页
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
      {editingUser && (
        <UserModal
          user={editingUser}
          onClose={() => setEditingUser(null)}
          onSaved={async () => {
            await load(users.number, appliedNameQuery);
            setEditingUser(null);
          }}
        />
      )}
    </>
  );
}

function UserModal({ user, onClose, onSaved }: { user: User; onClose: () => void; onSaved: () => Promise<void> }) {
  const [form, setForm] = useState<UserFormState>({
    name: user.name,
    department: user.department ?? '',
  });
  const [error, setError] = useState('');

  const submit = async (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError('');
    try {
      await api.updateUser(user.id, {
        name: form.name.trim(),
        department: form.department.trim(),
      });
      await onSaved();
    } catch (err) {
      setError(err instanceof Error ? err.message : '保存失败');
    }
  };

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-slate-900/30 p-5 backdrop-blur-sm">
      <form className="w-full max-w-2xl rounded-xl bg-white p-8 shadow-ambient" onSubmit={submit}>
        <div className="mb-8 flex items-start justify-between gap-4">
          <div>
            <h2 className="font-headline text-2xl font-extrabold">编辑医生账号</h2>
            <p className="mt-1 text-sm font-semibold text-muted">账号 ID：{user.id}</p>
          </div>
          <button type="button" className="text-slate-400 transition hover:text-slate-700" aria-label="关闭编辑窗口" onClick={onClose}>
            <X size={22} />
          </button>
        </div>
        <div className="grid gap-5 md:grid-cols-2">
          <label>
            <span className="form-label required">姓名</span>
            <input className="input-field" required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
          </label>
          <label>
            <span className="form-label">科室</span>
            <input className="input-field" value={form.department} onChange={(e) => setForm({ ...form, department: e.target.value })} />
          </label>
        </div>
        {error && <div className="mt-5 rounded-lg bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">{error}</div>}
        <div className="mt-8 flex justify-end gap-4">
          <button type="button" className="btn-secondary" onClick={onClose}>取消</button>
          <button className="btn-primary">保存修改</button>
        </div>
      </form>
    </div>
  );
}
