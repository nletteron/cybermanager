import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Output, input } from '@angular/core';

import type { UserResponse } from '../models/user.models';
import { UserStatusBadgeComponent } from './user-status-badge.component';

@Component({
  selector: 'app-users-table',
  imports: [CommonModule, UserStatusBadgeComponent],
  template: `
    <table class="table">
      <thead>
        <tr>
          <th>Utilisateur</th>
          <th>Roles</th>
          <th>Statut</th>
          <th></th>
        </tr>
      </thead>

      <tbody>
        <tr *ngFor="let user of users()" [class.active]="selectedUserId() === user.userId">
          <td>
            <strong>{{ user.firstName }} {{ user.lastName }}</strong>
            <div class="email">{{ user.email }}</div>
          </td>
          <td class="roles">{{ user.roles.join(', ') }}</td>
          <td><app-user-status-badge [status]="user.status" /></td>
          <td><button type="button" (click)="selected.emit(user)">Modifier</button></td>
        </tr>

        <tr *ngIf="users().length === 0">
          <td colspan="4" class="empty">Aucun utilisateur pour ce filtre.</td>
        </tr>
      </tbody>
    </table>
  `,
  styleUrl: './users-table.component.css',
})
export class UsersTableComponent {
  readonly users = input<UserResponse[]>([]);
  readonly selectedUserId = input<string | null>(null);

  @Output() readonly selected = new EventEmitter<UserResponse>();
}
