import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged, map } from 'rxjs';

import type { Customer } from '../../customers/models/customer.models';
import { CustomersApiService } from '../../customers/services/customers-api.service';
import type { Product } from '../../products/models/product.models';
import { ProductsApiService } from '../../products/services/products-api.service';
import type { SubscriptionOffer } from '../../subscriptions/models/subscription-offer.models';
import { SubscriptionOffersApiService } from '../../subscriptions/services/subscription-offers-api.service';
import type { Sale } from '../models/sales.models';
import { SalesApiService } from '../services/sales-api.service';

type SalesPanelMode = 'product' | 'subscription';

@Component({
  selector: 'app-sales-page',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <section class="page">
      <header class="hero">
        <div>
          <p class="eyebrow">Flux ventes</p>
          <h2>Ventes et tarification</h2>
          <div class="hero-actions">
            <button type="button" class="ghost filter-toggle" (click)="showFilters.set(!showFilters())" [attr.aria-expanded]="showFilters()">
              <span class="filter-icon" aria-hidden="true"></span>
              <span>Filtres</span>
            </button>
            <a routerLink="/sales/pricing" [state]="{ fromSales: true }" class="link-button secondary">Configurer les tarifs</a>
            <button type="button" class="secondary" (click)="openPanel('product')">Nouvelle vente</button>
          </div>
        </div>
      </header>

      <p class="error" *ngIf="error()">{{ error() }}</p>

      <div class="filters-grid collapsible" [class.is-collapsed]="!showFilters()" [formGroup]="filters">
          <label class="field">
            <span>Type</span>
            <select formControlName="type">
              <option value="">Tous les types</option>
              <option value="PRODUCTS">Produits</option>
              <option value="SUBSCRIPTION">Abonnement</option>
            </select>
          </label>

        <label class="field">
          <span>Article</span>
          <input formControlName="article" placeholder="Nom de l'article" />
        </label>
      </div>

      <div class="grid" [class.panel-open]="isPanelOpen()">
        <article class="panel list-panel">
          <div class="panel-header">
            <div>
              <h3>Ventes du jour</h3>
              <p>{{ filteredSales().length }} vente(s)</p>
            </div>
          </div>

          <table class="table">
            <thead>
              <tr>
                <th>Heure</th>
                <th>Type</th>
                <th>Articles</th>
                <th>Montant</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let sale of filteredSales()">
                <td>{{ sale.soldAt | date:'dd/MM/yyyy HH:mm' }}</td>
                <td>{{ saleTypeLabel(sale.type) }}</td>
                <td class="muted">{{ sale.lines[0]?.label ?? 'Vente' }}</td>
                <td>{{ sale.totalAmount | number:'1.2-2' }} EUR</td>
              </tr>
              <tr *ngIf="filteredSales().length === 0">
                <td colspan="4" class="empty">Aucune vente enregistree aujourd'hui.</td>
              </tr>
            </tbody>
          </table>

        </article>

        <article class="panel side-panel stack" [class.open]="isPanelOpen()" [attr.aria-hidden]="!isPanelOpen()">
          <div class="side-panel-header">
            <div>
              <h3>{{ panelTitle() }}</h3>
              <p>Recherche du client ou de l'abonne par nom, sans liste exhaustive.</p>
            </div>
            <button type="button" class="icon-button" (click)="closePanel()">Fermer</button>
          </div>

          <div class="mode-switches">
            <button type="button" [class.secondary]="panelMode() !== 'product'" (click)="openPanel('product')">Produit</button>
            <button type="button" [class.secondary]="panelMode() !== 'subscription'" (click)="openPanel('subscription')">Abonnement</button>
          </div>

          <label class="field">
            <span>Client ou abonne</span>
            <input [formControl]="customerSearchForm.controls.term" placeholder="Saisir le nom du client" />
          </label>

          <div class="selected-customer" *ngIf="selectedCustomer() as customer">
            <strong>{{ customer.name }}</strong>
            <span [class]="customerTypeChipClass(customer.type)">{{ customerTypeLabel(customer.type) }}</span>
            <button type="button" class="ghost" (click)="clearSelectedCustomer()">Changer</button>
          </div>

          <div class="suggestions" *ngIf="customerSuggestions().length > 0 && !selectedCustomer()">
            <button type="button" class="suggestion" *ngFor="let customer of customerSuggestions()" (click)="selectCustomer(customer)">
              <strong>{{ customer.name }}</strong>
              <span [class]="customerTypeChipClass(customer.type)">{{ customerTypeLabel(customer.type) }}</span>
            </button>
          </div>

          <form class="stack" [formGroup]="productSaleForm" (ngSubmit)="sellProduct()" *ngIf="panelMode() === 'product'">
            <label class="field">
              <span>Produit</span>
              <select formControlName="productId">
                <option value="">Choisir un produit</option>
                <option *ngFor="let item of products()" [value]="item.productId">{{ item.name }} - {{ item.price | number:'1.2-2' }} EUR</option>
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
            <button type="submit" [disabled]="!selectedCustomer() || productSaleForm.invalid">Enregistrer la vente</button>
          </form>

          <form class="stack" [formGroup]="subscriptionSaleForm" (ngSubmit)="sellSubscription()" *ngIf="panelMode() === 'subscription'">
            <label class="field">
              <span>Offre d'abonnement</span>
              <select formControlName="subscriptionOfferId">
                <option value="">Choisir une offre</option>
                <option *ngFor="let item of offers()" [value]="item.offerId">{{ item.name }} - {{ item.includedMinutes }} min</option>
              </select>
            </label>
            <label class="field">
              <span>Quantite</span>
              <input type="number" min="1" formControlName="quantity" />
            </label>
            <p class="helper">Les abonnements se cumulent sur le credit existant.</p>
            <label class="checkbox">
              <input type="checkbox" formControlName="createDebt" />
              <span>Creer une dette au lieu d'encaisser</span>
            </label>
            <button type="submit" [disabled]="!selectedCustomer() || subscriptionSaleForm.invalid">Enregistrer la vente</button>
          </form>
        </article>
      </div>
    </section>
  `,
  styleUrl: './sales-page.component.css',
})
export class SalesPageComponent {
  private readonly customersApi = inject(CustomersApiService);
  private readonly productsApi = inject(ProductsApiService);
  private readonly offersApi = inject(SubscriptionOffersApiService);
  private readonly salesApi = inject(SalesApiService);
  private readonly fb = inject(FormBuilder);

  readonly products = signal<Product[]>([]);
  readonly offers = signal<SubscriptionOffer[]>([]);
  readonly sales = signal<Sale[]>([]);
  readonly customerSuggestions = signal<Customer[]>([]);
  readonly selectedCustomer = signal<Customer | null>(null);
  readonly panelMode = signal<SalesPanelMode>('product');
  readonly isPanelOpen = signal(false);
  readonly showFilters = signal(false);
  readonly error = signal('');
  readonly filters = this.fb.nonNullable.group({ type: [''], article: [''] });
  readonly filterState = signal(this.filters.getRawValue());

  readonly customerSearchForm = this.fb.nonNullable.group({ term: [''] });
  readonly productSaleForm = this.fb.nonNullable.group({ productId: ['', Validators.required], quantity: [1, [Validators.required, Validators.min(1)]], createDebt: [false] });
  readonly subscriptionSaleForm = this.fb.nonNullable.group({ subscriptionOfferId: ['', Validators.required], quantity: [1, [Validators.required, Validators.min(1)]], createDebt: [false] });
  readonly filteredSales = computed(() => {
    const { type, article } = this.filterState();
    const normalizedArticle = article.trim().toLocaleLowerCase();

    return this.sales().filter((sale) => {
      const matchesType = !type || sale.type === type;
      const matchesArticle = !normalizedArticle
        || sale.lines.some((line) => line.label.toLocaleLowerCase().includes(normalizedArticle));
      return matchesType && matchesArticle;
    });
  });

  constructor() {
    this.productsApi.search('', 'ACTIVE', '').subscribe((value) => this.products.set(value));
    this.offersApi.search('', 'ACTIVE').subscribe((value) => this.offers.set(value));
    this.reload();
    this.filters.valueChanges.pipe(takeUntilDestroyed()).subscribe((value) => {
      this.filterState.set({
        type: value.type ?? '',
        article: value.article ?? '',
      });
    });

    this.customerSearchForm.controls.term.valueChanges.pipe(
      map((value) => value.trim()),
      debounceTime(150),
      distinctUntilChanged(),
      takeUntilDestroyed(),
    ).subscribe((term) => this.autocompleteCustomers(term));
  }

  panelTitle(): string {
    if (this.panelMode() === 'subscription') {
      return "Vente d'abonnement";
    }
    return 'Vente de produit';
  }

  openPanel(mode: SalesPanelMode): void {
    this.panelMode.set(mode);
    if (mode === 'subscription') {
      this.offersApi.search('', 'ACTIVE').subscribe((value) => this.offers.set(value));
    }
    this.isPanelOpen.set(true);
    this.error.set('');
  }

  closePanel(): void {
    this.isPanelOpen.set(false);
    this.clearSelectedCustomer();
    this.resetForms();
    this.error.set('');
  }

  clearSelectedCustomer(): void {
    this.selectedCustomer.set(null);
    this.customerSearchForm.reset({ term: '' });
    this.customerSuggestions.set([]);
  }

  selectCustomer(customer: Customer): void {
    this.selectedCustomer.set(customer);
    this.customerSearchForm.patchValue({ term: customer.name }, { emitEvent: false });
    this.customerSuggestions.set([]);
  }

  autocompleteCustomers(term: string): void {
    if (term.length < 2) {
      if (!this.selectedCustomer()) {
        this.customerSuggestions.set([]);
      }
      return;
    }
    this.customersApi.search(term).subscribe({
      next: (customers) => {
        this.customerSuggestions.set(customers);
        this.error.set('');
      },
      error: (error: HttpErrorResponse) => this.error.set(error.error?.message ?? 'Recherche des clients impossible'),
    });
  }

  reload(): void {
    this.salesApi.day().subscribe((value) => this.sales.set(value));
  }

  sellProduct(): void {
    const customer = this.selectedCustomer();
    const { productId, quantity, createDebt } = this.productSaleForm.getRawValue();
    if (!customer) {
      this.error.set('Selectionne un client ou un abonne.');
      return;
    }
    this.salesApi.productSale({ customerId: customer.customerId, lines: [{ productId, quantity }], createDebt }).subscribe({
      next: () => {
        this.closePanel();
        this.reload();
      },
      error: (error: HttpErrorResponse) => this.error.set(error.error?.message ?? 'Vente produit impossible'),
    });
  }

  sellSubscription(): void {
    const customer = this.selectedCustomer();
    const { subscriptionOfferId, quantity, createDebt } = this.subscriptionSaleForm.getRawValue();
    if (!customer) {
      this.error.set('Selectionne un client ou un abonne.');
      return;
    }
    this.salesApi.subscriptionSale({ customerId: customer.customerId, subscriptionOfferId, quantity, createDebt }).subscribe({
      next: () => {
        this.closePanel();
        this.reload();
      },
      error: (error: HttpErrorResponse) => this.error.set(error.error?.message ?? 'Vente abonnement impossible'),
    });
  }

  private resetForms(): void {
    this.productSaleForm.reset({ productId: '', quantity: 1, createDebt: false });
    this.subscriptionSaleForm.reset({ subscriptionOfferId: '', quantity: 1, createDebt: false });
  }

  saleTypeLabel(type: string): string {
    switch (type) {
      case 'PRODUCTS':
        return 'Produits';
      case 'CONNECTION_TIME':
        return 'Temps de connexion';
      case 'SUBSCRIPTION':
        return 'Abonnement';
      default:
        return type;
    }
  }

  customerTypeLabel(type: string): string {
    return type === 'SUBSCRIBER' || type.toLowerCase().includes('abonn') ? 'Abonne' : 'Client';
  }

  customerTypeChipClass(type: string): string {
    return this.customerTypeLabel(type) === 'Abonne' ? 'type-chip subscriber-chip' : 'type-chip walk-in-chip';
  }
}
