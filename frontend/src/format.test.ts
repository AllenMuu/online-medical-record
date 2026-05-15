import { describe, expect, it } from 'vitest';
import { genderLabel, recordStatusLabel } from './format';

describe('format helpers', () => {
  it('formats clinical labels', () => {
    expect(genderLabel('MALE')).toBe('男');
    expect(genderLabel('FEMALE')).toBe('女');
    expect(recordStatusLabel('COMPLETED')).toBe('已完成');
    expect(recordStatusLabel('IN_PROGRESS')).toBe('进行中');
  });
});
