import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';

import { CmButtonComponent, CmInputComponent, CmSelectComponent } from '../../../shared/components/design-system';
import type { UserResponse } from '../models/user.models';
import { UserFormComponent } from '../components/user-form.component';
import { UsersTableComponent } from '../components/users-table.component';
import { UsersApiService } from '../services/users-api.service';

@Component({
  selector: 'app-users-page',
  imports: [CommonModule, ReactiveFormsModule, UsersTableComponent, UserFormComponent, CmButtonComponent, CmInputComponent, CmSelectComponent],
  template: `
    <section class="page crud-page users-page">
      <header class="hero">
        <div>
          <p class="eyebrow">Bounded context users</p>
          <h2>Gestion des utilisateurs</h2>
        </div>
      </header>

      <p class="error" *ngIf="error()">{{ error() }}</p>

      <div class="toolbar crud-toolbar">
        <div class="section-title-group">
          <cm-button variant="ghost" (click)="showFilters.set(!showFilters())" [attr.aria-expanded]="showFilters()">
            <span class="filter-icon" aria-hidden="true"></span>
            <span>Filtres</span>
          </cm-button>
          <cm-button (click)="openCreatePanel()">Creer un nouvel utilisateur</cm-button>
        </div>
      </div>

      <div class="filters-grid collapsible" [class.is-collapsed]="!showFilters()" [formGroup]="filters">
        <cm-input label="Recherche par nom" type="search" formControlName="term" placeholder="Nom ou prenom" />

        <cm-select label="Role" formControlName="role">
          <option value="">Tous les roles</option>
          <option *ngFor="let role of roleOptions" [value]="role">{{ role }}</option>
        </cm-select>

        <cm-select label="Statut" formControlName="status">
          <option value="">Tous les statuts</option>
          <option value="ACTIVE">Actifs</option>
          <option value="DISABLED">Desactives</option>
        </cm-select>

        <cm-select label="Taille de page" formControlName="pageSize">
          <option *ngFor="let size of pageSizeOptions" [value]="size">{{ size }} / page</option>
        </cm-select>
      </div>

      <div class="grid crud-grid" [class.panel-open]="isPanelOpen()">
        <article class="panel list-panel crud-list-panel">
          <div class="panel-header results-toolbar">
            <h3>Liste</h3>
            <div class="section-tools">
              <p class="summary crud-summary">
                {{ filteredUsers().length }} utilisateur{{ filteredUsers().length > 1 ? 's' : '' }}
                <span *ngIf="filteredUsers().length !== users().length">sur {{ users().length }}</span>
              </p>
              <div class="pager" *ngIf="totalPages() > 1">
                <cm-button variant="ghost" (click)="previousPage()" [disabled]="currentPage() === 1">Precedent</cm-button>
                <span>Page {{ currentPage() }} / {{ totalPages() }}</span>
                <cm-button variant="ghost" (click)="nextPage()" [disabled]="currentPage() === totalPages()">Suivant</cm-button>
              </div>
            </div>
          </div>

          <app-users-table
            [users]="paginatedUsers()"
            [selectedUserId]="selectedUser()?.userId ?? null"
            (selected)="selectUser($event)"
          />
        </article>

        <article class="panel side-panel crud-side-panel" [class.open]="isPanelOpen()" [attr.aria-hidden]="!isPanelOpen()">
          <div class="side-panel-header">
            <h3>{{ panelMode() === 'edit' ? 'Edition utilisateur' : 'Creation utilisateur' }}</h3>
            <cm-button variant="icon" (click)="closePanel()">Fermer</cm-button>
          </div>

          <app-user-form
            [mode]="panelMode()"
            [initialUser]="selectedUser()"
            (saved)="saveUser($event)"
            (statusToggled)="onStatusToggled()"
            (deleted)="onDeleteRequested()"
          />
        </article>
      </div>
    </section>
  `,
  styleUrl: './users-page.component.css',
})
export class UsersPageComponent {
  private readonly api = inject(UsersApiService);
  private readonly fb = inject(FormBuilder);

  readonly roleOptions = ['ADMIN', 'EMPLOYEE'];
  readonly pageSizeOptions = [5, 10, 20, 50];
  readonly filters = this.fb.nonNullable.group({
    term: [''],
    role: [''],
    status: [''],
    pageSize: [10],
  });
  readonly users = signal<UserResponse[]>([]);
  readonly selectedUser = signal<UserResponse | null>(null);
  readonly panelMode = signal<'create' | 'edit'>('create');
  readonly isPanelOpen = signal(false);
  readonly showFilters = signal(false);
  readonly page = signal(1);
  readonly error = signal('');
  readonly filterState = signal(this.filters.getRawValue());

