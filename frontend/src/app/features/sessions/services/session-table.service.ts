import { Injectable } from '@angular/core';

import type { CafeSession } from '../models/session.models';
import { SESSION_STATES, type SessionState, type SessionTableFilters, type SessionTableSort } from '../models/session-table.models';

@Injectable({ providedIn: 'root' })
export class SessionTableService {
  private readonly collator = new Intl.Collator('fr', { sensitivity: 'base', numeric: true });

  stateOf(session: CafeSession): SessionState {
    if (session.paid) return 'Terminee';
    if (session.endedAt) return 'A payer';
    return session.paused ? 'Pause' : 'En cours';
  }

  filterAndSort(sessions: readonly CafeSession[], filters: SessionTableFilters, sort: SessionTableSort): CafeSession[] {
    const term = filters.term.trim().toLocaleLowerCase('fr');
    const startedAfter = filters.startedAfter ? new Date(filters.startedAfter).getTime() : null;
    const direction = sort.direction === 'asc' ? 1 : -1;

    return sessions.filter((session) =>
      (!term || session.customerName.toLocaleLowerCase('fr').includes(term))
      && (startedAfter === null || new Date(session.startedAt).getTime() >= startedAfter)
      && (!filters.state || this.stateOf(session) === filters.state)
    ).sort((left, right) => {
      let comparison: number;
      switch (sort.column) {
        case 'customerName':
          comparison = this.collator.compare(left.customerName, right.customerName);
          break;
        case 'state':
          comparison = SESSION_STATES.indexOf(this.stateOf(left)) - SESSION_STATES.indexOf(this.stateOf(right));
          break;
        case 'startedAt':
          comparison = new Date(left.startedAt).getTime() - new Date(right.startedAt).getTime();
          break;
      }
      return comparison * direction
        || new Date(right.startedAt).getTime() - new Date(left.startedAt).getTime()
        || this.collator.compare(left.customerName, right.customerName)
        || left.sessionId.localeCompare(right.sessionId);
    });
  }
}
