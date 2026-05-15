import type { Gender, RecordStatus } from './types';

export function genderLabel(gender: Gender | string) {
  if (gender === 'FEMALE') return '女';
  if (gender === 'MALE') return '男';
  return '其他';
}

export function recordStatusLabel(status: RecordStatus) {
  return status === 'COMPLETED' ? '已完成' : '进行中';
}
