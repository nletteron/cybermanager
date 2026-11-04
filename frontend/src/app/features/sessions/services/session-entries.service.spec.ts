import type { CustomerDetails, CustomerPurchase } from '../../customers/models/customer.models';
import type { CafeSession } from '../models/session.models';
import { SessionEntriesService } from './session-entries.service';

describe('SessionEntriesService', () => {
  const service = new SessionEntriesService();
  const session = { sessionId: 'session', startedAt: '2026-11-02T10:00:00Z', endedAt: null } as CafeSession;
  const purchase = (saleId: string, debtId: string | null = null, quantity = 1): CustomerPurchase => ({
    saleId, debtId, sessionId: 'session', type: 'PRODUCTS', label: 'Produit 1', debtLabel: '1 x Produit 1 du 02/11/2026 10:01',
    soldAt: '2026-11-02T10:01:00Z', totalAmount: quantity * 2, openDebt: !!debtId,
    lines: [{ label: 'Produit 1', quantity, unitPrice: 2, totalPrice: quantity * 2 }],
  });
  const debt = (debtId: string): CustomerDetails['debts'][number] => ({
    debtId, label: '1 x Produit 1 du 02/11/2026 10:01', amount: 2, status: 'OPEN',
    createdAt: '2026-11-02T10:01:00Z', settledAt: null,
  });
  const details = (purchases: CustomerPurchase[], debts: CustomerDetails['debts'] = []): CustomerDetails => ({
    customerId: 'customer', name: 'Alice', type: 'WALK_IN', status: 'ACTIVE', remainingMinutes: 0,
    openDebtAmount: debts.reduce((sum, row) => sum + row.amount, 0), currentSubscriptionLabel: null, purchases, debts,
  });

  it('keeps identical purchases as distinct rows with stable identifiers', () => {
    const rows = service.entries(session, details([purchase('sale-1'), purchase('sale-2')]));
    expect(rows).toHaveLength(2);
    expect(new Set(rows.map((row) => row.id)).size).toBe(2);
    expect(rows.map((row) => row.saleId)).toEqual(['sale-1', 'sale-2']);
    expect(rows.map((row) => row.amount)).toEqual([2, 2]);
  });

  it('links each repeated purchase to its own debt without matching labels or amounts', () => {
    const rows = service.entries(session, details(
      [purchase('sale-1', 'debt-1'), purchase('sale-2', 'debt-2')], [debt('debt-1'), debt('debt-2')],
    ));
    expect(rows).toHaveLength(2);
    expect(rows.map((row) => [row.saleId, row.debtId])).toEqual([['sale-1', 'debt-1'], ['sale-2', 'debt-2']]);
    expect(rows.every((row) => row.openDebt)).toBe(true);
  });

  it('only changes the purchase whose debt was reattached', () => {
    const rows = service.entries(session, details(
      [purchase('sale-1'), purchase('sale-2', 'debt-2')], [debt('debt-2')],
    ));
    expect(rows.map((row) => row.openDebt)).toEqual([false, true]);
    expect(rows[1].debtId).toBe('debt-2');
  });

  it('does not merge identical standalone debts', () => {
    const rows = service.entries(session, details([], [debt('debt-1'), debt('debt-2')]));
    expect(rows).toHaveLength(2);
    expect(rows.map((row) => row.debtId)).toEqual(['debt-1', 'debt-2']);
  });

  it('shows only the sessions purchases and carries forward open debts individually', () => {
    const rows = service.entries(session, details([
      purchase('current'),
      { ...purchase('earlier', 'previous-debt'), sessionId: 'previous-session' },
      { ...purchase('unrelated'), sessionId: 'another-session' },
    ], [debt('previous-debt')]));
    expect(rows.map((row) => row.saleId).sort()).toEqual(['current', 'earlier']);
  });

  it('exposes the persisted quantity of a product or subscription without replacing it with a row count', () => {
    const subscriber = { ...purchase('subscription', null, 4), type: 'SUBSCRIPTION' };
    const rows = service.entries(session, details([purchase('product', null, 3), subscriber]));
    expect(rows.find((row) => row.saleId === 'product')?.lines[0].quantity).toBe(3);
    expect(rows.find((row) => row.saleId === 'subscription')?.lines[0].quantity).toBe(4);
  });

  it('optionally stacks identical products using recorded quantities and totals without changing the original entries', () => {
    const customer = details([purchase('sale-1', null, 2), purchase('sale-2', null, 3)]);
    const original = structuredClone(customer);
    const rows = service.rows(session, customer, true);
    expect(rows).toHaveLength(1);
    expect(rows[0].lines[0]).toEqual({ label: 'Produit 1', quantity: 5, unitPrice: 2, totalPrice: 10 });
    expect(rows[0].amount).toBe(10);
    expect(rows[0].saleId).toBeNull();
    expect(rows[0].entries.map((entry) => entry.saleId)).toEqual(['sale-1', 'sale-2']);
    expect(customer).toEqual(original);
    expect(service.rows(session, customer, false)).toHaveLength(2);
  });

  it('stacks debts individually linked to sales and leaves the stored debt identifiers and amounts unchanged', () => {
    const firstDebt = { ...debt('debt-1'), amount: 4 };
    const secondDebt = { ...debt('debt-2'), amount: 6 };
    const customer = details([purchase('sale-1', 'debt-1', 2), purchase('sale-2', 'debt-2', 3)], [firstDebt, secondDebt]);
    const rows = service.rows(session, customer, true);
    expect(rows).toHaveLength(1);
    expect(rows[0].kind).toBe('debt');
    expect(rows[0].lines[0].quantity).toBe(5);
    expect(rows[0].amount).toBe(10);
    expect(rows[0].debtId).toBeNull();
    expect(rows[0].entries.map((entry) => entry.debtId)).toEqual(['debt-1', 'debt-2']);
    expect(customer.debts.map((entry) => entry.amount)).toEqual([4, 6]);
  });

  it('keeps purchases, debts, subscription types and different unit prices in separate groups', () => {
    const changedPrice = { ...purchase('different-price'), totalAmount: 3,
      lines: [{ label: 'Produit 1', quantity: 1, unitPrice: 3, totalPrice: 3 }] };
    const rows = service.rows(session, details([
      purchase('sale-1'), purchase('sale-2'), purchase('sale-3', 'debt-3'),
      { ...purchase('subscription'), type: 'SUBSCRIPTION' }, changedPrice,
    ], [debt('debt-3')]), true);
    expect(rows).toHaveLength(4);
    expect(rows.filter((row) => row.entries.length === 2)).toHaveLength(1);
  });

  it('adds monetary values in cents', () => {
    const customer = details([
      { ...purchase('sale-1'), totalAmount: .1, lines: [{ label: 'Produit 1', quantity: 1, unitPrice: .1, totalPrice: .1 }] },
      { ...purchase('sale-2'), totalAmount: .2, lines: [{ label: 'Produit 1', quantity: 2, unitPrice: .1, totalPrice: .2 }] },
    ]);
    const row = service.rows(session, customer, true)[0];
    expect(row.amount).toBe(.3);
    expect(row.lines[0].totalPrice).toBe(.3);
    expect(row.lines[0].quantity).toBe(3);
  });

  it('sums the quantities of each product in matching carts and keeps other cart compositions separate', () => {
    const first = { ...purchase('sale-1'), totalAmount: 5, lines: [
      { label: 'Produit 1', quantity: 1, unitPrice: 2, totalPrice: 2 },
      { label: 'Produit 2', quantity: 1, unitPrice: 3, totalPrice: 3 },
    ] };
    const second = { ...purchase('sale-2'), totalAmount: 7, lines: [
      { label: 'Produit 2', quantity: 1, unitPrice: 3, totalPrice: 3 },
      { label: 'Produit 1', quantity: 2, unitPrice: 2, totalPrice: 4 },
    ] };
    const rows = service.rows(session, details([first, second, purchase('sale-3')]), true);
    expect(rows).toHaveLength(2);
    const cart = rows.find((row) => row.entries.length === 2)!;
    expect(cart.amount).toBe(12);
    expect(cart.lines.find((line) => line.label === 'Produit 1')?.quantity).toBe(3);
    expect(cart.lines.find((line) => line.label === 'Produit 2')?.quantity).toBe(2);
  });

  it('uses the recorded debt balance rather than replacing it with the original sale total', () => {
    const row = service.entries(session, details([purchase('sale-1', 'debt-1')], [{ ...debt('debt-1'), amount: 1 }]))[0];
    expect(row.amount).toBe(1);
  });

  it('can stack identical standalone debts without inventing a product quantity', () => {
    const rows = service.rows(session, details([], [debt('debt-1'), debt('debt-2')]), true);
    expect(rows).toHaveLength(1);
    expect(rows[0].amount).toBe(4);
    expect(rows[0].lines).toEqual([]);
    expect(rows[0].entries).toHaveLength(2);
  });
});
