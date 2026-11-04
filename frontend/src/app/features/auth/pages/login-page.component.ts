import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { SessionService } from '../../../core/services/session.service';
import { AuthenticationErrorBannerComponent } from '../components/authentication-error-banner.component';
import { LoginFormComponent } from '../components/login-form.component';
import { AuthApiService } from '../services/auth-api.service';

@Component({
  selector: 'app-login-page',
  imports: [CommonModule, LoginFormComponent, AuthenticationErrorBannerComponent],
  template: `
    <section class="page">
      <div class="card">
        <p class="eyebrow">Cyber cafe operations</p>
        <h1>Connexion sécurisée</h1>
        <p class="lead">Authentification des employés et administrateurs CyberManager.</p>
        <app-authentication-error-banner [message]="error()" />
        <app-login-form [loading]="loading()" (loginRequested)="login($event)" />
      </div>
    </section>
  `,
  styleUrl: './login-page.component.css',
})
export class LoginPageComponent {
  private readonly authApi = inject(AuthApiService);
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly error = signal('');

  login(payload: { email: string; password: string }): void {
    this.loading.set(true);
    this.error.set('');
    this.authApi.login(payload).subscribe({
      next: (response) => {
        this.session.setSession(response.token, response);
        void this.router.navigate(['/dashboard']);
      },
      error: (error: HttpErrorResponse) => {
        if (error.error?.message) {
          this.error.set(error.error.message);
        } else if (error.status === 404) {
          this.error.set("Authentification impossible. L'URL de l'API d'authentification semble incorrecte.");
        } else if (error.status === 0) {
          this.error.set("Authentification impossible. L'API d'authentification est inaccessible ou mal configuree.");
        } else {
          this.error.set('Authentification impossible');
        }
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }
}
