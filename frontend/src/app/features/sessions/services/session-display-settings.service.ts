import { Injectable, signal } from '@angular/core';

export interface SessionDisplaySettings {
  currentPageSize: number;
  dayPageSize: number;
}

const DEFAULT_SETTINGS: SessionDisplaySettings = {
  currentPageSize: 5,
  dayPageSize: 8,
};

@Injectable({ providedIn: 'root' })
export class SessionDisplaySettingsService {
  readonly settings = signal<SessionDisplaySettings>({ ...DEFAULT_SETTINGS });

  update(settings: SessionDisplaySettings): SessionDisplaySettings {
    const normalized = {
      currentPageSize: this.normalize(settings.currentPageSize, DEFAULT_SETTINGS.currentPageSize),
      dayPageSize: this.normalize(settings.dayPageSize, DEFAULT_SETTINGS.dayPageSize),
    };
    this.settings.set(normalized);
    return normalized;
  }

  private normalize(value: number | string | undefined, fallback: number): number {
    const numericValue = typeof value === 'string' ? Number(value) : value;
    if (!numericValue || Number.isNaN(numericValue) || numericValue < 1) {
      return fallback;
    }
    return Math.floor(numericValue);
  }
}
