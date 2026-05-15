import { Activity, FileText, Stethoscope, Users } from 'lucide-react';
import { useEffect, useState } from 'react';
import { api } from '../api';
import { PageHeader } from '../components/PageHeader';
import type { DashboardSummary } from '../types';

export function DashboardPage() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);

  useEffect(() => {
    void api.dashboard().then(setSummary);
  }, []);

  const cards = [
    { label: '患者档案', value: summary?.patientCount ?? '-', icon: Users, tone: 'bg-blue-600 text-white' },
    { label: '病历记录', value: summary?.recordCount ?? '-', icon: FileText, tone: 'bg-white text-ink' },
    { label: '本月新增', value: summary?.monthlyRecords ?? '-', icon: Activity, tone: 'bg-white text-ink' },
    { label: '医生账号', value: summary?.doctorCount ?? '-', icon: Stethoscope, tone: 'bg-white text-ink' },
  ];

  return (
    <>
      <PageHeader title="仪表盘" description="查看临床运营摘要与近期诊疗数据。" />
      <div className="grid gap-6 md:grid-cols-4">
        {cards.map((card) => (
          <div key={card.label} className={`rounded-xl p-8 shadow-ambient ${card.tone}`}>
            <card.icon className="mb-8 opacity-70" size={34} />
            <div className="text-sm font-bold opacity-70">{card.label}</div>
            <div className="mt-3 font-headline text-5xl font-extrabold">{card.value}</div>
          </div>
        ))}
      </div>
      <div className="mt-8 rounded-xl bg-white p-8 shadow-ambient">
        <h2 className="font-headline text-2xl font-extrabold">临床指南提醒</h2>
        <p className="mt-3 max-w-3xl text-sm font-semibold leading-7 text-muted">
          当前患者群体录入平均时长为 4.5 分钟。请确保诊断编码符合 ICD-10 标准，并在提交前复核用药剂量。
        </p>
      </div>
    </>
  );
}
