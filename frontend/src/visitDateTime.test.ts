import { describe, expect, it } from 'vitest';
import {
  createVisitDateTimeInputValue,
  normalizeVisitTime,
  splitVisitDateTimeInputValue,
} from './visitDateTime';

describe('visitDateTime helpers', () => {
  it('normalizes visit time to seconds', () => {
    expect(normalizeVisitTime('10:30')).toBe('10:30:00');
    expect(normalizeVisitTime('9:5:7')).toBe('09:05:07');
    expect(normalizeVisitTime('10:30:45')).toBe('10:30:45');
  });

  it('builds datetime-local values', () => {
    expect(createVisitDateTimeInputValue('2026-05-17', '10:30')).toBe('2026-05-17T10:30:00');
  });

  it('splits datetime-local values back to payload fields', () => {
    expect(splitVisitDateTimeInputValue('2026-05-17T10:30:45')).toEqual({
      visitDate: '2026-05-17',
      visitTime: '10:30:45',
    });
  });
});
