import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { CmButtonComponent, CmInputComponent, CmSelectComponent } from '../../../shared/components/design-system';
import type { SubscriptionOffer } from '../models/subscription-offer.models';
import { SubscriptionOffersApiService } from '../services/subscription-offers-api.service';

@Component({
  selector: 'app-subscription-offers-page',
  imports: [CommonModule, ReactiveFormsModule, CmButtonComponent, CmInputComponent, CmSelectComponent],
  template: `
    <section class="page crud-page subscription-offers-page">
      <header class="hero">
        <div>
          <p class="eyebrow">Bounded context subscription</p>
          <h2>Offres d'abonnement</h2>
        </div>
      </header>

      <p class="error" *ngIf="error()">{{ error() }}</p>

      <div class="toolbar crud-toolbar">
        <div class="section-title-group">
          <cm-button variant="ghost" (click)="showFilters.set(!showFilters())" [attr.aria-expanded]="showFilters()">
            <span class="filter-icon" aria-hidden="true"></span>
            <span>Filtres</span>
          </cm-button>
          <cm-button (click)="openCreatePanel()">Creer une nouvelle offre</cm-button>
        </div>
      </div>

      <div class="filters-grid collapsible" [class.is-collapsed]="!showFilters()" [formGroup]="filters">
        <cm-input label="Recherche par nom" formControlName="term" placeholder="Nom de l'offre" />

        <cm-select label="Statut" formControlName="status">
          <option value="">Tous les statuts</option>
          <option value="ACTIVE">Actives</option>
          <option value="INACTIVE">Inactives</option>
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
                {{ filteredOffers().length }} offre{{ filteredOffers().length > 1 ? 's' : '' }}
                <span *ngIf="filteredOffers().length !== offers().length">sur {{ offers().length }}</span>
              </p>
              <div class="pager" *ngIf="totalPages() > 1">
                <cm-button variant="ghost" (click)="previousPage()" [disabled]="currentPage() === 1">Precedent</cm-button>
                <span>Page {{ currentPage() }} / {{ totalPages() }}</span>
                <cm-button variant="ghost" (click)="nextPage()" [disabled]="currentPage() === totalPages()">Suivant</cm-button>
              </div>
            </div>
          </div>

          <div class="table-shell">
            <table class="table">
              <thead>
                <tr>
                  <th>Nom</th>
                  <th>Duree</th>
                  <th>Prix</th>
                  <th>Statut</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let item of paginatedOffers()" [class.active]="selected()?.offerId === item.offerId">
                  <td><strong>{{ item.name }}</strong></td>
                  <td class="muted">{{ item.includedMinutes }} min</td>
                  <td>{{ item.price | number:'1.2-2' }} EUR</td>
                  <td>
                    <span class="status-badge" [class.active]="item.status === 'ACTIVE'" [class.inactive]="item.status === 'INACTIVE'">
                      {{ item.status === 'ACTIVE' ? 'Active' : 'Inactive' }}
                    </span>
                  </td>
                  <td class="action-cell"><cm-button (click)="edit(item)">Modifier</cm-button></td>
                </tr>

                <tr *ngIf="paginatedOffers().length === 0">
                  <td colspan="5" class="empty">Aucune offre pour ce filtre.</td>
                </tr>
              </tbody>
            </table>
          </div>
        </article>

        <form class="panel side-panel crud-side-panel form crud-form" [class.open]="isPanelOpen()" [formGroup]="form" (ngSubmit)="save()" [attr.aria-hidden]="!isPanelOpen()">
          <div class="side-panel-header">
            <h3>{{ selected() ? 'Edition offre' : 'Creation offre' }}</h3>
            <cm-button variant="icon" (click)="closePanel()">Fermer</cm-button>
          </div>

          <cm-input label="Nom" formControlName="name" placeholder="Nom de l'offre" />

          <cm-input label="Prix" formControlName="price" type="number" valueKind="number" step="0.01" min="0.01" placeholder="Prix" />

          <cm-input label="Minutes incluses" formControlName="includedMinutes" type="number" valueKind="number" min="1" placeholder="Minutes incluses" />

          <div class="actions">
            <cm-button type="submit" [disabled]="form.invalid">Enregistrer</cm-button>
            <cm-button *ngIf="selected()" variant="secondary" (click)="toggleStatus()">
              {{ selected()?.status === 'ACTIVE' ? 'Desactiver' : 'Activer' }}
            </cm-button>
            <cm-button *ngIf="selected()" variant="danger" (click)="deleteOffer()">Supprimer</cm-button>
          </div>
        </form>
      </div>
    </section>
  `,
  styleUrl: './subscription-offers-page.component.css',
})
export class SubscriptionOffersPageComponent {
  private readonly api = inject(SubscriptionOffersApiService);
  private readonly fb = inject(FormBuilder);

