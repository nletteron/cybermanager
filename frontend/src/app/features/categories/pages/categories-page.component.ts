import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { CmButtonComponent, CmInputComponent, CmTextareaComponent } from '../../../shared/components/design-system';
import type { Category } from '../models/category.models';
import { CategoriesApiService } from '../services/categories-api.service';

@Component({
  selector: 'app-categories-page',
  imports: [CommonModule, ReactiveFormsModule, CmButtonComponent, CmInputComponent, CmTextareaComponent],
  template: `
    <section class="page">
      <header>
        <p class="eyebrow">Gestion produits</p>
        <h2>Categories</h2>
      </header>

      <p class="success" *ngIf="message()">{{ message() }}</p>
      <p class="error" *ngIf="error()">{{ error() }}</p>

      <div class="toolbar">
        <div class="actions">
          <cm-button variant="ghost" (click)="showFilters.set(!showFilters())" [attr.aria-expanded]="showFilters()">
            <span class="filter-icon" aria-hidden="true"></span>
            <span>Filtres</span>
          </cm-button>
          <cm-button (click)="openCreate()">Creer une nouvelle categorie</cm-button>
        </div>
        <span>{{ filtered().length }} categorie{{ filtered().length > 1 ? 's' : '' }}</span>
      </div>

      <div class="filters-grid collapsible" [class.is-collapsed]="!showFilters()">
        <cm-input label="Recherche" [value]="term()" (input)="term.set($any($event.target).value)" placeholder="Nom ou description" />
      </div>

      <div class="grid" [class.panel-open]="panelOpen()">
        <article class="panel">
          <h3>Liste</h3>
          <table>
            <thead>
              <tr><th>Nom</th><th>Description</th><th>TVA</th></tr>
            </thead>
            <tbody>
              <tr *ngFor="let category of filtered()">
                <td><strong>{{ category.name }}</strong></td>
                <td>{{ category.description }}</td>
                <td>{{ category.vatRate | number:'1.0-2' }} %</td>
              </tr>
              <tr *ngIf="filtered().length === 0">
                <td colspan="3" class="empty">Aucune categorie.</td>
              </tr>
            </tbody>
          </table>
        </article>

        <form class="panel form" *ngIf="panelOpen()" [formGroup]="form" (ngSubmit)="save()">
          <div class="form-header">
            <h3>Creation categorie</h3>
            <cm-button variant="ghost" (click)="panelOpen.set(false)">Fermer</cm-button>
          </div>
          <cm-input label="Nom" formControlName="name" />
          <cm-textarea label="Description" formControlName="description" [rows]="4" />
          <cm-input label="TVA (%)" formControlName="vatRate" type="number" valueKind="number" min="0" max="100" step="0.01" />
          <cm-button type="submit" [disabled]="form.invalid">Enregistrer</cm-button>
        </form>
      </div>
    </section>
  `,
  styleUrl: './categories-page.component.css',
})
export class CategoriesPageComponent {
  private readonly api = inject(CategoriesApiService);
  private readonly fb = inject(FormBuilder);

  readonly categories = signal<Category[]>([]);
  readonly term = signal('');
  readonly showFilters = signal(false);
  readonly panelOpen = signal(false);
  readonly error = signal('');
  readonly message = signal('');
  readonly form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    description: ['', Validators.required],
    vatRate: [null as number | null, [Validators.required, Validators.min(0), Validators.max(100)]],
  });
  readonly filtered = computed(() => {
    const term = this.term().trim().toLocaleLowerCase();
    return this.categories().filter((category) =>
      !term || category.name.toLocaleLowerCase().includes(term) || category.description.toLocaleLowerCase().includes(term),
    );
  });

  constructor() {
    this.load();
  }

  load(): void {
    this.api.search().subscribe({
      next: (value) => {
        this.categories.set(value);
        this.error.set('');
      },
      error: (error) => this.fail(error, 'Chargement impossible'),
    });
  }

  openCreate(): void {
    this.form.reset({ name: '', description: '', vatRate: null });
    this.message.set('');
    this.panelOpen.set(true);
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const raw = this.form.getRawValue();
    this.api.create({ name: raw.name.trim(), description: raw.description.trim(), vatRate: Number(raw.vatRate) }).subscribe({
      next: () => {
        this.panelOpen.set(false);
        this.message.set('Categorie enregistree.');
        this.load();
      },
      error: (error) => this.fail(error, 'Enregistrement impossible'),
    });
  }

  private fail(error: HttpErrorResponse, fallback: string): void {
    this.error.set(typeof error.error?.message === 'string' ? error.error.message : fallback);
  }
}