  readonly filteredUsers = computed(() => {
    const { term, role, status } = this.filterState();
    const normalizedTerm = term.trim().toLocaleLowerCase();

    return this.users().filter((user) => {
      const fullName = `${user.firstName} ${user.lastName}`.toLocaleLowerCase();
      const matchesName = normalizedTerm.length === 0 || fullName.includes(normalizedTerm);
      const matchesRole = !role || user.roles.includes(role);
      const matchesStatus = !status || user.status === status;
      return matchesName && matchesRole && matchesStatus;
    });
  });

  readonly totalPages = computed(() => {
    const pageSize = Number(this.filterState().pageSize) || 10;
    return Math.max(1, Math.ceil(this.filteredUsers().length / pageSize));
  });

  readonly currentPage = computed(() => Math.min(this.page(), this.totalPages()));

  readonly paginatedUsers = computed(() => {
    const pageSize = Number(this.filterState().pageSize) || 10;
    const page = this.currentPage();
    const start = (page - 1) * pageSize;
    return this.filteredUsers().slice(start, start + pageSize);
  });

  constructor() {
    this.filters.valueChanges.subscribe((value) => {
      this.filterState.set({
        term: value.term ?? '',
        role: value.role ?? '',
        status: value.status ?? '',
        pageSize: Number(value.pageSize ?? 10),
      });
      this.page.set(1);
    });

    this.loadUsers();
  }

  loadUsers(): void {
    this.api.search('', '').subscribe({
      next: (users) => {
        this.users.set(users);
        this.error.set('');
      },
      error: (error: HttpErrorResponse) => this.error.set(this.resolveError(error, 'Chargement impossible')),
    });
  }

  selectUser(user: UserResponse): void {
    this.selectedUser.set(user);
    this.panelMode.set('edit');
    this.isPanelOpen.set(true);
  }

  openCreatePanel(): void {
    this.panelMode.set('create');
    this.selectedUser.set(null);
    this.error.set('');
    this.isPanelOpen.set(true);
  }

  closePanel(): void {
    this.selectedUser.set(null);
    this.isPanelOpen.set(false);
    this.error.set('');
  }

  previousPage(): void {
    this.page.update((page) => Math.max(1, page - 1));
  }

  nextPage(): void {
    this.page.update((page) => Math.min(this.totalPages(), page + 1));
  }

  saveUser(payload: { email: string; firstName: string; lastName: string; password?: string; roles: string[] }): void {
    const current = this.selectedUser();
    const request = current
      ? this.api.update(current.userId, payload)
      : this.api.create({ ...payload, password: payload.password ?? '' });

    request.subscribe({
      next: () => {
        this.closePanel();
        this.loadUsers();
      },
      error: (error: HttpErrorResponse) => this.error.set(this.resolveError(error, 'Sauvegarde impossible')),
    });
  }

  toggleStatus(user: UserResponse): void {
    const request = user.status === 'ACTIVE' ? this.api.disable(user.userId) : this.api.enable(user.userId);
    request.subscribe({
      next: (updated) => {
        this.selectedUser.set(updated);
        this.loadUsers();
      },
      error: (error: HttpErrorResponse) => this.error.set(this.resolveError(error, 'Mise a jour du statut impossible')),
    });
  }

  deleteUser(user: UserResponse): void {
    if (!confirm(`Supprimer l'utilisateur ${user.firstName} ${user.lastName} ?`)) {
      return;
    }

    this.api.delete(user.userId).subscribe({
      next: () => {
        this.closePanel();
        this.loadUsers();
      },
      error: (error: HttpErrorResponse) => this.error.set(this.resolveError(error, 'Suppression impossible')),
    });
  }

  onStatusToggled(): void {
    const user = this.selectedUser();
    if (!user) {
      return;
    }
    this.toggleStatus(user);
  }

  onDeleteRequested(): void {
    const user = this.selectedUser();
    if (!user) {
      return;
    }
    this.deleteUser(user);
  }

  private resolveError(error: HttpErrorResponse, fallback: string): string {
    if (typeof error.error?.message === 'string' && error.error.message.trim().length > 0) {
      return error.error.message;
    }
    if (typeof error.error === 'string' && error.error.trim().length > 0) {
      return error.error;
    }
    if (error.status === 0) {
      return 'Serveur inaccessible ou non redemarre.';
    }
    if (error.status === 404) {
      return 'Endpoint introuvable. Redemarre probablement le backend pour charger la suppression.';
    }
    if (error.status === 403) {
      return 'Action reservee a un administrateur.';
    }
    return fallback;
  }
}
