import { CommonModule } from '@angular/common';
import { Component, computed, inject } from '@angular/core';

import { SessionService } from '../../../core/services/session.service';

@Component({
  selector: 'app-dashboard-page',
  imports: [CommonModule],
  template: `
    <section class="dashboard">
      <header>
        <p class="eyebrow">Vue opérationnelle</p>
        <h2>Bienvenue {{ fullName() }}</h2>
        <p>Cette base couvre l’authentification et la gestion des utilisateurs.</p>
      </header>
      <article class="card">
        <h3>Profil courant</h3>
        <dl *ngIf="session.user() as user">
          <div><dt>Email</dt><dd>{{ user.email }}</dd></div>
          <div><dt>Rôles</dt><dd>{{ user.roles.join(', ') }}</dd></div>
        </dl>
      </article>
    </section>
  `,
  styleUrl: './dashboard-page.component.css',
})
export class DashboardPageComponent {
  readonly session = inject(SessionService);
  readonly fullName = computed(() => {
    const user = this.session.user();
    return user ? `${user.firstName} ${user.lastName}` : '';
  });
}
