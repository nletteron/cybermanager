export type SessionState = 'En cours' | 'Pause' | 'A payer' | 'Terminee';

export const SESSION_STATES: readonly SessionState[] = ['En cours', 'Pause', 'A payer', 'Terminee'];

export type SessionSortColumn = 'customerName' | 'state' | 'startedAt';

export interface SessionTableSort {
  column: SessionSortColumn;
  direction: 'asc' | 'desc';
}

export interface SessionTableFilters {
  term: string;
  startedAfter: string;
  state: SessionState | '';
}
