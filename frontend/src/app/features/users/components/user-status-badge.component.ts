import { Component, input } from '@angular/core';

@Component({
  selector: 'app-user-status-badge',
  template: `
    <span class="status-badge" [class.active]="status() === 'ACTIVE'" [class.disabled]="status() === 'DISABLED'">
      {{ status() === 'ACTIVE' ? 'Active' : 'Disabled' }}
    </span>
  `,
  styleUrl: './user-status-badge.component.css',
})
export class UserStatusBadgeComponent {
  readonly status = input<'ACTIVE' | 'DISABLED'>('ACTIVE');
}
