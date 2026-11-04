import type { CustomerPurchaseLine } from '../../customers/models/customer.models';

export interface SessionDetailEntry {
  id: string;
  kind: 'purchase' | 'debt';
  saleId: string | null;
  debtId: string | null;
  label: string;
  occurredAt: string;
  amount: number;
  openDebt: boolean;
  saleType: string | null;
  lines: readonly CustomerPurchaseLine[];
}

export interface SessionDetailRow extends SessionDetailEntry {
  entries: readonly SessionDetailEntry[];
}
