function padTimeSegment(value: string) {
  return value.padStart(2, '0');
}

export function normalizeVisitTime(value?: string) {
  if (!value) {
    return '00:00:00';
  }

  const [hours = '00', minutes = '00', seconds = '00'] = value.split(':');
  return [hours, minutes, seconds].map((segment) => padTimeSegment(segment.slice(0, 2))).join(':');
}

export function createVisitDateTimeInputValue(visitDate: string, visitTime: string) {
  return `${visitDate}T${normalizeVisitTime(visitTime)}`;
}

export function splitVisitDateTimeInputValue(value: string) {
  const [visitDate = '', rawVisitTime = ''] = value.split('T');
  return {
    visitDate,
    visitTime: normalizeVisitTime(rawVisitTime),
  };
}
