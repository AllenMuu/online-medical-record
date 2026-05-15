import { useAuth } from '../AuthContext';
import { PageHeader } from '../components/PageHeader';

export function SettingsPage() {
  const { user } = useAuth();
  return (
    <>
      <PageHeader title="设置" description="查看当前账号和系统运行信息。" />
      <div className="panel max-w-2xl">
        <div className="grid gap-4 text-sm font-semibold text-slate-700">
          <div className="flex justify-between"><span>当前账号</span><strong>{user?.email}</strong></div>
          <div className="flex justify-between"><span>角色</span><strong>{user?.role === 'ADMIN' ? '管理员' : '医生'}</strong></div>
          <div className="flex justify-between"><span>系统状态</span><strong className="text-green-700">正常</strong></div>
          <div className="flex justify-between"><span>最后同步</span><strong>2 分钟前</strong></div>
        </div>
      </div>
    </>
  );
}
