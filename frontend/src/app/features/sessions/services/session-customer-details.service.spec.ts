import { TestBed } from '@angular/core/testing';
import { firstValueFrom, of, Subject, throwError } from 'rxjs';

import type { CustomerDetails, CustomerPurchase } from '../../customers/models/customer.models';
import { CustomersApiService } from '../../customers/services/customers-api.service';
import type { Sale } from '../../sales/models/sales.models';
import { SalesApiService } from '../../sales/services/sales-api.service';
import type { CafeSession } from '../models/session.models';
import { SessionCustomerDetailsService } from './session-customer-details.service';
import { SessionEntriesService } from './session-entries.service';

describe('SessionCustomerDetailsService', () => {
  const purchase = (saleId: string, quantity = 2): CustomerPurchase => ({
    saleId, sessionId: 'session', type: 'PRODUCTS', label: 'Cafe', debtLabel: 'Cafe',
    soldAt: '2026-11-02T10:00:00Z', totalAmount: quantity * 1.5, openDebt: false,
  });
  const sale = (saleId: string, quantity = 2): Sale => ({
    saleId, customerId: 'customer', type: 'PRODUCTS', soldAt: '2026-11-02T10:00:00Z', totalAmount: quantity * 1.5,
    lines: [{ label: 'Cafe', quantity, unitPrice: 1.5, totalPrice: quantity * 1.5 }],
  });
  const details = (purchases: CustomerPurchase[]): CustomerDetails => ({
    customerId: 'customer', name: 'Test', type: 'WALK_IN', status: 'ACTIVE', remainingMinutes: 0,
    openDebtAmount: 0, currentSubscriptionLabel: null, purchases, debts: [],
  });
  const customerGet = vi.fn();
  const saleGet = vi.fn();

  beforeEach(() => {
    customerGet.mockReset();
    saleGet.mockReset().mockImplementation((id: string) => of(sale(id)));
    TestBed.configureTestingModule({ providers: [
      { provide: CustomersApiService, useValue: { get: customerGet } },
      { provide: SalesApiService, useValue: { get: saleGet } },
    ] });
  });

  it('loads recorded quantities from sale details when the running customer API omits lines', async () => {
    const original = details([purchase('sale-1')]);
    customerGet.mockReturnValue(of(original));

    const result = await firstValueFrom(TestBed.inject(SessionCustomerDetailsService).get('customer'));

    expect(saleGet).toHaveBeenCalledExactlyOnceWith('sale-1');
    expect(result.purchases[0].lines?.[0]).toEqual({ label: 'Cafe', quantity: 2, unitPrice: 1.5, totalPrice: 3 });
    expect(original.purchases[0].lines).toBeUndefined();
  });

  it('keeps complete API data without additional requests and only fetches missing lines', async () => {
    const complete = { ...purchase('complete'), lines: sale('complete').lines };
    const original = details([complete]);
    customerGet.mockReturnValue(of(original));
    const reader = TestBed.inject(SessionCustomerDetailsService);
    expect(await firstValueFrom(reader.get('customer'))).toBe(original);
    expect(saleGet).not.toHaveBeenCalled();

    customerGet.mockReturnValue(of(details([complete, purchase('missing')])));
    const result = await firstValueFrom(reader.get('customer'));
    expect(saleGet).toHaveBeenCalledExactlyOnceWith('missing');
    expect(result.purchases[0]).toBe(complete);
  });

  it('makes cafes bought with the older API groupable using their actual quantities and prices', async () => {
    customerGet.mockReturnValue(of(details([purchase('sale-1', 2), purchase('sale-2', 1)])));
    saleGet.mockImplementation((id: string) => of(sale(id, id === 'sale-1' ? 2 : 1)));
    const result = await firstValueFrom(TestBed.inject(SessionCustomerDetailsService).get('customer'));
    const session = { sessionId: 'session', startedAt: '2026-11-02T09:00:00Z', endedAt: null } as CafeSession;

    const rows = new SessionEntriesService().rows(session, result, true);

    expect(rows).toHaveLength(1);
    expect(rows[0].lines[0].quantity).toBe(3);
    expect(rows[0].amount).toBe(4.5);
    expect(rows[0].entries).toHaveLength(2);
    expect(result.purchases).toHaveLength(2);
  });

  it('propagates a failed or incomplete sale read instead of inventing a quantity', async () => {
    customerGet.mockReturnValue(of(details([purchase('sale-1')])));
    const reader = TestBed.inject(SessionCustomerDetailsService);
    saleGet.mockReturnValue(throwError(() => new Error('API unavailable')));
    await expect(firstValueFrom(reader.get('customer'))).rejects.toThrow('API unavailable');

    saleGet.mockReturnValue(of({ ...sale('sale-1'), lines: [] }));
    await expect(firstValueFrom(reader.get('customer'))).rejects.toThrow('absentes');
    saleGet.mockReturnValue(of(sale('another-sale')));
    await expect(firstValueFrom(reader.get('customer'))).rejects.toThrow('absentes');
  });

  it('limits simultaneous detail reads while preserving all purchases', async () => {
    const ids = ['1', '2', '3', '4', '5', '6'];
    const pending = new Map(ids.map((id) => [id, new Subject<Sale>()]));
    customerGet.mockReturnValue(of(details(ids.map((id) => purchase(id)))));
    saleGet.mockImplementation((id: string) => pending.get(id));
    const result = firstValueFrom(TestBed.inject(SessionCustomerDetailsService).get('customer'));
    expect(saleGet).toHaveBeenCalledTimes(4);
    for (const id of ids) {
      pending.get(id)!.next(sale(id));
      pending.get(id)!.complete();
    }
    expect((await result).purchases).toHaveLength(6);
    expect(saleGet).toHaveBeenCalledTimes(6);
  });
});
