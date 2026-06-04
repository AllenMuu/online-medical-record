import { ArrowRight, Eye, EyeOff, Lock, UserRound } from 'lucide-react';
import { type SubmitEvent, useState } from 'react';
import { useAuth } from '../AuthContext';
import { api } from '../api';

export function LoginPage() {
  const { login } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [resetToken, setResetToken] = useState('');
  const [showPassword, setShowPassword] = useState(false);

  const submit = async (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError('');
    try {
      await login(email, password);
    } catch (err) {
      setError(err instanceof Error ? err.message : '登录失败');
    }
  };

  const requestReset = async () => {
    setError('');
    try {
      const result = await api.requestReset(email);
      setResetToken(result.resetToken || result.message);
    } catch (err) {
      setError(err instanceof Error ? err.message : '重置失败');
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-white via-slate-50 to-blue-50 font-body text-ink">
      <div className="flex justify-center pt-12">
        <div className="flex items-center gap-3">
          <div className="grid h-9 w-9 place-items-center rounded-md bg-blue-800 text-white">
            <Lock size={22} />
          </div>
          <span className="font-headline text-2xl font-extrabold tracking-tight text-blue-800">Ice 临床管理系统</span>
        </div>
      </div>
      <div className="grid min-h-[calc(100vh-180px)] place-items-center px-5 py-12">
        <form className="w-full max-w-[520px] rounded-xl bg-white px-10 py-12 shadow-ambient" onSubmit={submit}>
          <div className="mb-10 text-center">
            <h1 className="font-headline text-3xl font-bold">欢迎登录</h1>
            <p className="mt-3 text-sm font-semibold text-muted">请输入您的凭据以访问临床后台</p>
          </div>
          <label className="form-label">账号 / 手机号</label>
          <div className="relative mb-6">
            <UserRound className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" />
            <input className="input-field pl-12" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="请输入您的账号" />
          </div>
          <div className="mb-2 flex items-center justify-between">
            <label className="form-label mb-0">密码</label>
            <button type="button" className="text-xs font-bold text-primary" onClick={requestReset}>
              忘记密码?
            </button>
          </div>
          <div className="relative mb-5">
            <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              className="input-field px-12"
              type={showPassword ? 'text' : 'password'}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="请输入登录密码"
            />
            <button
              type="button"
              className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-700"
              onClick={() => setShowPassword((v) => !v)}
              aria-label={showPassword ? '隐藏密码' : '显示密码'}
              aria-pressed={showPassword}
            >
              {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
            </button>
          </div>
          <label className="mb-8 flex items-center gap-3 text-sm font-semibold text-slate-700">
            <input type="checkbox" className="h-5 w-5 rounded border-outline" />
            记住账号
          </label>
          {error && <div className="mb-4 rounded-lg bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">{error}</div>}
          {resetToken && <div className="mb-4 rounded-lg bg-blue-50 px-4 py-3 text-xs font-semibold text-blue-700">本地重置令牌：{resetToken}</div>}
          <button className="btn-primary h-14 w-full justify-center text-base">
            立即登录
            <ArrowRight size={20} />
          </button>
        </form>
      </div>
      <footer className="pb-8 text-center text-xs font-semibold text-slate-400">隐私政策 | 服务条款 | 联系支持 | © 2026 Ice 临床管理系统</footer>
    </div>
  );
}
