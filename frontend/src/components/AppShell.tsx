import { CalendarDays, FileText, Gauge, LogOut, Plus, Settings, Shield, Users } from 'lucide-react';
import type { ReactNode } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../AuthContext';

const navItems = [
  { to: '/dashboard', label: '仪表盘', icon: Gauge },
  { to: '/patients', label: '患者管理', icon: Users },
  { to: '/records', label: '病历记录', icon: FileText },
  { to: '/schedule', label: '排班计划', icon: CalendarDays },
  { to: '/settings', label: '设置', icon: Settings },
];

export function AppShell({ children }: { children: ReactNode }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const isAdmin = user?.role === 'ADMIN';

  return (
    <div className="min-h-screen bg-surface text-ink">
      <aside className="fixed inset-y-0 left-0 z-20 hidden w-[280px] border-r border-outline/50 bg-white/75 px-7 py-8 backdrop-blur md:flex md:flex-col">
        <div className="mb-14 flex items-center gap-4">
          <div className="grid h-12 w-12 place-items-center rounded-xl bg-primary text-white shadow-ambient">
            <FileText size={28} />
          </div>
          <div>
            <div className="font-headline text-lg font-extrabold text-blue-800">临床精准系统</div>
            <div className="text-xs font-semibold text-muted">数字化医疗专家</div>
          </div>
        </div>

        <nav className="space-y-2">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                [
                  'flex items-center gap-3 rounded-xl px-4 py-3 text-sm font-semibold transition',
                  isActive ? 'border-l-4 border-primary bg-primary-soft text-blue-700' : 'text-slate-600 hover:bg-slate-100',
                ].join(' ')
              }
            >
              <item.icon size={22} />
              {item.label}
            </NavLink>
          ))}
          {isAdmin && (
            <NavLink
              to="/admin/users"
              className={({ isActive }) =>
                [
                  'flex items-center gap-3 rounded-xl px-4 py-3 text-sm font-semibold transition',
                  isActive ? 'border-l-4 border-primary bg-primary-soft text-blue-700' : 'text-slate-600 hover:bg-slate-100',
                ].join(' ')
              }
            >
              <Shield size={22} />
              医生账号
            </NavLink>
          )}
        </nav>

        <button className="btn-primary mt-auto w-full" onClick={() => navigate('/records/new')}>
          <Plus size={20} />
          快速录入病历
        </button>
      </aside>

      <div className="md:pl-[280px]">
        <header className="sticky top-0 z-10 flex h-20 items-center justify-end border-b border-outline/50 bg-white/75 px-5 backdrop-blur md:px-10">
          <div className="flex items-center gap-5">
            <button
              className="hidden rounded-lg p-2 text-slate-500 hover:bg-slate-100 md:block"
              aria-label="退出登录"
              onClick={() => void logout()}
            >
              <LogOut size={21} />
            </button>
            <button
              type="button"
              className="flex items-center gap-3 border-l border-outline/70 pl-5 text-left transition hover:opacity-85 focus:outline-none focus:ring-2 focus:ring-blue-100"
              aria-label="打开设置"
              onClick={() => navigate('/settings')}
            >
              <div className="text-right">
                <div className="font-headline text-sm font-extrabold">{user?.name}</div>
                <div className="text-xs text-muted">{user?.role === 'ADMIN' ? '系统管理员' : user?.department || '临床医生'}</div>
              </div>
              <img
                className="h-11 w-11 rounded-2xl object-cover ring-2 ring-blue-100"
                alt="用户头像"
                src={
                  user?.avatarUrl ||
                  'https://lh3.googleusercontent.com/aida-public/AB6AXuA2SvgHuI_MCXg-jIRL2kNz7rC6jxnIRaNy6X3Dm-tgwFEkR9DAFzCSMVAy75wC7P_yBkFtl2KY4GTcJIMqEQ-Y2ZFT_Zqhi9lcQhI_fk4LqrUAKKaZOgywTk1UiHoieAzqd0p9l06OjN09jU4pvY6t0r3UQFQ0HlKQ7-RsLDsMImLSvPVOGqPz4rKGB1rntPS688oX0LNnsrAOms6M3-A_s4S9JcgLGJyO69GUHNyzMhuRN6_0BXtyu-Iq9y1h3IxqrZ8-NH45cxs'
                }
              />
            </button>
          </div>
        </header>
        <main className="mx-auto max-w-[1440px] px-5 py-10 md:px-10">{children}</main>
      </div>
    </div>
  );
}
