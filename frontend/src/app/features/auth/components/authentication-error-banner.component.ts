import { CommonModule } from '@angular/common';
import { Component, input } from '@angular/core';

@Component({
  selector: 'app-authentication-error-banner',
  imports: [CommonModule],
  template: `<p *ngIf="message()" class="banner">{{ message() }}</p>`,
  styleUrl: './authentication-error-banner.component.css',
})
export class AuthenticationErrorBannerComponent {
  readonly message = input<string>('');
}
