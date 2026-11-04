export interface Customer {
  customerId: string;
  name: string;
  type: 'WALK_IN' | 'SUBSCRIBER';
  status: 'ACTIVE' | 'INACTIVE';
  remainingMinutes: number;
  openDebtAmount: number;
}

export interface CustomerPurchaseLine {
  label: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
}

export interface CustomerPurchase {
  saleId: string;
  sessionId: string | null;
  type: string;
  label: string;
  debtLabel: string;
  soldAt: string;
  totalAmount: number;
  openDebt: boolean;
  debtId?: string | null;
  lines?: readonly CustomerPurchaseLine[];
}

export interface CustomerComment {
  customerId: string;
  comment: string | null;
}

export interface CustomerDetails extends Customer {
  currentSubscriptionLabel: string | null;
  purchases: CustomerPurchase[];
  debts: { debtId: string; label: string; amount: number; status: 'OPEN' | 'SETTLED'; createdAt: string; settledAt: string | null }[];
}
