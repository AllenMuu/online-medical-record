import { Camera } from 'lucide-react';
import { type ChangeEvent, type FormEvent, useRef, useState } from 'react';
import { useAuth } from '../AuthContext';
import { api } from '../api';
import { PageHeader } from '../components/PageHeader';
import { useNavigate } from 'react-router-dom';

export function SettingsPage() {
  const { user, clear, refresh } = useAuth();
  const navigate = useNavigate();
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [avatarSubmitting, setAvatarSubmitting] = useState(false);

  const avatarSrc =
    user?.avatarUrl ||
    'https://lh3.googleusercontent.com/aida-public/AB6AXuA2SvgHuI_MCXg-jIRL2kNz7rC6jxnIRaNy6X3Dm-tgwFEkR9DAFzCSMVAy75wC7P_yBkFtl2KY4GTcJIMqEQ-Y2ZFT_Zqhi9lcQhI_fk4LqrUAKKaZOgywTk1UiHoieAzqd0p9l06OjN09jU4pvY6t0r3UQFQ0HlKQ7-RsLDsMImLSvPVOGqPz4rKGB1rntPS688oX0LNnsrAOms6M3-A_s4S9JcgLGJyO69GUHNyzMhuRN6_0BXtyu-Iq9y1h3IxqrZ8-NH45cxs';

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError('');
    setMessage('');

    if (newPassword !== confirmPassword) {
      setError('两次输入的新密码不一致');
      return;
    }

    setSubmitting(true);
    try {
      const result = await api.changePassword(currentPassword, newPassword);
      setMessage(result.message);
      clear();
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
      navigate('/login', { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : '密码修改失败');
    } finally {
      setSubmitting(false);
    }
  };

  const onAvatarChange = async (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    event.target.value = '';
    if (!file) {
      return;
    }

    setError('');
    setMessage('');
    setAvatarSubmitting(true);
    try {
      await api.updateMyAvatar(file);
      await refresh();
      setMessage('头像已更新');
    } catch (err) {
      setError(err instanceof Error ? err.message : '头像更新失败');
    } finally {
      setAvatarSubmitting(false);
    }
  };

  return (
    <>
      <PageHeader title="设置" description="查看当前账号信息，并更新自己的登录密码。" />
      <div className="grid gap-8 xl:grid-cols-[360px_1fr]">
        <div className="panel">
          <div className="flex flex-col items-center border-b border-outline/60 pb-6">
            <img className="h-28 w-28 rounded-3xl object-cover ring-4 ring-blue-100" alt="当前用户头像" src={avatarSrc} />
            <button
              type="button"
              className="btn-secondary mt-4 w-full justify-center px-4 py-2"
              disabled={avatarSubmitting}
              onClick={() => fileInputRef.current?.click()}
            >
              <Camera size={18} />
              {avatarSubmitting ? '上传中...' : '更换头像'}
            </button>
            <input
              ref={fileInputRef}
              className="hidden"
              type="file"
              accept="image/png,image/jpeg,image/webp"
              onChange={onAvatarChange}
            />
          </div>
          <div className="mt-6 grid gap-4 text-sm font-semibold text-slate-700">
            <div className="flex justify-between"><span>当前账号</span><strong>{user?.email}</strong></div>
            <div className="flex justify-between"><span>角色</span><strong>{user?.role === 'ADMIN' ? '管理员' : '医生'}</strong></div>
            <div className="flex justify-between"><span>系统状态</span><strong className="text-green-700">正常</strong></div>
          </div>
        </div>
        <form className="panel max-w-2xl" onSubmit={submit}>
          <div className="mb-6">
            <h2 className="font-headline text-2xl font-extrabold text-ink">修改密码</h2>
            <p className="mt-2 text-sm font-semibold text-muted">请输入当前密码，并设置一个不少于 8 位的新密码。</p>
          </div>
          <label>
            <span className="form-label">当前密码</span>
            <input
              className="input-field"
              type="password"
              required
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              placeholder="请输入当前密码"
            />
          </label>
          <label>
            <span className="form-label">新密码</span>
            <input
              className="input-field"
              type="password"
              required
              minLength={8}
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              placeholder="请输入新密码"
            />
          </label>
          <label>
            <span className="form-label">确认新密码</span>
            <input
              className="input-field"
              type="password"
              required
              minLength={8}
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="请再次输入新密码"
            />
          </label>
          {error && <div className="rounded-lg bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">{error}</div>}
          {message && <div className="rounded-lg bg-blue-50 px-4 py-3 text-sm font-semibold text-blue-700">{message}</div>}
          <button className="btn-primary mt-4 justify-center" disabled={submitting}>
            {submitting ? '提交中...' : '更新密码'}
          </button>
        </form>
      </div>
    </>
  );
}
