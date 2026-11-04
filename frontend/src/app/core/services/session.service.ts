import { Injectable, computed, signal } from '@angular/core';

import type { SessionUser } from '../models/session-user.model';

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly tokenState = signal<string | null>(null);
  private readonly userState = signal<SessionUser | null>(null);

  readonly token = computed(() => this.tokenState());
  readonly user = computed(() => this.userState());
  readonly isAuthenticated = computed(() => !!this.tokenState());

  setSession(token: string, user: SessionUser): void {
    this.tokenState.set(token);
    this.userState.set(user);
  }

  clear(): void {
    this.tokenState.set(null);
    this.userState.set(null);
  }

  updateUser(user: SessionUser): void {
    this.userState.set(user);
  }
}
