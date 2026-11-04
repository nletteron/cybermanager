import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { SessionDisplaySettingsService } from '../services/session-display-settings.service';

@Component({
  selector: 'app-session-display-settings-page',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <section class="page">
      <header class="hero">
        <div>
          <p class="eyebrow">Reglages sessions</p>
          <h2>Affichage des listes</h2>
        </div>
        <p class="lede">Definis combien de lignes afficher par page pour les sessions en cours et les sessions du jour.</p>
      </header>

      <article class="panel">
        <form [formGroup]="form" (ngSubmit)="save()" class="stack">
          <p class="success" *ngIf="notice()">{{ notice() }}</p>
          <label class="field">
            <span>Nombre de sessions en cours par page</span>
            <input type="number" min="1" formControlName="currentPageSize" />
          </label>
          <label class="field">
            <span>Nombre de sessions du jour par page</span>
            <input type="number" min="1" formControlName="dayPageSize" />
          </label>
          <div class="actions">
            <button type="submit" [disabled]="form.invalid">Enregistrer</button>
            <a routerLink="/sessions" class="ghost">Retour aux sessions</a>
          </div>
        </form>
      </article>
    </section>
  `,
  styleUrl: './session-display-settings-page.component.css',
})
export class SessionDisplaySettingsPageComponent {
  private readonly settingsService = inject(SessionDisplaySettingsService);
  private readonly fb = inject(FormBuilder);
  readonly notice = signal('');

  readonly form = this.fb.nonNullable.group({
    currentPageSize: [this.settingsService.settings().currentPageSize, [Validators.required, Validators.min(1)]],
    dayPageSize: [this.settingsService.settings().dayPageSize, [Validators.required, Validators.min(1)]],
  });

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const saved = this.settingsService.update({
      currentPageSize: Number(this.form.getRawValue().currentPageSize),
      dayPageSize: Number(this.form.getRawValue().dayPageSize),
    });
    this.form.setValue(saved);
    this.notice.set('Configuration enregistree.');
  }
}
