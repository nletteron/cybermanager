import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import type { CustomerDebtSummary } from '../models/debt.models';
import { DebtsApiService } from '../services/debts-api.service';

@Component({
  selector: 'app-debts-page',
  imports: [CommonModule, FormsModule],
  template: `
    <section class="page debts-page">
      <header class="hero">
        <div>
          <p class="eyebrow">Suivi financier</p>
          <h2>Dettes clients</h2>
          <p class="lede">Affiche les clients ayant des dettes ouvertes et le detail de chaque dette non reglee.</p>
          <div class="hero-actions">
            <button type="button" class="ghost filter-toggle" (click)="showFilters.set(!showFilters())" [attr.aria-expanded]="showFilters()">
              <span class="filter-icon" aria-hidden="true"></span>
              <span>Filtres</span>
            </button>
          </div>
        </div>
      </header>

      <p class="error" *ngIf="error()">{{ error() }}</p>

      <div class="filters-grid collapsible debt-filters" [class.is-collapsed]="!showFilters()">
        <label class="field">
          <span>Dettes par page</span>
          <select [ngModel]="pageSize()" (ngModelChange)="updatePageSize($event)">
            <option [ngValue]="5">5</option>
            <option [ngValue]="10">10</option>
            <option [ngValue]="15">15</option>
            <option [ngValue]="20">20</option>
          </select>
        </label>
      </div>

      <div class="stack">
        <div class="panel results-toolbar" *ngIf="debts().length > 0">
          <p class="summary crud-summary">{{ debts().length }} client{{ debts().length > 1 ? 's' : '' }} avec dette ouverte</p>
          <div class="pager" *ngIf="totalPages() > 1">
            <button type="button" class="ghost" (click)="previousPage()" [disabled]="currentPage() === 1">Precedent</button>
            <p class="meta">Page {{ currentPage() }} / {{ totalPages() }}</p>
            <button type="button" class="ghost" (click)="nextPage()" [disabled]="currentPage() === totalPages()">Suivant</button>
          </div>
        </div>

        <article class="panel" *ngFor="let customer of pagedDebts()">
          <div class="panel-header">
            <div>
              <h3>{{ customer.customerName }}</h3>
              <p><span [class]="customerTypeChipClass(customer.customerType)">{{ customerTypeLabel(customer.customerType) }}</span></p>
            </div>
            <strong class="debt-total">{{ customer.totalOpenDebt | number:'1.2-2' }} EUR</strong>
          </div>

          <table class="table">
            <thead>
              <tr><th>Dette</th><th>Commentaire</th><th>Montant</th><th>Date</th><th></th></tr>
            </thead>
            <tbody>
              <tr *ngFor="let debt of customer.debts">
                <td class="debt-label-cell">
                  <div class="cell-content debt-label">
                    <span class="money-alert-icon" title="Dette ouverte"><span class="bill back"></span><span class="bill front"></span><span class="slash"></span></span>
                    <span>{{ debt.label }}</span>
                  </div>
                </td>
                <td class="comment-cell">
                  <div class="cell-content">
                    <input
                      [ngModel]="commentValue(debt.debtId, debt.comment)"
                      (ngModelChange)="updateComment(debt.debtId, $event)"
                      placeholder="Commentaire"
                    />
                  </div>
                </td>
                <td class="debt-amount">
                  <div class="cell-content">{{ debt.amount | number:'1.2-2' }} EUR</div>
                </td>
                <td>
                  <div class="cell-content">{{ debt.createdAt | date:'dd/MM/yyyy HH:mm' }}</div>
                </td>
                <td class="settle-cell">
                  <div class="cell-content">
                    <button type="button" (click)="settle(debt.debtId, debt.comment)">Marquer comme reglee</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </article>

        <p class="empty" *ngIf="debts().length === 0">Aucune dette ouverte.</p>
      </div>
    </section>
  `,
  styleUrl: './debts-page.component.css',
})
export class DebtsPageComponent {
  private readonly api = inject(DebtsApiService);

  readonly debts = signal<CustomerDebtSummary[]>([]);
  readonly comments = signal<Record<string, string>>({});
  readonly error = signal('');
  readonly showFilters = signal(false);
  readonly currentPage = signal(1);
  readonly pageSize = signal(10);
  readonly totalPages = computed(() => Math.max(1, Math.ceil(this.debts().length / this.pageSize())));
  readonly pagedDebts = computed(() => {
    const start = (this.currentPage() - 1) * this.pageSize();
    return this.debts().slice(start, start + this.pageSize());
  });

  constructor() {
    this.load();
  }

  load(): void {
    this.api.list().subscribe({
      next: (debts) => {
        this.debts.set(debts);
        this.currentPage.set(1);
        this.comments.set(
          debts.flatMap((customer) => customer.debts).reduce<Record<string, string>>((map, debt) => {
            map[debt.debtId] = debt.comment ?? '';
            return map;
          }, {}),
        );
        this.error.set('');
      },
      error: (error: HttpErrorResponse) => this.error.set(error.error?.message ?? 'Chargement des dettes impossible'),
    });
  }

  settle(debtId: string, fallbackComment: string): void {
    this.api.settle(debtId, this.commentValue(debtId, fallbackComment)).subscribe({
      next: () => this.load(),
      error: (error: HttpErrorResponse) => this.error.set(error.error?.message ?? 'Reglement impossible'),
    });
  }

  updateComment(debtId: string, comment: string): void {
    this.comments.update((current) => ({ ...current, [debtId]: comment }));
  }

  commentValue(debtId: string, fallbackComment: string): string {
    return this.comments()[debtId] ?? fallbackComment ?? '';
  }

  updatePageSize(value: number | string): void {
    const nextSize = Number(value);
    this.pageSize.set(Number.isFinite(nextSize) && nextSize > 0 ? nextSize : 10);
    this.currentPage.set(1);
  }

  previousPage(): void {
    this.currentPage.update((page) => Math.max(1, page - 1));
  }

  nextPage(): void {
    this.currentPage.update((page) => Math.min(this.totalPages(), page + 1));
  }

  customerTypeLabel(type: string): string {
    return type === 'SUBSCRIBER' || type.toLowerCase().includes('abonn') ? 'Abonne' : 'Client';
  }

  customerTypeChipClass(type: string): string {
    return this.customerTypeLabel(type) === 'Abonne' ? 'type-chip subscriber-chip' : 'type-chip walk-in-chip';
  }
}
