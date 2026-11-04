import { Component, DestroyRef, effect, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CustomersApiService } from '../services/customers-api.service';

@Component({
  selector: 'app-customer-comment',
  imports: [ReactiveFormsModule],
  template: `
    <div [formGroup]="form" class="stack">
      <label class="field">
        <span>Commentaire client</span>
        <textarea formControlName="comment" rows="1" maxlength="2000" (blur)="save()" [readOnly]="loading() || saving()" placeholder="Ajouter un commentaire"></textarea>
      </label>
      @if (error()) {
        <p class="error" role="alert">{{ error() }}</p>
      }
      @if (loadFailed()) {
        <button type="button" class="secondary" (click)="retry.update(increment)">Réessayer</button>
      }
      @if (loading() || saving() || notice()) {
        <span class="meta" role="status">{{ loading() ? 'Chargement…' : saving() ? 'Enregistrement…' : notice() }}</span>
      }
    </div>
  `,
  styleUrl: './customer-comment.component.css',
})
export class CustomerCommentComponent {
  private readonly api = inject(CustomersApiService);
  private readonly destroyRef = inject(DestroyRef);
  private lastSavedComment: string | null = null;
  private contextVersion = 0;
  readonly customerId = input.required<string>();
  readonly form = inject(FormBuilder).nonNullable.group({ comment: ['', Validators.maxLength(2000)] });
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly notice = signal('');
  readonly loadFailed = signal(false);
  readonly retry = signal(0);
  readonly increment = (value: number) => value + 1;

  constructor() {
    effect((onCleanup) => {
      const id = this.customerId();
      this.retry();
      this.contextVersion++;
      this.lastSavedComment = null;
      this.loading.set(true);
      this.saving.set(false);
      this.loadFailed.set(false);
      this.error.set('');
      this.notice.set('');
      this.form.reset({ comment: '' });
      const subscription = this.api.getComment(id).subscribe({
        next: (value) => {
          this.lastSavedComment = value.comment?.trim() || null;
          this.form.reset({ comment: value.comment ?? '' });
          this.loading.set(false);
        },
        error: () => {
          this.loading.set(false);
          this.loadFailed.set(true);
          this.error.set('Chargement du commentaire impossible.');
        },
      });
      onCleanup(() => {
        subscription.unsubscribe();
      });
    });
    this.form.controls.comment.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => this.notice.set(''));
  }

  save(): void {
    if (this.form.invalid || this.loading() || this.saving() || this.loadFailed()) return;
    const comment = this.form.controls.comment.value.trim() || null;
    if (comment === this.lastSavedComment) return;
    const version = this.contextVersion;
    this.saving.set(true);
    this.error.set('');
    this.notice.set('');
    // Let this single HTTP save finish when the session detail is closed after blur.
    this.api.updateComment(this.customerId(), comment).subscribe({
      next: (value) => {
        if (this.destroyRef.destroyed || version !== this.contextVersion) return;
        this.lastSavedComment = value.comment?.trim() || null;
        this.form.reset({ comment: value.comment ?? '' });
        this.saving.set(false);
        this.notice.set('Commentaire enregistré.');
      },
      error: () => {
        if (this.destroyRef.destroyed || version !== this.contextVersion) return;
        this.saving.set(false);
        this.error.set('Enregistrement du commentaire impossible.');
      },
    });
  }
}
