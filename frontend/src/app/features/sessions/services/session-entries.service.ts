import { Injectable } from '@angular/core';

import type { CustomerDetails, CustomerPurchase, CustomerPurchaseLine } from '../../customers/models/customer.models';
import type { SessionDetailEntry, SessionDetailRow } from '../models/session-detail-entry.models';
import type { CafeSession } from '../models/session.models';

@Injectable({ providedIn: 'root' })
export class SessionEntriesService {
  entries(session: CafeSession, details: CustomerDetails): SessionDetailEntry[] {
    const openDebts = details.debts.filter((debt) => debt.status === 'OPEN');
    const debtsById = new Map(openDebts.map((debt) => [debt.debtId, debt]));
    const purchasesByDebtId = new Map(details.purchases
      .filter((purchase) => !!purchase.debtId)
      .map((purchase) => [purchase.debtId!, purchase]));
    const displayedDebtIds = new Set<string>();
    const entries: SessionDetailEntry[] = details.purchases
      .filter((purchase) => this.belongsToSession(purchase, session))
      .map((purchase) => {
        const debt = purchase.debtId ? debtsById.get(purchase.debtId) : undefined;
        if (debt) displayedDebtIds.add(debt.debtId);
        return {
          id: 'sale-' + purchase.saleId,
          kind: debt ? 'debt' : 'purchase',
          saleId: purchase.saleId,
          debtId: debt?.debtId ?? null,
          label: purchase.label,
          occurredAt: purchase.soldAt,
          amount: debt?.amount ?? purchase.totalAmount,
          openDebt: !!debt,
          saleType: purchase.type,
          lines: purchase.lines ?? [],
        };
      });
    for (const debt of openDebts) {
      if (!displayedDebtIds.has(debt.debtId)) {
        const purchase = purchasesByDebtId.get(debt.debtId);
        entries.push({
          id: 'debt-' + debt.debtId,
          kind: 'debt',
          saleId: purchase?.saleId ?? null,
          debtId: debt.debtId,
          label: purchase?.label ?? debt.label,
          occurredAt: debt.createdAt,
          amount: debt.amount,
          openDebt: true,
          saleType: purchase?.type ?? null,
          lines: purchase?.lines ?? [],
        });
      }
    }
    return entries.sort((left, right) => new Date(right.occurredAt).getTime() - new Date(left.occurredAt).getTime()
      || left.id.localeCompare(right.id));
  }

  rows(session: CafeSession, details: CustomerDetails, grouped: boolean): SessionDetailRow[] {
    const entries = this.entries(session, details);
    if (!grouped) return entries.map((entry) => ({ ...entry, entries: [entry] }));
    const groups = new Map<string, SessionDetailRow>();
    for (const entry of entries) {
      const key = this.groupKey(entry);
      const previous = groups.get(key);
      if (!previous) {
        groups.set(key, { ...entry, lines: this.sumLines(entry.lines), entries: [entry] });
      } else {
        const members = [...previous.entries, entry];
        groups.set(key, {
          ...previous,
          id: 'group-' + members.map((member) => member.id).sort().join(':'),
          saleId: null,
          debtId: null,
          amount: this.addAmounts(previous.amount, entry.amount),
          lines: this.sumLines([...previous.lines, ...entry.lines]),
          entries: members,
        });
      }
    }
    return [...groups.values()];
  }

  private groupKey(entry: SessionDetailEntry): string {
    const products = [...new Set(entry.lines.map((line) => JSON.stringify([line.label, line.unitPrice])))].sort();
    return JSON.stringify([entry.kind, entry.saleType,
      products.length ? products : entry.kind === 'debt' ? entry.label : entry.id]);
  }

  private sumLines(lines: readonly CustomerPurchaseLine[]): CustomerPurchaseLine[] {
    const totals = new Map<string, CustomerPurchaseLine>();
    for (const line of lines) {
      const key = JSON.stringify([line.label, line.unitPrice]);
      const previous = totals.get(key);
      totals.set(key, previous ? {
        ...previous,
        quantity: previous.quantity + line.quantity,
        totalPrice: this.addAmounts(previous.totalPrice, line.totalPrice),
      } : { ...line });
    }
    return [...totals.values()];
  }

  private addAmounts(left: number, right: number): number {
    return (Math.round(left * 100) + Math.round(right * 100)) / 100;
  }

  private belongsToSession(purchase: CustomerPurchase, session: CafeSession): boolean {
    if (purchase.sessionId) return purchase.sessionId === session.sessionId;
    const occurredAt = new Date(purchase.soldAt).getTime();
    return occurredAt >= new Date(session.startedAt).getTime()
      && (!session.endedAt || occurredAt <= new Date(session.endedAt).getTime());
  }
}