  readonly pageSizeOptions = [5, 10, 20, 50];
  readonly offers = signal<SubscriptionOffer[]>([]);
  readonly selected = signal<SubscriptionOffer | null>(null);
  readonly isPanelOpen = signal(false);
  readonly showFilters = signal(false);
  readonly page = signal(1);
  readonly error = signal('');
  readonly filters = this.fb.nonNullable.group({ term: [''], status: [''], pageSize: [10] });
  readonly filterState = signal(this.filters.getRawValue());
  readonly form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    price: [0, [Validators.required, Validators.min(0.01)]],
    includedMinutes: [60, [Validators.required, Validators.min(1)]],
  });

  readonly filteredOffers = computed(() => {
    const { term, status } = this.filterState();
    const normalizedTerm = term.trim().toLocaleLowerCase();

    return this.offers().filter((item) => {
      const matchesName = normalizedTerm.length === 0 || item.name.toLocaleLowerCase().includes(normalizedTerm);
      const matchesStatus = !status || item.status === status;
      return matchesName && matchesStatus;
    });
  });

  readonly totalPages = computed(() => {
    const pageSize = Number(this.filterState().pageSize) || 10;
    return Math.max(1, Math.ceil(this.filteredOffers().length / pageSize));
  });

  readonly currentPage = computed(() => Math.min(this.page(), this.totalPages()));

  readonly paginatedOffers = computed(() => {
    const pageSize = Number(this.filterState().pageSize) || 10;
    const start = (this.currentPage() - 1) * pageSize;
    return this.filteredOffers().slice(start, start + pageSize);
  });

  constructor() {
    this.filters.valueChanges.subscribe((value) => {
      this.filterState.set({
        term: value.term ?? '',
        status: value.status ?? '',
        pageSize: Number(value.pageSize ?? 10),
      });
      this.page.set(1);
    });

    this.load();
  }

  load(): void {
    this.api.search('', '').subscribe({
      next: (items) => {
        this.offers.set(items);
        this.error.set('');
      },
      error: (error: HttpErrorResponse) => this.error.set(this.resolveError(error, 'Chargement impossible')),
    });
  }

  edit(item: SubscriptionOffer): void {
    this.selected.set(item);
    this.form.patchValue({ name: item.name, price: item.price, includedMinutes: item.includedMinutes });
    this.isPanelOpen.set(true);
  }

  openCreatePanel(): void {
    this.selected.set(null);
    this.form.reset({ name: '', price: 0, includedMinutes: 60 });
    this.error.set('');
    this.isPanelOpen.set(true);
  }

  closePanel(): void {
    this.selected.set(null);
    this.isPanelOpen.set(false);
    this.error.set('');
  }

  previousPage(): void {
    this.page.update((page) => Math.max(1, page - 1));
  }

  nextPage(): void {
    this.page.update((page) => Math.min(this.totalPages(), page + 1));
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const payload = this.form.getRawValue();
    const current = this.selected();
    const request = current ? this.api.update(current.offerId, payload) : this.api.create(payload);
    request.subscribe({
      next: () => {
        this.closePanel();
        this.load();
      },
      error: (error: HttpErrorResponse) => this.error.set(this.resolveError(error, 'Sauvegarde impossible')),
    });
  }

  toggleStatus(): void {
    const current = this.selected();
    if (!current) {
      return;
    }

    const request = current.status === 'ACTIVE'
      ? this.api.deactivate(current.offerId)
      : this.api.activate(current.offerId);

    request.subscribe({
      next: (updated) => {
        this.selected.set(updated);
        this.load();
      },
      error: (error: HttpErrorResponse) => this.error.set(this.resolveError(error, 'Mise a jour du statut impossible')),
    });
  }

  deleteOffer(): void {
    const current = this.selected();
    if (!current) {
      return;
    }
    if (!confirm(`Supprimer l'offre ${current.name} ?`)) {
      return;
    }

    this.api.delete(current.offerId).subscribe({
      next: () => {
        this.closePanel();
        this.load();
      },
      error: (error: HttpErrorResponse) => this.error.set(this.resolveError(error, 'Suppression impossible')),
    });
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
