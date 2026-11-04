import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { environment } from '../../../../environments/environment';
import type { CafeSession } from '../models/session.models';
import type { SessionDisplayPreference } from '../models/session-display-preference.models';

@Injectable({ providedIn: 'root' })
export class SessionsApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.usersApiUrl}/sessions`;

  day() {
    return this.http.get<CafeSession[]>(`${this.baseUrl}/day`);
  }

  current() {
    return this.http.get<{ sessions: CafeSession[] }>(`${this.baseUrl}/current`);
  }

  displayPreference() {
    return this.http.get<SessionDisplayPreference | null>(`${this.baseUrl}/display-preference`);
  }

  configureDisplayPreference(groupEntries: boolean) {
    return this.http.put<SessionDisplayPreference>(`${this.baseUrl}/display-preference`, { groupEntries });
  }

  start(payload: { customerId?: string; customerName?: string }) {
    return this.http.post<CafeSession>(`${this.baseUrl}/start`, payload);
  }

  stop(sessionId: string) {
    return this.http.post<CafeSession>(`${this.baseUrl}/${sessionId}/stop`, {});
  }

  pay(sessionId: string, payload: { amountPaid: number; subscriptionOfferIds: string[]; createSubscriptionDebt: boolean }) {
    return this.http.post<CafeSession>(`${this.baseUrl}/${sessionId}/pay`, payload);
  }

  payWithInvoice(sessionId: string, payload: { amountPaid: number; subscriptionOfferIds: string[]; createSubscriptionDebt: boolean }) {
    return this.http.post(`${this.baseUrl}/${sessionId}/pay-with-invoice`, payload, { observe: 'response', responseType: 'blob' });
  }

  restartDay() {
    return this.http.post<{ archivedSessions: number }>(`${this.baseUrl}/restart-day`, {});
  }

  pause(sessionId: string) {
    return this.http.post<CafeSession>(`${this.baseUrl}/${sessionId}/pause`, {});
  }

  resume(sessionId: string) {
    return this.http.post<CafeSession>(`${this.baseUrl}/${sessionId}/resume`, {});
  }

  deletePurchase(sessionId: string, saleId: string) {
    return this.http.delete<void>(`${this.baseUrl}/${sessionId}/purchases/${saleId}`);
  }

  deleteDebt(sessionId: string, debtId: string) {
    return this.http.delete<void>(`${this.baseUrl}/${sessionId}/debts/${debtId}`);
  }

  fileName(response: HttpResponse<Blob>): string {
    const header = response.headers.get('content-disposition') ?? '';
    const match = /filename="([^"]+)"/i.exec(header);
    return match?.[1] ?? 'facture.pdf';
  }
}
