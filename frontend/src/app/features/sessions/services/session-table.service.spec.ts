import type { CafeSession } from '../models/session.models';
import type { SessionTableFilters, SessionTableSort } from '../models/session-table.models';
import { SessionTableService } from './session-table.service';

describe('SessionTableService', () => {
  const service = new SessionTableService();
  const filters: SessionTableFilters = { term: '', startedAfter: '', state: '' };
  const session = (id: string, name: string, hour: number, changes: Partial<CafeSession> = {}): CafeSession => ({
    sessionId: id, customerId: id, customerName: name, customerType: 'WALK_IN',
    remainingMinutes: 0, displayRemainingMinutes: 0, startedAt: `2026-11-02T${hour}:00:00Z`,
    endedAt: null, paused: false, paid: false, consumedSeconds: 0, consumedMinutes: 0,
    calculatedPrice: 0, purchasesAmount: 0, openDebtAmount: 0, totalAmountDue: 0, totalPaidAmount: 0,
    ...changes,
  });
  const sessions = [
    session('a', 'Alice', 10),
    session('b', 'Émile', 11, { paused: true }),
    session('c', 'Bob', 12, { endedAt: '2026-11-02T13:00:00Z' }),
    session('d', 'Zoé', 13, { endedAt: '2026-11-02T14:00:00Z', paid: true }),
  ];

  const sorts: Array<{ sort: SessionTableSort; expected: string[] }> = [
    { sort: { column: 'startedAt', direction: 'desc' }, expected: ['d', 'c', 'b', 'a'] },
    { sort: { column: 'startedAt', direction: 'asc' }, expected: ['a', 'b', 'c', 'd'] },
    { sort: { column: 'customerName', direction: 'asc' }, expected: ['a', 'c', 'b', 'd'] },
    { sort: { column: 'customerName', direction: 'desc' }, expected: ['d', 'b', 'c', 'a'] },
    { sort: { column: 'state', direction: 'asc' }, expected: ['a', 'b', 'c', 'd'] },
    { sort: { column: 'state', direction: 'desc' }, expected: ['d', 'c', 'b', 'a'] },
  ];

  it.each(sorts)('sorts by $sort.column $sort.direction', ({ sort, expected }) => {
    expect(service.filterAndSort(sessions, filters, sort).map((item) => item.sessionId)).toEqual(expected);
  });

  it('combines name, date and state filters without changing the source array', () => {
    const source = [...sessions];
    const filtered = service.filterAndSort(source, {
      term: '  ÉM  ', startedAfter: '2026-11-02T11:00:00Z', state: 'Pause',
    }, { column: 'customerName', direction: 'asc' });
    expect(filtered.map((item) => item.sessionId)).toEqual(['b']);
    expect(source).toEqual(sessions);
    expect(service.filterAndSort(source, { ...filters, startedAfter: '2026-11-02T12:00:00Z', state: 'Pause' }, sorts[0].sort)).toEqual([]);
  });

  it('uses the displayed state precedence for filtering and updates', () => {
    expect(service.stateOf({ ...sessions[0], paid: true, paused: true })).toBe('Terminee');
    expect(service.stateOf({ ...sessions[2], paused: true })).toBe('A payer');
    const resumed = sessions.map((item) => item.sessionId === 'b' ? { ...item, paused: false } : item);
    expect(service.filterAndSort(resumed, { ...filters, state: 'Pause' }, sorts[0].sort)).toEqual([]);
  });

  it('keeps a deterministic order for equal dates and customer names', () => {
    const items = [session('z', 'Alice', 10), session('a', 'Alice', 10), session('b', 'Bob', 10)];
    expect(service.filterAndSort(items, filters, sorts[0].sort).map((item) => item.sessionId)).toEqual(['a', 'z', 'b']);
    expect(items.map((item) => item.sessionId)).toEqual(['z', 'a', 'b']);
  });
});
