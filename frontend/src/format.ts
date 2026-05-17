import type { Gender, RecordStatus } from './types';

export function genderLabel(gender: Gender | string) {
  if (gender === 'FEMALE') return '女';
  if (gender === 'MALE') return '男';
  return '其他';
}

export function recordStatusLabel(status: RecordStatus) {
  return status === 'COMPLETED' ? '已完成' : '进行中';
}

export function formatDateTime(value?: string, timeZone?: string) {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  const formatter = new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
    ...(timeZone ? { timeZone } : {}),
  });
  const parts = Object.fromEntries(
    formatter
      .formatToParts(date)
      .filter((part) => part.type !== 'literal')
      .map((part) => [part.type, part.value]),
  );
  return `${parts.year}-${parts.month}-${parts.day} ${parts.hour}:${parts.minute}`;
}
