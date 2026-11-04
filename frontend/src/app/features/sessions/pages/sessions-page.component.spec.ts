import { TestBed } from '@angular/core/testing';
import { of, Subject, throwError } from 'rxjs';

import { AppLoggerService } from '../../../core/services/app-logger.service';
import { CustomersApiService } from '../../customers/services/customers-api.service';
import type { Customer, CustomerDetails } from '../../customers/models/customer.models';
import { ProductsApiService } from '../../products/services/products-api.service';
import { SalesApiService } from '../../sales/services/sales-api.service';
import { SubscriptionOffersApiService } from '../../subscriptions/services/subscription-offers-api.service';
import type { CafeSession } from '../models/session.models';
import type { SessionDisplayPreference } from '../models/session-display-preference.models';
import { SessionsApiService } from '../services/sessions-api.service';
import { SessionsPageComponent } from './sessions-page.component';

describe('SessionsPageComponent table controls', () => {
  const sessions: CafeSession[] = [
    { sessionId: '1', customerId: '1', customerName: 'Alice', startedAt: '2026-11-02T10:00:00Z', paused: false, endedAt: null, paid: false },
    { sessionId: '2', customerId: '2', customerName: 'Zoé', startedAt: '2026-11-02T12:00:00Z', paused: true, endedAt: null, paid: false },
    { sessionId: '3', customerId: '3', customerName: 'Bob', startedAt: '2026-11-02T11:00:00Z', paused: false, endedAt: '2026-11-02T12:00:00Z', paid: false },
  ].map((item) => ({
    ...item, customerType: 'WALK_IN', remainingMinutes: 0, displayRemainingMinutes: 0,
    consumedSeconds: 0, consumedMinutes: 0, calculatedPrice: 0, purchasesAmount: 0,
    openDebtAmount: 0, totalAmountDue: 0, totalPaidAmount: 0,
  }));

  const current = vi.fn(() => of({ sessions }));
  const start = vi.fn(() => of(sessions[0]));
  let persistedPreference: SessionDisplayPreference | null = null;
  const displayPreference = vi.fn(() => of<SessionDisplayPreference | null>(persistedPreference));
  const configureDisplayPreference = vi.fn((groupEntries: boolean) => {
    persistedPreference = { groupEntries };
    return of(persistedPreference);
  });
  const customerSearch = vi.fn<CustomersApiService['search']>(() => of<Customer[]>([]));
  const details = (id: string): CustomerDetails => ({ customerId: id, name: 'Alice', type: 'WALK_IN', status: 'ACTIVE', remainingMinutes: 0, openDebtAmount: 0, currentSubscriptionLabel: null, purchases: [], debts: [] });
  const customerGet = vi.fn((id: string) => of(details(id)));
  const deletePurchase = vi.fn(() => of(undefined));
  const deleteDebt = vi.fn(() => of(undefined));
  const createDebtFromSale = vi.fn(() => of(undefined));
  const reattachDebtToSession = vi.fn(() => of(undefined));
  const saleGet = vi.fn((saleId: string) => of({ saleId, customerId: '1', type: 'PRODUCTS', soldAt: '2026-11-02T10:02:00Z',
    totalAmount: 2, lines: [{ label: 'Produit 1', quantity: 1, unitPrice: 2, totalPrice: 2 }] }));
  const customers: Customer[] = [
    { customerId: '4', name: 'Alice Client', type: 'WALK_IN', status: 'ACTIVE', remainingMinutes: 0, openDebtAmount: 0 },
    { customerId: '5', name: 'Alice Abonnée', type: 'SUBSCRIBER', status: 'ACTIVE', remainingMinutes: 90, openDebtAmount: 0 },
  ];

  beforeEach(async () => {
    current.mockReturnValue(of({ sessions }));
    start.mockClear();
    persistedPreference = null;
    displayPreference.mockReset().mockImplementation(() => of(persistedPreference));
    configureDisplayPreference.mockReset().mockImplementation((groupEntries: boolean) => {
      persistedPreference = { groupEntries };
      return of(persistedPreference);
    });
    customerSearch.mockReset().mockReturnValue(of([]));
    customerGet.mockReset().mockImplementation((id: string) => of(details(id)));
    deletePurchase.mockClear();
    deleteDebt.mockClear();
    createDebtFromSale.mockClear();
    reattachDebtToSession.mockClear();
    saleGet.mockReset().mockImplementation((saleId: string) => of({ saleId, customerId: '1', type: 'PRODUCTS', soldAt: '2026-11-02T10:02:00Z',
      totalAmount: 2, lines: [{ label: 'Produit 1', quantity: 1, unitPrice: 2, totalPrice: 2 }] }));
    await TestBed.configureTestingModule({
      imports: [SessionsPageComponent],
      providers: [
        { provide: SessionsApiService, useValue: { current, start, deletePurchase, deleteDebt, displayPreference, configureDisplayPreference } },
        { provide: CustomersApiService, useValue: {
          search: customerSearch,
          get: customerGet,
          createDebtFromSale,
          reattachDebtToSession,
          getComment: (id: string) => of({ customerId: id, comment: null }),
        } },
        { provide: SubscriptionOffersApiService, useValue: { search: () => of([]) } },
        { provide: ProductsApiService, useValue: { search: () => of([]) } },
        { provide: SalesApiService, useValue: { get: saleGet, pricing: () => of({ tiers: [] }) } },
        { provide: AppLoggerService, useValue: { info: vi.fn(), debug: vi.fn(), warn: vi.fn() } },
      ],
    }).compileComponents();
  });

  const setup = () => {
    const fixture = TestBed.createComponent(SessionsPageComponent);
    fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;
    const names = () => Array.from(element.querySelectorAll('.client-link')).map((node) => node.textContent?.trim());
    const click = (selector: string) => {
      element.querySelector<HTMLElement>(selector)!.click();
      fixture.detectChanges();
    };
    const filter = () => {
      fixture.componentInstance.showCurrentFilters.set(true);
      fixture.detectChanges();
      click('app-session-state-filter summary');
    };
    return { fixture, element, names, click, filter };
  };

  it('sorts from column headings and exposes the direction to assistive technology', () => {
    const { element, names, click } = setup();
    expect(names()).toEqual(['Zoé', 'Bob', 'Alice']);
    expect(element.querySelector('th[aria-sort="descending"]')?.textContent).toContain('Début');
    click('th:nth-child(2) button');
    expect(names()).toEqual(['Alice', 'Bob', 'Zoé']);
    expect(element.querySelector('th:nth-child(2)')?.getAttribute('aria-sort')).toBe('ascending');
    click('th:nth-child(2) button');
    expect(names()).toEqual(['Zoé', 'Bob', 'Alice']);
    click('th:nth-child(4) button');
    expect(names()).toEqual(['Alice', 'Zoé', 'Bob']);
    click('th:nth-child(8) button');
    click('th:nth-child(8) button');
    expect(names()).toEqual(['Alice', 'Bob', 'Zoé']);
  });

  it('searches available states, applies a state and restores all states', () => {
    const { fixture, element, names, filter, click } = setup();
    filter();
    const search = element.querySelector<HTMLInputElement>('app-session-state-filter input')!;
    search.value = 'PÁU';
    search.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    const options = element.querySelectorAll('app-session-state-filter .state-options button');
    expect(Array.from(options).map((option) => option.textContent?.trim())).toEqual(['Tous les états', 'Pause']);
    (options[1] as HTMLButtonElement).click();
    fixture.detectChanges();
    expect(names()).toEqual(['Zoé']);
    expect(element.querySelector<HTMLDetailsElement>('details')?.open).toBe(false);
    click('app-session-state-filter summary');
    click('app-session-state-filter .state-options button');
    expect(names()).toHaveLength(3);
  });

  it('keeps the selection when searching states and supports empty searches and Escape', () => {
    const { fixture, element, filter } = setup();
    fixture.componentInstance.currentState.set('Pause');
    filter();
    const search = element.querySelector<HTMLInputElement>('app-session-state-filter input')!;
    search.value = 'inexistant';
    search.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    expect(element.querySelector('[role="status"]')?.textContent).toContain('Aucun état trouvé');
    expect(fixture.componentInstance.currentState()).toBe('Pause');
    search.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
    fixture.detectChanges();
    expect(element.querySelector<HTMLDetailsElement>('details')?.open).toBe(false);
    expect(document.activeElement).toBe(element.querySelector('app-session-state-filter summary'));
  });

  it('combines filters reactively and keeps them after reloading sessions', async () => {
    const { fixture, element, names } = setup();
    const page = fixture.componentInstance;
    page.currentFilters.controls.term.setValue('zo');
    page.currentFilters.controls.startedAfter.setValue('2026-11-02T11:30:00Z');
    page.currentState.set('Pause');
    page.sortCurrent('customerName');
    await vi.waitFor(() => expect(page.currentTerm()).toBe('zo'));
    fixture.detectChanges();
    expect(names()).toEqual(['Zoé']);
    current.mockReturnValue(of({ sessions: sessions.map((session) => ({ ...session, paused: false })) }));
    page.reload();
    fixture.detectChanges();
    expect(names()).toEqual([]);
    expect(element.textContent).toContain('Aucune session ne correspond aux filtres.');
    expect(page.currentState()).toBe('Pause');
    expect(page.currentSort()).toEqual({ column: 'customerName', direction: 'asc' });
  });

  it('keeps the expanded detail and row actions attached to the same session after sorting', () => {
    const { fixture, element, click } = setup();
    fixture.componentInstance.expandedSessionIds.set(new Set(['1']));
    fixture.detectChanges();
    click('th:nth-child(2) button');
    const aliceRow = element.querySelector('.detail-row')?.previousElementSibling;
    expect(aliceRow?.querySelector('.client-link')?.textContent?.trim()).toBe('Alice');
    const toggle = aliceRow!.querySelector<HTMLButtonElement>('.detail-toggle')!;
    expect(toggle.textContent).toBe('−');
    expect(toggle.getAttribute('aria-expanded')).toBe('true');
    toggle.click();
    fixture.detectChanges();
    expect(element.querySelector('.detail-row')).toBeNull();
    expect(toggle.textContent).toBe('+');
    expect(aliceRow!.lastElementChild?.contains(toggle)).toBe(true);
    expect(element.textContent).not.toContain('Convertir');
  });

  it('searches all clients and selects a subscriber without creating a new customer', async () => {
    customerSearch.mockReturnValue(of(customers));
    const { fixture, element } = setup();
    const page = fixture.componentInstance;
    page.clientForm.controls.customerName.setValue('ali');
    await vi.waitFor(() => expect(customerSearch).toHaveBeenCalledWith('ali'));
    fixture.detectChanges();
    expect(element.querySelectorAll('.customer-option')).toHaveLength(2);
    expect(element.textContent).toContain('Alice Abonnée');
    expect(element.textContent).not.toContain('Abonne existant');
    expect(element.textContent).not.toContain('Nouvel abonne');
    element.querySelectorAll<HTMLButtonElement>('.customer-option')[1].click();
    fixture.detectChanges();
    expect(page.clientForm.controls.customerName.value).toBe('Alice Abonnée');
    page.startClientSession();
    expect(start).toHaveBeenCalledWith({ customerId: '5' });
    expect(page.selectedCustomer()).toBeNull();
  });

  it('shows filters immediately and keeps the client search in the left panel', () => {
    const { fixture, element } = setup();
    expect(fixture.componentInstance.showCurrentFilters()).toBe(true);
    expect(element.querySelector('.filters-grid')?.classList.contains('is-collapsed')).toBe(false);
    expect(element.querySelector('.grid')?.firstElementChild?.querySelector('.client-search-form')).not.toBeNull();
    expect(element.querySelector('.grid')?.lastElementChild?.querySelector('.current-section')).not.toBeNull();
  });

  it('toggles details by clicking session cells and keeps other details open', () => {
    const { fixture, element } = setup();
    const alice = element.querySelector<HTMLElement>('.session-row[aria-controls="session-detail-1"]')!;
    const zoe = element.querySelector<HTMLElement>('.session-row[aria-controls="session-detail-2"]')!;
    (alice.children[2] as HTMLElement).click();
    (zoe.children[3] as HTMLElement).click();
    fixture.detectChanges();
    expect(element.querySelectorAll('.detail-row')).toHaveLength(2);
    expect(alice.getAttribute('aria-expanded')).toBe('true');
    alice.click();
    fixture.detectChanges();
    expect(element.querySelectorAll('.detail-row')).toHaveLength(1);
    expect(fixture.componentInstance.expandedSessionIds().has('2')).toBe(true);
    zoe.querySelector<HTMLButtonElement>('.detail-toggle')!.click();
    fixture.detectChanges();
    expect(element.querySelectorAll('.detail-row')).toHaveLength(0);
  });

  it('keeps customer, pause, stop and payment buttons independent of the row toggle', () => {
    const { fixture, element } = setup();
    const page = fixture.componentInstance;
    const openCustomer = vi.spyOn(page, 'openCustomerModal').mockImplementation(() => {});
    const pause = vi.spyOn(page, 'togglePause').mockImplementation(() => {});
    const stop = vi.spyOn(page, 'stopSession').mockImplementation(() => {});
    const pay = vi.spyOn(page, 'openPaymentDialog').mockImplementation(() => {});
    const alice = element.querySelector<HTMLElement>('.session-row[aria-controls="session-detail-1"]')!;
    const bob = element.querySelector<HTMLElement>('.session-row[aria-controls="session-detail-3"]')!;
    alice.querySelector<HTMLButtonElement>('.client-link')!.click();
    const aliceActions = alice.querySelectorAll<HTMLButtonElement>('.action-cell button');
    aliceActions[0].click();
    aliceActions[1].click();
    Array.from(bob.querySelectorAll<HTMLButtonElement>('.action-cell button')).find((button) => button.textContent?.trim() === 'Payer')!.click();
    fixture.detectChanges();
    expect(openCustomer).toHaveBeenCalledExactlyOnceWith('1');
    expect(pause).toHaveBeenCalledExactlyOnceWith(sessions[0]);
    expect(stop).toHaveBeenCalledExactlyOnceWith(sessions[0]);
    expect(pay).toHaveBeenCalledExactlyOnceWith(sessions[2]);
    expect(page.expandedSessionIds().size).toBe(0);
  });

  it('supports Enter and Space on session rows without consuming nested button keys', () => {
    const { fixture, element } = setup();
    const row = element.querySelector<HTMLElement>('.session-row[aria-controls="session-detail-1"]')!;
    const enter = new KeyboardEvent('keydown', { key: 'Enter', bubbles: true, cancelable: true });
    row.dispatchEvent(enter);
    fixture.detectChanges();
    expect(enter.defaultPrevented).toBe(true);
    expect(row.getAttribute('aria-expanded')).toBe('true');
    const nestedEnter = new KeyboardEvent('keydown', { key: 'Enter', bubbles: true, cancelable: true });
    row.querySelector('.detail-toggle')!.dispatchEvent(nestedEnter);
    fixture.detectChanges();
    expect(nestedEnter.defaultPrevented).toBe(false);
    expect(row.getAttribute('aria-expanded')).toBe('true');
    row.dispatchEvent(new KeyboardEvent('keydown', { key: ' ', bubbles: true, cancelable: true }));
    fixture.detectChanges();
    expect(row.getAttribute('aria-expanded')).toBe('false');
  });

  it('exposes icon sales on the session row without toggling details and keeps the comment compact', () => {
    const { fixture, element } = setup();
    const page = fixture.componentInstance;
    const product = vi.spyOn(page, 'openProductSaleModal').mockImplementation(() => {});
    const subscription = vi.spyOn(page, 'openSubscriptionSaleModal').mockImplementation(() => {});
    const row = element.querySelector<HTMLElement>('.session-row[aria-controls="session-detail-1"]')!;
    const actions = row.querySelectorAll<HTMLButtonElement>('.session-sale-actions button');
    expect(Array.from(actions).map((button) => button.getAttribute('aria-label'))).toEqual(['Vendre un produit', 'Vendre un abonnement']);
    expect(Array.from(actions).map((button) => button.title)).toEqual(['Vendre un produit', 'Vendre un abonnement']);
    expect(row.querySelectorAll('.session-sale-actions svg')).toHaveLength(2);
    actions[0].querySelector('path')!.dispatchEvent(new MouseEvent('click', { bubbles: true }));
    actions[1].click();
    expect(product).toHaveBeenCalledExactlyOnceWith(sessions[0]);
    expect(subscription).toHaveBeenCalledExactlyOnceWith(sessions[0]);
    expect(page.expandedSessionIds().size).toBe(0);
    row.click();
    fixture.detectChanges();
    const detail = element.querySelector<HTMLElement>('#session-detail-1')!;
    expect(detail.textContent).not.toContain('Resume abonnement');
    expect(detail.textContent).not.toContain('Cout connexion');
    expect(detail.querySelector('.session-sale-actions')).toBeNull();
    expect(detail.querySelector<HTMLTextAreaElement>('app-customer-comment textarea')?.rows).toBe(1);
    expect(page.expandedSessionIds().has('1')).toBe(true);
  });

  it('opens several session details independently and preserves them after sorting and refresh', () => {
    const { fixture, element, click } = setup();
    const page = fixture.componentInstance;
    page.toggleDetails(sessions[0]);
    page.toggleDetails(sessions[1]);
    fixture.detectChanges();
    expect(element.querySelectorAll('.detail-row')).toHaveLength(2);
    click('th:nth-child(2) button');
    page.reload();
    fixture.detectChanges();
    expect(element.querySelectorAll('.detail-row')).toHaveLength(2);
    page.toggleDetails(sessions[0]);
    fixture.detectChanges();
    expect(element.querySelectorAll('.detail-row')).toHaveLength(1);
    expect(page.expandedSessionIds().has('2')).toBe(true);
  });

  it('restores the database grouping choice after leaving the page and applies it to every session', () => {
    const first = setup();
    first.fixture.componentInstance.toggleEntryGrouping(sessions[0]);
    expect(configureDisplayPreference).toHaveBeenCalledExactlyOnceWith(true);
    expect(first.fixture.componentInstance.isEntryGrouping(sessions[1])).toBe(true);
    first.fixture.destroy();

    const reopened = setup();
    const page = reopened.fixture.componentInstance;
    expect(displayPreference).toHaveBeenCalledTimes(2);
    expect(page.isEntryGrouping(sessions[0])).toBe(true);
    expect(page.isEntryGrouping(sessions[1])).toBe(true);
    page.toggleDetails(sessions[0]);
    page.showSeparateEntries(sessions[0]);
    expect(page.isEntryGrouping(sessions[0])).toBe(false);
    expect(page.isEntryGrouping(sessions[1])).toBe(true);
    page.toggleDetails(sessions[0]);
    page.toggleDetails(sessions[0]);
    expect(page.isEntryGrouping(sessions[0])).toBe(true);
    expect(configureDisplayPreference).toHaveBeenCalledTimes(1);
  });

  it('persists an explicit return to separate rows and reloads the choice for a new page', () => {
    persistedPreference = { groupEntries: true };
    const first = setup();
    first.fixture.componentInstance.toggleEntryGrouping(sessions[0]);
    expect(configureDisplayPreference).toHaveBeenCalledExactlyOnceWith(false);
    first.fixture.destroy();
    const reopened = setup();
    expect(reopened.fixture.componentInstance.isEntryGrouping(sessions[0])).toBe(false);
  });

  it('keeps the previous display choice and shows an error when persistence fails', () => {
    const { fixture } = setup();
    const page = fixture.componentInstance;
    configureDisplayPreference.mockReturnValueOnce(throwError(() => new Error('Persistence failed')));
    page.toggleEntryGrouping(sessions[0]);
    expect(page.isEntryGrouping(sessions[0])).toBe(false);
    expect(page.savingGroupingPreference()).toBe(false);
    expect(page.error()).toContain('Enregistrement');
  });

  it('waits for the persisted choice and prevents overlapping preference writes', () => {
    const loading = new Subject<SessionDisplayPreference | null>();
    displayPreference.mockReturnValueOnce(loading);
    const { fixture } = setup();
    const page = fixture.componentInstance;
    page.toggleEntryGrouping(sessions[0]);
    expect(configureDisplayPreference).not.toHaveBeenCalled();
    loading.next(null);
    loading.complete();
    const saving = new Subject<SessionDisplayPreference>();
    configureDisplayPreference.mockReturnValueOnce(saving);
    page.toggleEntryGrouping(sessions[0]);
    expect(page.savingGroupingPreference()).toBe(true);
    page.toggleEntryGrouping(sessions[1]);
    expect(configureDisplayPreference).toHaveBeenCalledTimes(1);
    saving.next({ groupEntries: true });
    saving.complete();
    expect(page.isEntryGrouping(sessions[0])).toBe(true);
    expect(page.isEntryGrouping(sessions[1])).toBe(true);
  });

  it('removes only the selected repeated product purchase and leaves the other row', () => {
    const { fixture, element } = setup();
    const purchases = ['sale-1', 'sale-2'].map((saleId) => ({ saleId, sessionId: '1', type: 'PRODUCTS', label: 'Produit 1', debtLabel: 'Produit 1', soldAt: '2026-11-02T10:02:00Z', totalAmount: 2, openDebt: false }));
    const customer = { ...details('1'), purchases };
    fixture.componentInstance.sessionDetails.set({ '1': customer });
    fixture.componentInstance.expandedSessionIds.set(new Set(['1']));
    fixture.detectChanges();
    expect(element.querySelectorAll('.session-entry .icon-remove')).toHaveLength(2);
    customerGet.mockReturnValue(of({ ...customer, purchases: [purchases[1]] }));

    element.querySelector<HTMLButtonElement>('.session-entry .icon-remove')!.click();
    fixture.detectChanges();

    expect(deletePurchase).toHaveBeenCalledExactlyOnceWith('1', 'sale-1');
    expect(deleteDebt).not.toHaveBeenCalled();
    expect(element.querySelectorAll('.session-entry')).toHaveLength(1);
    expect(fixture.componentInstance.sessionEntries(sessions[0], fixture.componentInstance.sessionDetails()['1'])[0].saleId).toBe('sale-2');
  });

  it('stacks purchases and debts without changing business rows and saves only the display preference', () => {
    const { fixture, element } = setup();
    const page = fixture.componentInstance;
    const purchases = [
      { saleId: 'sale-1', quantity: 2, debtId: null }, { saleId: 'sale-2', quantity: 3, debtId: null },
      { saleId: 'sale-3', quantity: 1, debtId: 'debt-3' }, { saleId: 'sale-4', quantity: 2, debtId: 'debt-4' },
    ].map((row) => ({ saleId: row.saleId, debtId: row.debtId, sessionId: '1', type: 'PRODUCTS', label: 'Produit 1',
      debtLabel: 'Produit 1', soldAt: '2026-11-02T10:02:00Z', totalAmount: row.quantity * 2, openDebt: !!row.debtId,
      lines: [{ label: 'Produit 1', quantity: row.quantity, unitPrice: 2, totalPrice: row.quantity * 2 }] }));
    const debts: CustomerDetails['debts'] = [
      { debtId: 'debt-3', label: 'Produit 1', amount: 2, status: 'OPEN', createdAt: '2026-11-02T10:02:00Z', settledAt: null },
      { debtId: 'debt-4', label: 'Produit 1', amount: 4, status: 'OPEN', createdAt: '2026-11-02T10:02:00Z', settledAt: null },
    ];
    const customer = { ...details('1'), purchases, debts };
    page.sessionDetails.set({ '1': customer });
    page.expandedSessionIds.set(new Set(['1']));
    fixture.detectChanges();
    const grouping = element.querySelector<HTMLButtonElement>('.entry-group-toggle')!;
    expect(grouping.getAttribute('aria-pressed')).toBe('false');
    expect(element.querySelectorAll('.session-entry')).toHaveLength(4);
    expect(element.querySelector('.session-entry strong')?.textContent).toContain('2 x Produit 1');
    const originalReads = customerGet.mock.calls.length;

    grouping.click();
    fixture.detectChanges();

    expect(grouping.getAttribute('aria-pressed')).toBe('true');
    expect(element.querySelectorAll('.session-entry')).toHaveLength(2);
    expect(element.querySelector('.session-entry:not(.debt-entry)')?.textContent).toContain('5 x Produit 1');
    expect(element.querySelector('.debt-entry')?.textContent).toContain('3 x Produit 1');
    expect(element.querySelector('.session-entry:not(.debt-entry) .entry-amount')?.textContent).toContain('10.00');
    expect(element.querySelector('.debt-entry .entry-amount')?.textContent).toContain('6.00');
    expect(element.querySelectorAll('.session-entry .icon-remove')).toHaveLength(0);
    expect(deletePurchase).not.toHaveBeenCalled();
    expect(deleteDebt).not.toHaveBeenCalled();
    expect(createDebtFromSale).not.toHaveBeenCalled();
    expect(reattachDebtToSession).not.toHaveBeenCalled();
    expect(customerGet.mock.calls.length).toBe(originalReads);
    expect(configureDisplayPreference).toHaveBeenCalledExactlyOnceWith(true);
    expect(page.sessionDetails()['1'].purchases).toEqual(purchases);
    expect(page.sessionDetails()['1'].debts).toEqual(debts);

    element.querySelector<HTMLButtonElement>('.session-entry .detail-toggle')!.click();
    fixture.detectChanges();
    expect(element.querySelectorAll('.session-entry')).toHaveLength(4);
    expect(element.querySelectorAll('.session-entry .icon-remove')).toHaveLength(4);
    expect(grouping.getAttribute('aria-pressed')).toBe('false');
    expect(page.groupingPreference()?.groupEntries).toBe(true);
    expect(configureDisplayPreference).toHaveBeenCalledTimes(1);
  });

  it('loads missing sale lines from the running API and renders two coffees before grouping matching purchases', () => {
    const purchases = ['coffee-1', 'coffee-2'].map((saleId, index) => ({
      saleId, sessionId: '1', type: 'PRODUCTS', label: 'Café', debtLabel: 'Café',
      soldAt: '2026-11-02T10:02:00Z', totalAmount: index === 0 ? 3 : 1.5, openDebt: false,
    }));
    customerGet.mockReturnValue(of({ ...details('1'), purchases }));
    saleGet.mockImplementation((saleId: string) => {
      const quantity = saleId === 'coffee-1' ? 2 : 1;
      return of({ saleId, customerId: '1', type: 'PRODUCTS', soldAt: '2026-11-02T10:02:00Z',
        totalAmount: quantity * 1.5, lines: [{ label: 'Café', quantity, unitPrice: 1.5, totalPrice: quantity * 1.5 }] });
    });
    const { fixture, element } = setup();
    fixture.componentInstance.toggleDetails(sessions[0]);
    fixture.detectChanges();

    expect(element.querySelectorAll('.session-entry')).toHaveLength(2);
    expect(element.querySelector('.session-entry strong')?.textContent).toContain('2 x Café');
    expect(saleGet).toHaveBeenCalledWith('coffee-1');
    expect(saleGet).toHaveBeenCalledWith('coffee-2');
    element.querySelector<HTMLButtonElement>('.entry-group-toggle')!.click();
    fixture.detectChanges();
    expect(element.querySelectorAll('.session-entry')).toHaveLength(1);
    expect(element.querySelector('.session-entry strong')?.textContent).toContain('3 x Café');
    expect(element.querySelector('.session-entry .entry-amount')?.textContent).toContain('4.50');
  });

  it('unchecks or deletes only the chosen debt among identical purchases and standalone debts', () => {
    const { fixture, element } = setup();
    const purchases = ['sale-1', 'sale-2'].map((saleId, index) => ({ saleId, debtId: `debt-${index + 1}`, sessionId: '1', type: 'PRODUCTS', label: 'Produit 1', debtLabel: 'Produit 1', soldAt: '2026-11-02T10:02:00Z', totalAmount: 2, openDebt: true }));
    const debts: CustomerDetails['debts'] = ['debt-1', 'debt-2', 'debt-3'].map((debtId) => ({ debtId, label: 'Produit 1', amount: 2, status: 'OPEN', createdAt: '2026-11-02T10:02:00Z', settledAt: null }));
    const customer = { ...details('1'), purchases, debts };
    customerGet.mockReturnValue(of(customer));
    const page = fixture.componentInstance;
    page.sessionDetails.set({ '1': customer });
    page.expandedSessionIds.set(new Set(['1']));
    fixture.detectChanges();
    expect(element.querySelectorAll('.session-entry .icon-remove')).toHaveLength(3);
    const row = page.sessionEntries(sessions[0], customer).find((entry) => entry.debtId === 'debt-1')!;
    const checkbox = element.querySelector<HTMLInputElement>('.session-entry .entry-check input')!;
    checkbox.checked = false;
    page.toggleEntryDebt(sessions[0], row, { target: checkbox } as unknown as Event);
    expect(reattachDebtToSession).toHaveBeenCalledExactlyOnceWith('debt-1', '1');
    expect(createDebtFromSale).not.toHaveBeenCalled();

    page.deleteEntry(sessions[0], row);
    expect(deleteDebt).toHaveBeenCalledExactlyOnceWith('1', 'debt-1');
    expect(deletePurchase).not.toHaveBeenCalled();
  });

  it('starts an existing walk-in by id or an unknown customer by name', async () => {
    const { fixture } = setup();
    const page = fixture.componentInstance;
    page.selectCustomer(customers[0]);
    page.startClientSession();
    expect(start).toHaveBeenLastCalledWith({ customerId: '4' });
    page.clientForm.controls.customerName.setValue('Nouveau Client');
    await vi.waitFor(() => expect(page.searchingCustomers()).toBe(false));
    page.startClientSession();
    expect(start).toHaveBeenLastCalledWith({ customerName: 'Nouveau Client' });
  });

  it('does not create a duplicate while searching or when an exact name needs selection', async () => {
    customerSearch.mockReturnValue(of(customers));
    const { fixture } = setup();
    const page = fixture.componentInstance;
    page.clientForm.controls.customerName.setValue('Alice Client');
    page.startClientSession();
    expect(start).not.toHaveBeenCalled();
    await vi.waitFor(() => expect(page.searchingCustomers()).toBe(false));
    page.startClientSession();
    expect(start).not.toHaveBeenCalled();
    expect(page.error()).toContain('Sélectionne');
    page.selectCustomer({ ...customers[0], customerId: '1' });
    page.startClientSession();
    expect(start).not.toHaveBeenCalled();
    page.selectCustomer({ ...customers[0], status: 'INACTIVE' });
    page.startClientSession();
    expect(start).not.toHaveBeenCalled();
    page.current.set([{ ...sessions[0], endedAt: '2026-11-02T12:00:00Z', paid: true }]);
    page.selectCustomer({ ...customers[0], customerId: '1' });
    page.startClientSession();
    expect(start).toHaveBeenCalledWith({ customerId: '1' });
  });

  it('ignores stale autocomplete responses after another name is typed', async () => {
    const first = new Subject<Customer[]>();
    const second = new Subject<Customer[]>();
    customerSearch.mockReturnValueOnce(first).mockReturnValueOnce(second);
    const { fixture } = setup();
    const page = fixture.componentInstance;
    page.clientForm.controls.customerName.setValue('ali');
    await vi.waitFor(() => expect(customerSearch).toHaveBeenCalledWith('ali'));
    page.clientForm.controls.customerName.setValue('bob');
    await vi.waitFor(() => expect(customerSearch).toHaveBeenCalledWith('bob'));
    second.next([customers[1]]);
    first.next([customers[0]]);
    expect(page.customerResults()).toEqual([customers[1]]);
  });
});
