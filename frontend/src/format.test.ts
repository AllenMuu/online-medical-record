import { describe, expect, it } from 'vitest';
import { formatDateTime, genderLabel, recordStatusLabel } from './format';

describe('format helpers', () => {
  it('formats clinical labels', () => {
    expect(genderLabel('MALE')).toBe('男');
    expect(genderLabel('FEMALE')).toBe('女');
    expect(recordStatusLabel('COMPLETED')).toBe('已完成');
    expect(recordStatusLabel('IN_PROGRESS')).toBe('进行中');
  });

  it('formats datetime strings', () => {
    expect(formatDateTime('2026-05-17T00:30:00Z', 'UTC')).toBe('2026-05-17 00:30');
    expect(formatDateTime('2026-05-17T00:30:00Z', 'Asia/Shanghai')).toBe('2026-05-17 08:30');
  });
});
