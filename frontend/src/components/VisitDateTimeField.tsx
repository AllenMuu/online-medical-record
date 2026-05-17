import { CalendarDays, Clock3 } from 'lucide-react';
import { createVisitDateTimeInputValue, splitVisitDateTimeInputValue } from '../visitDateTime';

interface VisitDateTimeFieldProps {
  id: string;
  label: string;
  visitDate: string;
  visitTime: string;
  onChange: (value: { visitDate: string; visitTime: string }) => void;
  required?: boolean;
}

export function VisitDateTimeField({
  id,
  label,
  visitDate,
  visitTime,
  onChange,
  required = false,
}: VisitDateTimeFieldProps) {
  const value = createVisitDateTimeInputValue(visitDate, visitTime);

  return (
    <label htmlFor={id}>
      <span className={required ? 'form-label required' : 'form-label'}>{label}</span>
      <div className="datetime-field">
        <div className="datetime-field__prefix">
          <CalendarDays size={18} />
          <Clock3 size={16} />
        </div>
        <input
          id={id}
          className="datetime-field__input"
          type="datetime-local"
          step={1}
          value={value}
          onChange={(event) => onChange(splitVisitDateTimeInputValue(event.target.value))}
          required={required}
        />
      </div>
    </label>
  );
}
