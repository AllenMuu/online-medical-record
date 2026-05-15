import { PageHeader } from '../components/PageHeader';

export function SimplePage({ title, description }: { title: string; description: string }) {
  return (
    <>
      <PageHeader title={title} description={description} />
      <div className="panel">
        <div className="rounded-xl bg-surface-low p-10 text-center font-semibold text-muted">模块入口已建立，可在后续迭代接入真实业务。</div>
      </div>
    </>
  );
}
