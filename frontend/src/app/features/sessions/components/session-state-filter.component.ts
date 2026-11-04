import { Component, computed, input, output, signal } from '@angular/core';

import { SESSION_STATES, type SessionState } from '../models/session-table.models';

@Component({
  selector: 'app-session-state-filter',
  template: `
    <div class="field">
      <span>État</span>
      <details #menu (toggle)="onToggle(menu)" (keydown.escape)="close(menu, $event)">
        <summary [attr.aria-label]="'État : ' + label(value())">{{ label(value()) }}</summary>
        <div class="state-menu">
          <label class="field">
            <span>Rechercher un état</span>
            <input type="search" [value]="search()" (input)="updateSearch($event)" autocomplete="off" />
          </label>
          <div class="state-options" role="group" aria-label="États disponibles">
            <button type="button" class="ghost" [attr.aria-pressed]="value() === ''" (click)="select('', menu)">Tous les états</button>
            @for (state of filteredStates(); track state) {
              <button type="button" class="ghost" [attr.aria-pressed]="value() === state" (click)="select(state, menu)">{{ label(state) }}</button>
            } @empty {
              <p class="meta" role="status">Aucun état trouvé.</p>
            }
          </div>
        </div>
      </details>
    </div>
  `,
  styleUrl: './session-state-filter.component.css',
})
export class SessionStateFilterComponent {
  readonly value = input<SessionState | ''>('');
  readonly valueChange = output<SessionState | ''>();
  readonly search = signal('');
  readonly filteredStates = computed(() => {
    const term = this.normalize(this.search());
    return SESSION_STATES.filter((state) => this.normalize(this.label(state)).includes(term));
  });

  label(state: SessionState | ''): string {
    if (state === 'A payer') return 'À payer';
    if (state === 'Terminee') return 'Terminée';
    return state || 'Tous les états';
  }

  updateSearch(event: Event): void {
    this.search.set((event.target as HTMLInputElement).value);
  }

  select(state: SessionState | '', menu: HTMLDetailsElement): void {
    this.valueChange.emit(state);
    this.close(menu);
  }

  close(menu: HTMLDetailsElement, event?: Event): void {
    event?.preventDefault();
    event?.stopPropagation();
    menu.open = false;
    this.search.set('');
    menu.querySelector('summary')?.focus();
  }

  onToggle(menu: HTMLDetailsElement): void {
    if (!menu.open) this.search.set('');
  }

  private normalize(value: string): string {
    return value.normalize('NFD').replace(/\p{Diacritic}/gu, '').trim().toLocaleLowerCase('fr');
  }
}
