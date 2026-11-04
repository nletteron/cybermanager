import { Component, forwardRef, Input } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

type InputType = 'text' | 'email' | 'password' | 'search' | 'number' | 'date';
type InputValueKind = 'string' | 'number';

@Component({
  selector: 'cm-input',
  standalone: true,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => CmInputComponent),
      multi: true,
    },
  ],
  template: `
    <label class="field">
      <span>{{ label }}</span>
      <input
        [type]="type"
        [value]="value"
        [placeholder]="placeholder"
        [autocomplete]="autocomplete"
        [attr.min]="min"
        [attr.max]="max"
        [attr.step]="step"
        [disabled]="disabled"
        (input)="handleInput($event)"
        (blur)="onTouched()"
      />
    </label>
  `,
  styleUrl: './cm-input.component.css',
})
export class CmInputComponent implements ControlValueAccessor {
  @Input({ required: true }) label = '';
  @Input() type: InputType = 'text';
  @Input() valueKind: InputValueKind = 'string';
  @Input() placeholder = '';
  @Input() autocomplete = '';
  @Input() min: string | number | null = null;
  @Input() max: string | number | null = null;
  @Input() step: string | number | null = null;

  @Input() value = '';
  disabled = false;

  onChange: (value: string | number | null) => void = () => undefined;
  onTouched: () => void = () => undefined;

  writeValue(value: string | number | null | undefined): void {
    this.value = value === null || value === undefined ? '' : String(value);
  }

  registerOnChange(fn: (value: string | number | null) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(disabled: boolean): void {
    this.disabled = disabled;
  }

  handleInput(event: Event): void {
    const nextValue = (event.target as HTMLInputElement).value;
    this.value = nextValue;
    if (this.valueKind === 'number') {
      this.onChange(nextValue.trim().length === 0 ? null : Number(nextValue));
      return;
    }
    this.onChange(nextValue);
  }
}
