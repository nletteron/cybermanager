import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs';

import type { Customer, CustomerDetails, CustomerPurchase } from '../models/customer.models';
import { CustomersApiService } from '../services/customers-api.service';
import type { Product } from '../../products/models/product.models';
import { ProductsApiService } from '../../products/services/products-api.service';
import { SalesApiService } from '../../sales/services/sales-api.service';
import type { SubscriptionOffer } from '../../subscriptions/models/subscription-offer.models';
import { SubscriptionOffersApiService } from '../../subscriptions/services/subscription-offers-api.service';

@Component({
  selector: 'app-customers-page',
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <section class="page">
      <header class="hero">
        <div>
          <p class="eyebrow">Clientele</p>
          <h2>Clients</h2>
          <div class="hero-actions">
            <button type="button" class="ghost filter-toggle" (click)="showFilters.set(!showFilters())" [attr.aria-expanded]="showFilters()">
              <span class="filter-icon" aria-hidden="true"></span>
              <span>Filtres</span>
            </button>
            <button *ngIf="!isPanelOpen()" type="button" class="secondary" (click)="openCreatePanel()">Creer un nouveau client</button>
          </div>
        </div>
        <p class="lede">Les clients journaliers et abonnes crees depuis l'ecran sessions apparaissent ici. Cette page permet aussi de creer des clients et des abonnes.</p>
      </header>

      <p class="error" *ngIf="error()">{{ error() }}</p>
      <p class="muted" *ngIf="notice()">{{ notice() }}</p>

      <div class="filters-grid collapsible" [class.is-collapsed]="!showFilters()" [formGroup]="filters">
        <label class="field">
          <span>Recherche</span>
          <input formControlName="term" placeholder="Filtrer par nom" />
        </label>

        <label class="field">
          <span>Clients par page</span>
          <select formControlName="pageSize">
            <option *ngFor="let size of pageSizeOptions" [value]="size">{{ size }}</option>
          </select>
        </label>
      </div>

      <div class="grid" [class.panel-open]="isPanelOpen()">
        <article class="panel list-panel">
          <div class="panel-header">
            <div class="panel-actions">
              <p class="summary">
                {{ filteredCustomers().length }} client{{ filteredCustomers().length > 1 ? 's' : '' }}
                <span *ngIf="filteredCustomers().length !== customers().length">sur {{ customers().length }}</span>
              </p>
              <div class="pager" *ngIf="totalPages() > 1">
                <button type="button" class="ghost" (click)="previousPage()" [disabled]="currentPage() === 1">Precedent</button>
                <span>Page {{ currentPage() }} / {{ totalPages() }}</span>
                <button type="button" class="ghost" (click)="nextPage()" [disabled]="currentPage() === totalPages()">Suivant</button>
              </div>
            </div>
          </div>

          <table class="table">
            <thead>
              <tr>
                <th>Nom</th>
                <th>Type</th>
                <th>Abonnement</th>
                <th>Credit</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let item of paginatedCustomers()" [class.active]="selected()?.customerId === item.customerId">
                <td><strong>{{ item.name }}</strong></td>
                <td><span [class]="customerTypeChipClass(item.type)">{{ customerTypeLabel(item.type) }}</span></td>
                <td>{{ item.type === 'SUBSCRIBER' ? 'Actif' : 'Aucun' }}</td>
                <td>{{ item.remainingMinutes }} min</td>
                <td><button type="button" (click)="open(item.customerId)">Ouvrir</button></td>
              </tr>

              <tr *ngIf="paginatedCustomers().length === 0">
                <td colspan="5" class="empty">Aucun client pour ce filtre.</td>
              </tr>
            </tbody>
          </table>
        </article>

        <article class="panel side-panel stack" [class.open]="isPanelOpen()" [attr.aria-hidden]="!isPanelOpen()">
          <div class="side-panel-header">
            <h3>{{ panelMode() === 'edit' ? 'Fiche client' : 'Creation client' }}</h3>
            <button type="button" class="icon-button" (click)="closePanel()">Fermer</button>
          </div>

          <section class="stack" *ngIf="panelMode() === 'create'; else editPanel">
            <form [formGroup]="createForm" (ngSubmit)="createCustomer()" class="stack">
              <label class="field">
                <span>Nom du client</span>
                <input formControlName="name" placeholder="Ex. Nadia Benali" />
              </label>
              <label class="field">
                <span>Type de client</span>
                <select formControlName="type">
                  <option value="WALK_IN">Client</option>
                  <option value="SUBSCRIBER">Abonne</option>
                </select>
              </label>
              <label class="field" *ngIf="createForm.controls.type.value === 'SUBSCRIBER'">
                <span>Offre d'abonnement initiale</span>
                <select formControlName="subscriptionOfferId">
                  <option value="">Choisir une offre</option>
                  <option *ngFor="let offer of offers()" [value]="offer.offerId">{{ offer.name }} - {{ offer.includedMinutes }} min</option>
                </select>
              </label>
              <button type="submit" [disabled]="createForm.invalid">Creer</button>
            </form>
          </section>

          <ng-template #editPanel>
            <section class="stack" *ngIf="selected() as customer">
              <form [formGroup]="editForm" (ngSubmit)="save(customer.customerId)" class="stack">
                <label class="field">
                  <span>Nom du client</span>
                  <input formControlName="name" />
                </label>
                <div class="facts">
                  <p><strong>Type :</strong> <span [class]="customerTypeChipClass(customer.type)">{{ customerTypeLabel(customer.type) }}</span></p>
                  <p><strong>Abonnements :</strong> {{ customer.currentSubscriptionLabel ?? 'Aucun' }}</p>
                  <p *ngIf="customer.type === 'SUBSCRIBER'" class="muted">Les abonnements se cumulent.</p>
                  <p><strong>Credit disponible :</strong> {{ customer.remainingMinutes }} min</p>
                  <p><strong>Total paye sur tous les achats :</strong> {{ totalPaidPurchases(customer) | number:'1.2-2' }} EUR</p>
                  <p><strong>Total paye sur forfaits et produits :</strong> {{ totalPaidSales(customer) | number:'1.2-2' }} EUR</p>
                  <p *ngIf="customer.debts.length > 0" class="debt-summary"><strong>Dettes :</strong> <span class="money-alert-icon debt-indicator" title="Dettes ouvertes"><span class="bill back"></span><span class="bill front"></span><span class="slash"></span></span></p>
                </div>
                <div class="actions">
                  <button type="submit" [disabled]="editForm.invalid">Enregistrer</button>
                  <button type="button" class="secondary" (click)="openActionModal('subscription')">Ajouter un abonnement</button>
                  <button type="button" class="secondary" (click)="openActionModal('product')">Vendre un produit</button>
                </div>
              </form>

              <div class="stack">
                <h4>Achats du client</h4>
                <table class="table compact" *ngIf="customerPurchaseEntries(customer).length > 0; else emptyPurchases">
                  <thead>
                    <tr><th>Quand</th><th>Type</th><th>Detail</th><th>Total</th></tr>
                  </thead>
                  <tbody>
                    <tr *ngFor="let entry of customerPurchaseEntries(customer)" [class.debt-row]="entry.isDebt">
                      <td>{{ entry.occurredAt | date:'dd/MM/yyyy HH:mm' }}</td>
                      <td>{{ entry.type }}</td>
                      <td>{{ entry.detail }}</td>
                      <td><strong>{{ entry.total | number:'1.2-2' }} EUR</strong></td>
                    </tr>
                  </tbody>
                  <tfoot>
                    <tr>
                      <td colspan="3"><strong>Total paye</strong></td>
                      <td><strong>{{ totalPaidPurchases(customer) | number:'1.2-2' }} EUR</strong></td>
                    </tr>
                  </tfoot>
                </table>
                <ng-template #emptyPurchases><p class="muted">Aucun achat ni dette ouverte.</p></ng-template>
              </div>
            </section>
          </ng-template>
        </article>
      </div>

      <div class="modal-backdrop" *ngIf="actionModal() && selected() as customer" (click)="closeActionModal()">
        <div class="confirm-modal" (click)="$event.stopPropagation()">
          <div class="side-panel-header">
            <div>
              <h3>{{ actionModal() === 'subscription' ? 'Ajouter un abonnement' : 'Vendre un produit' }}</h3>
              <p class="muted">{{ customer.name }}</p>
            </div>
            <button type="button" class="icon-button" (click)="closeActionModal()">Fermer</button>
          </div>

          <form *ngIf="actionModal() === 'subscription'; else productModal" [formGroup]="subscriptionSaleForm" (ngSubmit)="sellSubscription(customer.customerId)" class="stack confirm-body">
            <label class="field">
              <span>Offre</span>
              <select formControlName="subscriptionOfferId">
                <option value="">Choisir une offre</option>
                <option *ngFor="let offer of offers()" [value]="offer.offerId">{{ offer.name }} - {{ offer.includedMinutes }} min</option>
              </select>
            </label>
            <label class="field">
              <span>Quantite</span>
              <input type="number" min="1" formControlName="quantity" />
            </label>
            <label class="checkbox">
              <input type="checkbox" formControlName="createDebt" />
              <span>Creer une dette au lieu d'encaisser</span>
            </label>
            <button type="submit" [disabled]="subscriptionSaleForm.invalid">Vendre l'abonnement</button>
          </form>

          <ng-template #productModal>
            <form [formGroup]="productSaleForm" (ngSubmit)="sellProduct(customer.customerId)" class="stack confirm-body">
              <label class="field">
                <span>Produit</span>
                <select formControlName="productId">
                  <option value="">Choisir un produit</option>
                  <option *ngFor="let product of products()" [value]="product.productId">{{ product.name }} - {{ product.price | number:'1.2-2' }} EUR</option>
                </select>
              </label>
              <label class="field">
                <span>Quantite</span>
                <input type="number" min="1" formControlName="quantity" />
              </label>
              <label class="checkbox">
                <input type="checkbox" formControlName="createDebt" />
                <span>Creer une dette au lieu d'encaisser</span>
              </label>
              <button type="submit" [disabled]="productSaleForm.invalid">Vendre le produit</button>
            </form>
          </ng-template>
        </div>
      </div>
    </section>
  `,
  styleUrl: './customers-page.component.css',
})
export class CustomersPageComponent {
  private readonly api = inject(CustomersApiService);
  private readonly productsApi = inject(ProductsApiService);
  private readonly salesApi = inject(SalesApiService);
  private readonly offersApi = inject(SubscriptionOffersApiService);
  private readonly fb = inject(FormBuilder);

  readonly pageSizeOptions = [5, 10, 20, 50];
  readonly customers = signal<Customer[]>([]);
  readonly offers = signal<SubscriptionOffer[]>([]);
  readonly products = signal<Product[]>([]);
  readonly selected = signal<CustomerDetails | null>(null);
  readonly panelMode = signal<'create' | 'edit'>('create');
  readonly actionModal = signal<'subscription' | 'product' | null>(null);
  readonly isPanelOpen = signal(false);
  readonly showFilters = signal(false);
  readonly page = signal(1);
  readonly error = signal('');
  readonly notice = signal('');

  readonly filters = this.fb.nonNullable.group({ term: [''], pageSize: [10] });
  readonly filterState = signal(this.filters.getRawValue());
  readonly createForm = this.fb.nonNullable.group({
    name: ['', Validators.required],
    type: ['WALK_IN' as 'WALK_IN' | 'SUBSCRIBER'],
    subscriptionOfferId: [''],
  });
  readonly editForm = this.fb.nonNullable.group({ name: ['', Validators.required] });
  readonly subscriptionSaleForm = this.fb.nonNullable.group({
    subscriptionOfferId: ['', Validators.required],
    quantity: [1, [Validators.required, Validators.min(1)]],
    createDebt: [false],
  });
  readonly productSaleForm = this.fb.nonNullable.group({
    productId: ['', Validators.required],
    quantity: [1, [Validators.required, Validators.min(1)]],
    createDebt: [false],
  });

  readonly filteredCustomers = computed(() => {
    const term = this.filterState().term.trim().toLocaleLowerCase();
    return this.customers().filter((item) => term.length === 0 || item.name.toLocaleLowerCase().includes(term));
  });

  readonly totalPages = computed(() => {
    const pageSize = Number(this.filterState().pageSize) || 10;
    return Math.max(1, Math.ceil(this.filteredCustomers().length / pageSize));
  });

  readonly currentPage = computed(() => Math.min(this.page(), this.totalPages()));

  readonly paginatedCustomers = computed(() => {
    const pageSize = Number(this.filterState().pageSize) || 10;
    const start = (this.currentPage() - 1) * pageSize;
    return this.filteredCustomers().slice(start, start + pageSize);
  });

  constructor() {
    this.load();
    this.loadOffers();
    this.productsApi.search('', 'ACTIVE', '').subscribe((products) => this.products.set(products));

    this.filters.controls.term.valueChanges.pipe(
      debounceTime(150),
      distinctUntilChanged(),
      takeUntilDestroyed(),
    ).subscribe(() => {
      this.filterState.set({
        term: this.filters.controls.term.value ?? '',
        pageSize: Number(this.filters.controls.pageSize.value ?? 10),
      });
      this.page.set(1);
    });

    this.filters.controls.pageSize.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => {
      this.filterState.set({
        term: this.filters.controls.term.value ?? '',
        pageSize: Number(this.filters.controls.pageSize.value ?? 10),
      });
      this.page.set(1);
    });
  }

  load(): void {
    this.api.search('').subscribe({
      next: (items) => {
        this.customers.set(items);
        this.error.set('');
      },
      error: (error: HttpErrorResponse) => this.error.set(error.error?.message ?? 'Chargement impossible'),
    });
  }

  loadOffers(): void {
    this.offersApi.search('', 'ACTIVE').subscribe({
      next: (offers) => this.offers.set(offers),
      error: (error: HttpErrorResponse) => this.error.set(error.error?.message ?? 'Chargement des offres impossible'),
    });
  }

  openCreatePanel(): void {
    this.panelMode.set('create');
    this.selected.set(null);
    this.createForm.reset({ name: '', type: 'WALK_IN', subscriptionOfferId: '' });
    this.loadOffers();
    this.error.set('');
    this.isPanelOpen.set(true);
  }

  closePanel(): void {
    this.selected.set(null);
    this.isPanelOpen.set(false);
    this.actionModal.set(null);
    this.editForm.reset({ name: '' });
    this.error.set('');
  }

  openActionModal(mode: 'subscription' | 'product'): void {
    this.actionModal.set(mode);
    if (mode === 'subscription') {
      this.loadOffers();
    }
    this.error.set('');
    this.notice.set('');
  }

  closeActionModal(): void {
    this.actionModal.set(null);
    this.subscriptionSaleForm.reset({ subscriptionOfferId: '', quantity: 1, createDebt: false });
    this.productSaleForm.reset({ productId: '', quantity: 1, createDebt: false });
  }

  previousPage(): void {
    this.page.update((page) => Math.max(1, page - 1));
  }

  nextPage(): void {
    this.page.update((page) => Math.min(this.totalPages(), page + 1));
  }

  createCustomer(): void {
    const payload = this.createForm.getRawValue();
    this.error.set('');
    this.notice.set('');
    if (!payload.name.trim()) {
      this.error.set('Le nom du client est requis');
      return;
    }
    if (payload.type === 'SUBSCRIBER' && !payload.subscriptionOfferId) {
      this.error.set('Une offre d abonnement est requise');
      return;
    }
    this.api.create({
      name: payload.name.trim(),
      type: payload.type,
      subscriptionOfferId: payload.type === 'SUBSCRIBER' ? payload.subscriptionOfferId : null,
    }).subscribe({
      next: () => {
        this.notice.set('Client enregistre.');
        this.closePanel();
        this.load();
      },
      error: (error: HttpErrorResponse) => this.error.set(this.resolveHttpError(error, 'Creation impossible')),
    });
  }

  open(customerId: string): void {
    this.api.get(customerId).subscribe({
      next: (customer) => {
        this.selected.set(customer);
        this.panelMode.set('edit');
        this.editForm.patchValue({ name: customer.name });
        this.subscriptionSaleForm.reset({ subscriptionOfferId: '', quantity: 1, createDebt: false });
        this.productSaleForm.reset({ productId: '', quantity: 1, createDebt: false });
        this.error.set('');
        this.notice.set('');
        this.isPanelOpen.set(true);
      },
      error: (error: HttpErrorResponse) => this.error.set(this.resolveHttpError(error, 'Chargement de la fiche impossible')),
    });
  }

  save(customerId: string): void {
    const payload = this.editForm.getRawValue();
    this.error.set('');
    this.api.update(customerId, payload).subscribe({
      next: () => {
        this.notice.set('Client mis a jour.');
        this.open(customerId);
        this.load();
      },
      error: (error: HttpErrorResponse) => this.error.set(this.resolveHttpError(error, 'Sauvegarde impossible')),
    });
  }

  sellSubscription(customerId: string): void {
    if (this.subscriptionSaleForm.invalid) {
      this.subscriptionSaleForm.markAllAsTouched();
      return;
    }
    this.error.set('');
    const payload = this.subscriptionSaleForm.getRawValue();
    this.salesApi.subscriptionSale({
      customerId,
      subscriptionOfferId: payload.subscriptionOfferId,
      quantity: payload.quantity,
      createDebt: payload.createDebt,
    }).subscribe({
      next: () => {
        this.notice.set(payload.createDebt ? 'Abonnement ajoute en dette.' : 'Abonnement ajoute.');
        this.closeActionModal();
        this.open(customerId);
        this.load();
      },
      error: (error: HttpErrorResponse) => this.error.set(this.resolveHttpError(error, 'Vente d abonnement impossible')),
    });
  }

  sellProduct(customerId: string): void {
    if (this.productSaleForm.invalid) {
      this.productSaleForm.markAllAsTouched();
      return;
    }
    this.error.set('');
    const payload = this.productSaleForm.getRawValue();
    this.salesApi.productSale({
      customerId,
      lines: [{ productId: payload.productId, quantity: payload.quantity }],
      createDebt: payload.createDebt,
    }).subscribe({
      next: () => {
        this.notice.set(payload.createDebt ? 'Produit ajoute en dette.' : 'Produit enregistre.');
        this.closeActionModal();
        this.open(customerId);
        this.load();
      },
      error: (error: HttpErrorResponse) => this.error.set(this.resolveHttpError(error, 'Vente produit impossible')),
    });
  }

  totalPaidPurchases(customer: CustomerDetails): number {
    return customer.purchases
      .filter((purchase) => !purchase.openDebt)
      .reduce((total, purchase) => total + purchase.totalAmount, 0);
  }

  totalPaidSales(customer: CustomerDetails): number {
    return customer.purchases
      .filter((purchase) => !purchase.openDebt)
      .filter((purchase) => purchase.type === 'SUBSCRIPTION' || purchase.type === 'PRODUCTS')
      .reduce((total, purchase) => total + purchase.totalAmount, 0);
  }

  customerPurchaseEntries(customer: CustomerDetails): Array<{ occurredAt: string; type: string; detail: string; total: number; isDebt: boolean }> {
    const uniquePurchases = Array.from(new Map(customer.purchases.map((purchase) => [purchase.saleId, purchase] as const)).values());
    const paidPurchases = uniquePurchases
      .filter((purchase) => !purchase.openDebt)
      .map((purchase) => ({
        occurredAt: purchase.soldAt,
        type: this.purchaseTypeLabel(purchase.type),
        detail: purchase.label,
        total: purchase.totalAmount,
        isDebt: false,
      }));
    const openDebts = customer.debts
      .filter((debt) => debt.status === 'OPEN')
      .map((debt) => ({
        occurredAt: debt.createdAt,
        type: this.debtTypeLabel(debt.label),
        detail: debt.label,
        total: debt.amount,
        isDebt: true,
      }));

    return [...paidPurchases, ...openDebts]
      .sort((left, right) => new Date(right.occurredAt).getTime() - new Date(left.occurredAt).getTime());
  }

  customerTypeLabel(type: string): string {
    return type === 'SUBSCRIBER' || type.toLowerCase().includes('abonn') ? 'Abonne' : 'Client';
  }

  customerTypeChipClass(type: string): string {
    return this.customerTypeLabel(type) === 'Abonne' ? 'type-chip subscriber-chip' : 'type-chip walk-in-chip';
  }

  private purchaseTypeLabel(type: CustomerPurchase['type']): string {
    switch (type) {
      case 'PRODUCTS':
        return 'Produit';
      case 'SUBSCRIPTION':
        return 'Abonnement';
      case 'CONNECTION_TIME':
        return 'Connexion';
      default:
        return type;
    }
  }

  private debtTypeLabel(label: string): string {
    const normalized = label.toLowerCase();
    if (normalized.startsWith('vente produits')) {
      return 'Produit';
    }
    if (normalized.startsWith('vente abonnements')) {
      return 'Abonnement';
    }
    if (normalized.startsWith('session du') || normalized.startsWith('depassement abonnement du')) {
      return 'Connexion';
    }
    return 'Dette';
  }

  private resolveHttpError(error: HttpErrorResponse, fallback: string): string {
    if (typeof error.error === 'string' && error.error.trim()) {
      return error.error;
    }
    if (error.error?.message) {
      return error.error.message;
    }
    if (error.status === 0) {
      return 'Serveur inaccessible ou non redemarre.';
    }
    if (error.status === 401) {
      return 'Session expiree. Reconnecte-toi.';
    }
    if (error.status === 403) {
      return 'Action reservee a un administrateur.';
    }
    if (error.status === 404) {
      return 'Endpoint introuvable. Redemarre probablement le backend.';
    }
    return fallback;
  }
}
