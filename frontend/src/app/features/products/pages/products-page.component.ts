import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { CmButtonComponent, CmInputComponent, CmSelectComponent, CmTextareaComponent } from '../../../shared/components/design-system';
import type { Product } from '../models/product.models';
import { ProductsApiService } from '../services/products-api.service';
import type { Category } from '../../categories/models/category.models';
import { CategoriesApiService } from '../../categories/services/categories-api.service';

@Component({
  selector: 'app-products-page',
  imports: [CommonModule, ReactiveFormsModule, CmButtonComponent, CmInputComponent, CmSelectComponent, CmTextareaComponent],
  template: `
    <section class="page crud-page products-page">
      <header class="hero">
        <div>
          <p class="eyebrow">Bounded context product</p>
          <h2>Catalogue produits</h2>
        </div>
      </header>

      <p class="error" *ngIf="error()">{{ error() }}</p>

      <div class="toolbar crud-toolbar">
        <div class="section-title-group">
          <cm-button variant="ghost" (click)="showFilters.set(!showFilters())" [attr.aria-expanded]="showFilters()">
            <span class="filter-icon" aria-hidden="true"></span>
            <span>Filtres</span>
          </cm-button>
          <cm-button (click)="openCreatePanel()">Creer un nouveau produit</cm-button>
        </div>
      </div>

      <div class="filters-grid collapsible" [class.is-collapsed]="!showFilters()" [formGroup]="filters">
        <cm-input label="Recherche" formControlName="term" placeholder="Nom ou description" />

        <cm-input label="Categorie" formControlName="category" placeholder="Categorie" />

        <cm-select label="Statut" formControlName="status">
          <option value="">Tous les statuts</option>
          <option value="ACTIVE">Actif</option>
          <option value="INACTIVE">Inactif</option>
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
                {{ filteredProducts().length }} produit{{ filteredProducts().length > 1 ? 's' : '' }}
                <span *ngIf="filteredProducts().length !== products().length">sur {{ products().length }}</span>
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
                  <th>Description</th>
                  <th>Categorie</th>
                  <th>Prix</th>
                  <th>Statut</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let item of paginatedProducts()" [class.active]="selected()?.productId === item.productId">
                  <td><strong>{{ item.name }}</strong></td>
                  <td class="description-cell">{{ item.description || 'Aucune description' }}</td>
                  <td class="muted">{{ item.category || 'Non renseignee' }}</td>
                  <td>{{ item.price | number:'1.2-2' }} EUR</td>
                  <td>
                    <span class="status-badge" [class.active]="item.status === 'ACTIVE'" [class.inactive]="item.status === 'INACTIVE'">
                      {{ statusLabel(item.status) }}
                    </span>
                  </td>
                  <td class="action-cell"><cm-button (click)="edit(item)">Modifier</cm-button></td>
                </tr>

                <tr *ngIf="paginatedProducts().length === 0">
                  <td colspan="6" class="empty">Aucun produit pour ce filtre.</td>
                </tr>
              </tbody>
            </table>
          </div>
        </article>

        <form class="panel side-panel crud-side-panel form crud-form" [class.open]="isPanelOpen()" [formGroup]="form" (ngSubmit)="save()" [attr.aria-hidden]="!isPanelOpen()">
          <div class="side-panel-header">
            <h3>{{ selected() ? 'Edition produit' : 'Creation produit' }}</h3>
            <cm-button variant="icon" (click)="closePanel()">Fermer</cm-button>
          </div>

          <cm-input label="Nom" formControlName="name" placeholder="Nom du produit" />

          <cm-textarea label="Description" formControlName="description" [rows]="3" placeholder="Description courte du produit" />

          <cm-select label="Categorie" formControlName="categoryId">
            <option value="" disabled>Sélectionner une catégorie</option>
            <option *ngFor="let category of categories()" [value]="category.categoryId">{{ category.name }} (TVA {{ category.vatRate | number:'1.0-2' }} %)</option>
          </cm-select>

          <cm-input label="Prix" formControlName="price" type="number" valueKind="number" step="0.01" min="0.01" placeholder="Prix" />

          <div class="actions">
            <cm-button type="submit" [disabled]="form.invalid">Enregistrer</cm-button>
            <cm-button *ngIf="selected()" variant="secondary" (click)="toggleStatus()">
              {{ selected()?.status === 'ACTIVE' ? 'Desactiver' : 'Activer' }}
            </cm-button>
            <cm-button *ngIf="selected()" variant="danger" (click)="deleteProduct()">Supprimer</cm-button>
          </div>
        </form>
      </div>
    </section>
  `,
  styleUrl: './products-page.component.css',
})
export class ProductsPageComponent {
  private readonly api = inject(ProductsApiService);
  private readonly categoriesApi = inject(CategoriesApiService);
  private readonly fb = inject(FormBuilder);

