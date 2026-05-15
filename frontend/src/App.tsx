import { Navigate, Route, Routes } from 'react-router-dom';
import type { ReactNode } from 'react';
import { AuthProvider, useAuth } from './AuthContext';
import { AppShell } from './components/AppShell';
import { DashboardPage } from './pages/DashboardPage';
import { LoginPage } from './pages/LoginPage';
import { NewRecordPage } from './pages/NewRecordPage';
import { PatientsPage } from './pages/PatientsPage';
import { RecordsPage } from './pages/RecordsPage';
import { SettingsPage } from './pages/SettingsPage';
import { SimplePage } from './pages/SimplePage';
import { UsersPage } from './pages/UsersPage';

function Protected({ children }: { children: ReactNode }) {
  const { user, loading } = useAuth();
  if (loading) {
    return <div className="grid min-h-screen place-items-center bg-surface text-muted">正在加载系统...</div>;
  }
  if (!user) {
    return <Navigate to="/login" replace />;
  }
  return <AppShell>{children}</AppShell>;
}

function LoginRoute() {
  const { user, loading } = useAuth();
  if (!loading && user) {
    return <Navigate to="/dashboard" replace />;
  }
  return <LoginPage />;
}

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<LoginRoute />} />
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard" element={<Protected><DashboardPage /></Protected>} />
        <Route path="/patients" element={<Protected><PatientsPage /></Protected>} />
        <Route path="/records" element={<Protected><RecordsPage /></Protected>} />
        <Route path="/records/new" element={<Protected><NewRecordPage /></Protected>} />
        <Route path="/schedule" element={<Protected><SimplePage title="排班计划" description="排班视图已预留，后续可接入医生日程和班次管理。" /></Protected>} />
        <Route path="/settings" element={<Protected><SettingsPage /></Protected>} />
        <Route path="/admin/users" element={<Protected><UsersPage /></Protected>} />
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </AuthProvider>
  );
}
