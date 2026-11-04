import { Component, forwardRef, Input } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'cm-textarea',
  standalone: true,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => CmTextareaComponent),
      multi: true,
    },
  ],
  template: `
    <label class="field">
      <span>{{ label }}</span>
      <textarea
        [value]="value"
        [placeholder]="placeholder"
        [rows]="rows"
        [disabled]="disabled"
        (input)="handleInput($event)"
        (blur)="onTouched()"
      ></textarea>
    </label>
  `,
  styleUrl: './cm-textarea.component.css',
})
export class CmTextareaComponent implements ControlValueAccessor {
  @Input({ required: true }) label = '';
  @Input() placeholder = '';
  @Input() rows = 3;

  value = '';
  disabled = false;

  onChange: (value: string) => void = () => undefined;
  onTouched: () => void = () => undefined;

  writeValue(value: string | null | undefined): void {
    this.value = value ?? '';
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

  handleInput(event: Event): void {
    const nextValue = (event.target as HTMLTextAreaElement).value;
    this.value = nextValue;
    this.onChange(nextValue);
  }
}
