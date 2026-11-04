import { CommonModule } from '@angular/common';
import { Component, EventEmitter, OnChanges, Output, inject, input } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { CmButtonComponent, CmInputComponent } from '../../../shared/components/design-system';
import type { UserResponse } from '../models/user.models';
import { RoleSelectorComponent } from './role-selector.component';

@Component({
  selector: 'app-user-form',
  imports: [CommonModule, ReactiveFormsModule, RoleSelectorComponent, CmButtonComponent, CmInputComponent],
  template: `
    <form class="form" [formGroup]="form" (ngSubmit)="submit()">
      <cm-input label="Email" type="email" formControlName="email" />
      <cm-input label="Prenom" type="text" formControlName="firstName" />
      <cm-input label="Nom" type="text" formControlName="lastName" />
      <cm-input label="Mot de passe" type="password" formControlName="password" [placeholder]="mode() === 'create' ? 'obligatoire' : 'laisser vide pour conserver'" />
      <div>
        <span>Roles</span>
        <app-role-selector [selected]="roles" (selectedChange)="setRoles($event)" />
      </div>

      <div class="actions">
        <cm-button type="submit" [disabled]="form.invalid || roles.length === 0">{{ mode() === 'create' ? 'Creer' : 'Enregistrer' }}</cm-button>
        <cm-button *ngIf="mode() === 'edit' && initialUser() as user" variant="secondary" (click)="statusToggled.emit()">
          {{ user.status === 'ACTIVE' ? 'Desactiver' : 'Activer' }}
        </cm-button>
        <cm-button *ngIf="mode() === 'edit'" variant="danger" (click)="deleted.emit()">Supprimer</cm-button>
      </div>
    </form>
  `,
  styleUrl: './user-form.component.css',
})
export class UserFormComponent implements OnChanges {
  private readonly fb = inject(FormBuilder);

  readonly mode = input<'create' | 'edit'>('create');
  readonly initialUser = input<UserResponse | null>(null);
  roles: string[] = ['EMPLOYEE'];

  @Output() readonly saved = new EventEmitter<{
    email: string;
    firstName: string;
    lastName: string;
    password?: string;
    roles: string[];
  }>();
  @Output() readonly statusToggled = new EventEmitter<void>();
  @Output() readonly deleted = new EventEmitter<void>();

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    firstName: ['', [Validators.required]],
    lastName: ['', [Validators.required]],
    password: [''],
  });

  ngOnChanges(): void {
    const user = this.initialUser();
    if (!user) {
      this.roles = ['EMPLOYEE'];
      this.form.reset({
        email: '',
        firstName: '',
        lastName: '',
        password: '',
      });
      return;
    }

    this.roles = [...user.roles];
    this.form.patchValue({
      email: user.email,
      firstName: user.firstName,
      lastName: user.lastName,
      password: '',
    });
  }

  setRoles(roles: string[]): void {
    this.roles = roles;
  }

  submit(): void {
    if (this.form.invalid || this.roles.length === 0) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    this.saved.emit({ ...raw, roles: this.roles });
  }
}
