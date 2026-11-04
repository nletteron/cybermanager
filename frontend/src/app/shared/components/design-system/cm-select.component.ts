import { Component, forwardRef, Input } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'cm-select',
  standalone: true,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => CmSelectComponent),
      multi: true,
    },
  ],
  template: `
    <label class="field">
      <span>{{ label }}</span>
      <select [value]="value" [disabled]="disabled" (change)="handleChange($event)" (blur)="onTouched()">
        <ng-content />
      </select>
    </label>
  `,
  styleUrl: './cm-select.component.css',
})
export class CmSelectComponent implements ControlValueAccessor {
  @Input({ required: true }) label = '';

  value = '';
  disabled = false;

  onChange: (value: string) => void = () => undefined;
  onTouched: () => void = () => undefined;

  writeValue(value: string | number | null | undefined): void {
    this.value = value === null || value === undefined ? '' : String(value);
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(disabled: boolean): void {
    this.disabled = disabled;
  }

  handleChange(event: Event): void {
    const nextValue = (event.target as HTMLSelectElement).value;
    this.value = nextValue;
    this.onChange(nextValue);
  }
}
