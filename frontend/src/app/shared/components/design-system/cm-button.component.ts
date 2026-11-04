import { Component, Input } from '@angular/core';

type ButtonType = 'button' | 'submit' | 'reset';
type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'danger' | 'icon';

@Component({
  selector: 'cm-button',
  standalone: true,
  template: `
    <button
      [attr.type]="type"
      [class.secondary]="variant === 'secondary'"
      [class.ghost]="variant === 'ghost'"
      [class.danger]="variant === 'danger'"
      [class.icon-button]="variant === 'icon'"
      [disabled]="disabled"
    >
      <ng-content />
    </button>
  `,
  styleUrl: './cm-button.component.css',
})
export class CmButtonComponent {
  @Input() type: ButtonType = 'button';
  @Input() variant: ButtonVariant = 'primary';
  @Input() disabled = false;
}
