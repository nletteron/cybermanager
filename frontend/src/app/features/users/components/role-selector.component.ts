import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Output, input } from '@angular/core';

@Component({
  selector: 'app-role-selector',
  imports: [CommonModule],
  template: `
    <div class="roles">
      <label *ngFor="let role of rolesList">
        <input type="checkbox" [checked]="selected().includes(role)" (change)="toggle(role, $event)" />
        <span>{{ role }}</span>
      </label>
    </div>
  `,
  styleUrl: './role-selector.component.css',
})
export class RoleSelectorComponent {
  readonly selected = input<string[]>([]);
  readonly rolesList = ['ADMIN', 'EMPLOYEE'];

  @Output() readonly selectedChange = new EventEmitter<string[]>();

  toggle(role: string, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    const next = checked
      ? [...this.selected(), role]
      : this.selected().filter((item) => item !== role);
    this.selectedChange.emit(next);
  }
}
