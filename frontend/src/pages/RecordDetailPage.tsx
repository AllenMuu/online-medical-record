import { ArrowLeft, CalendarDays, Pencil, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { api } from '../api';
import { PageHeader } from '../components/PageHeader';
import { genderLabel, recordStatusLabel } from '../format';
import type { MedicalRecord } from '../types';

function displayText(value?: string) {
  return value?.trim() ? value : '暂无记录';
}

export function RecordDetailPage() {
  const navigate = useNavigate();
  const { recordId } = useParams();
  const [record, setRecord] = useState<MedicalRecord | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const numericRecordId = Number(recordId);
    if (!Number.isFinite(numericRecordId)) {
      setLoading(false);
      setError('病历编号无效');
      return;
    }

    let cancelled = false;
    setLoading(true);
    setError('');
    void api.record(numericRecordId)
      .then((nextRecord) => {
        if (!cancelled) {
          setRecord(nextRecord);
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setRecord(null);
          setError(err instanceof Error ? err.message : '病历加载失败');
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [recordId]);

  const handleDelete = async () => {
    if (!record) {
      return;
    }
    if (!window.confirm(`确认删除 ${record.patientName} 的这条病历吗？`)) {
      return;
    }
    setError('');
    try {
      await api.deleteRecord(record.id);
      navigate('/records');
    } catch (err) {
      setError(err instanceof Error ? err.message : '删除失败');
    }
  };

  return (
    <>
      <PageHeader
        title={record ? `${record.patientName} 病历详情` : '病历详情'}
        description="查看当前病历的诊疗信息与状态。"
        action={
          <div className="flex flex-wrap gap-4">
            <button type="button" className="btn-secondary" onClick={() => navigate('/records')}>
              <ArrowLeft size={20} />
              返回列表
            </button>
            {record && (
              <>
                <button type="button" className="btn-secondary" onClick={handleDelete}>
                  <Trash2 size={20} />
                  删除病历
                </button>
                <button type="button" className="btn-primary" onClick={() => navigate(`/records/${record.id}/edit`)}>
                  <Pencil size={20} />
                  编辑病历
                </button>
              </>
            )}
          </div>
        }
      />
      {loading && <div className="panel text-sm font-semibold text-muted">正在加载病历详情...</div>}
      {!loading && error && <div className="rounded-lg bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">{error}</div>}
      {!loading && record && (
        <>
          <div className="panel mb-8 grid gap-6 md:grid-cols-4">
            <div>
              <div className="text-sm font-bold text-muted">患者</div>
              <div className="mt-2 font-headline text-2xl font-extrabold text-ink">{record.patientName}</div>
              <div className="mt-1 text-sm font-semibold text-muted">{genderLabel(record.patientGender)} · {record.patientAge}岁</div>
            </div>
            <div>
              <div className="text-sm font-bold text-muted">接诊医生</div>
              <div className="mt-2 text-base font-extrabold text-ink">{record.doctorName}</div>
            </div>
            <div>
              <div className="text-sm font-bold text-muted">就诊时间</div>
              <div className="mt-2 flex items-center gap-2 text-base font-extrabold text-ink">
                <CalendarDays size={18} />
                <span>{record.visitDate} {record.visitTime}</span>
              </div>
            </div>
            <div>
              <div className="text-sm font-bold text-muted">状态</div>
              <div className="mt-2"><span className="badge">{recordStatusLabel(record.status)}</span></div>
            </div>
          </div>

          <div className="grid gap-8 xl:grid-cols-[1fr_360px]">
            <div className="space-y-8">
              <section className="record-section blue">
                <h2>临床评估</h2>
                <div>
                  <div className="form-label">身体状况/主诉</div>
                  <div className="rounded-xl bg-surface-low px-4 py-4 text-sm font-semibold leading-7 text-ink">{displayText(record.complaint)}</div>
                </div>
                <div>
                  <div className="form-label">查体</div>
                  <div className="rounded-xl bg-surface-low px-4 py-4 text-sm font-semibold leading-7 text-ink">{displayText(record.examination)}</div>
                </div>
                <div>
                  <div className="form-label">诊断</div>
                  <div className="rounded-xl bg-surface-low px-4 py-4 text-sm font-semibold leading-7 text-ink">{displayText(record.diagnosis)}</div>
                </div>
              </section>

              <section className="record-section slate">
                <h2>治疗方案与预后</h2>
                <div>
                  <div className="form-label">处置</div>
                  <div className="rounded-xl bg-surface-low px-4 py-4 text-sm font-semibold leading-7 text-ink">{displayText(record.treatment)}</div>
                </div>
                <div>
                  <div className="form-label">预后</div>
                  <div className="rounded-xl bg-surface-low px-4 py-4 text-sm font-semibold leading-7 text-ink">{displayText(record.prognosis)}</div>
                </div>
              </section>
            </div>

            <aside className="space-y-6">
              <div className="side-card">
                <h3>备注</h3>
                <div className="rounded-xl bg-surface-low px-4 py-4 text-sm font-semibold leading-7 text-ink">{displayText(record.notes)}</div>
              </div>
            </aside>
          </div>
        </>
      )}
    </>
  );
}
