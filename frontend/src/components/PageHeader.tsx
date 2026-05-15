import type { ReactNode } from 'react';

export function PageHeader({
  title,
  description,
  action,
}: {
  title: string;
  description: string;
  action?: ReactNode;
}) {
  return (
    <div className="mb-8 flex flex-col gap-5 md:flex-row md:items-center md:justify-between">
      <div>
        <h1 className="font-headline text-4xl font-extrabold tracking-tight text-ink">{title}</h1>
        <p className="mt-2 text-base font-semibold text-muted">{description}</p>
      </div>
      {action}
    </div>
  );
}
