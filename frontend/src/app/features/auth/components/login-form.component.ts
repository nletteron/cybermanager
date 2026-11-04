import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Output, inject, input } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { CmButtonComponent, CmInputComponent } from '../../../shared/components/design-system';
import type { LoginPayload } from '../models/auth.models';

@Component({
  selector: 'app-login-form',
  imports: [CommonModule, ReactiveFormsModule, CmButtonComponent, CmInputComponent],
  template: `
    <form class="form" [formGroup]="form" (ngSubmit)="submit()">
      <cm-input label="Email" type="email" formControlName="email" autocomplete="username" />
      <cm-input label="Mot de passe" type="password" formControlName="password" placeholder="admin123" />
      <cm-button type="submit" [disabled]="loading() || form.invalid">
        {{ loading() ? 'Connexion...' : 'Se connecter' }}
      </cm-button>
    </form>
  `,
  styleUrl: './login-form.component.css',
})
export class LoginFormComponent {
  private readonly fb = inject(FormBuilder);

  readonly loading = input(false);

  @Output() readonly loginRequested = new EventEmitter<LoginPayload>();

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['admin123', [Validators.required]],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loginRequested.emit(this.form.getRawValue());
  }
}