  readonly pageSizeOptions = [5, 10, 20, 50];
  readonly products = signal<Product[]>([]);
  readonly categories = signal<Category[]>([]);
  readonly selected = signal<Product | null>(null);
  readonly isPanelOpen = signal(false);
  readonly showFilters = signal(false);
  readonly page = signal(1);
  readonly error = signal('');
  readonly filters = this.fb.nonNullable.group({ term: [''], status: [''], category: [''], pageSize: [10] });
  readonly filterState = signal(this.filters.getRawValue());
  readonly form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    description: [''],
    categoryId: ['', Validators.required],
    price: [0, [Validators.required, Validators.min(0.01)]],
  });

  readonly filteredProducts = computed(() => {
    const { term, status, category } = this.filterState();
    const normalizedTerm = term.trim().toLocaleLowerCase();
    const normalizedCategory = category.trim().toLocaleLowerCase();

    return this.products().filter((item) => {
      const matchesTerm = normalizedTerm.length === 0
        || item.name.toLocaleLowerCase().includes(normalizedTerm)
        || item.description.toLocaleLowerCase().includes(normalizedTerm);
      const matchesCategory = normalizedCategory.length === 0 || item.category.toLocaleLowerCase().includes(normalizedCategory);
      const matchesStatus = !status || item.status === status;
      return matchesTerm && matchesCategory && matchesStatus;
    });
  });

  readonly totalPages = computed(() => {
    const pageSize = Number(this.filterState().pageSize) || 10;
    return Math.max(1, Math.ceil(this.filteredProducts().length / pageSize));
  });

  readonly currentPage = computed(() => Math.min(this.page(), this.totalPages()));

  readonly paginatedProducts = computed(() => {
    const pageSize = Number(this.filterState().pageSize) || 10;
    const start = (this.currentPage() - 1) * pageSize;
    return this.filteredProducts().slice(start, start + pageSize);
  });

  constructor() {
    this.filters.valueChanges.subscribe((value) => {
      this.filterState.set({
        term: value.term ?? '',
        status: value.status ?? '',
        category: value.category ?? '',
        pageSize: Number(value.pageSize ?? 10),
      });
      this.page.set(1);
    });

    this.load();
    this.loadCategories();
  }

  load(): void {
    this.api.search('', '', '').subscribe({
      next: (items) => {
        this.products.set(items);
        this.error.set('');
      },
      error: (error: HttpErrorResponse) => this.error.set(this.resolveError(error, 'Chargement impossible')),
    });
  }

  loadCategories(): void {
    this.categoriesApi.search().subscribe({
      next: items => this.categories.set(items),
      error: (error: HttpErrorResponse) => this.error.set(this.resolveError(error, 'Chargement des catégories impossible')),
    });
  }

  edit(item: Product): void {
    this.selected.set(item);
    this.form.patchValue({ name: item.name, description: item.description, categoryId: item.categoryId ?? '', price: item.price });
    this.isPanelOpen.set(true);
  }

  openCreatePanel(): void {
    this.selected.set(null);
    this.form.reset({ name: '', description: '', categoryId: '', price: 0 });
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
    const request = current ? this.api.update(current.productId, payload) : this.api.create(payload);
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
      ? this.api.deactivate(current.productId)
      : this.api.activate(current.productId);

    request.subscribe({
      next: (updated) => {
        this.selected.set(updated);
        this.load();
      },
      error: (error: HttpErrorResponse) => this.error.set(this.resolveError(error, 'Mise a jour du statut impossible')),
    });
  }

  deleteProduct(): void {
    const current = this.selected();
    if (!current) {
      return;
    }
    if (!confirm(`Supprimer le produit ${current.name} ?`)) {
      return;
    }

    this.api.delete(current.productId).subscribe({
      next: () => {
        this.closePanel();
        this.load();
      },
      error: (error: HttpErrorResponse) => this.error.set(this.resolveError(error, 'Suppression impossible')),
    });
  }

  statusLabel(status: Product['status']): string {
    return status === 'ACTIVE' ? 'Actif' : 'Inactif';
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
