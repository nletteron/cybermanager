import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';

import type { DatabaseDump } from '../models/database-backup.models';
import { DatabaseBackupsApiService } from '../services/database-backups-api.service';

@Component({
  selector: 'app-database-backups-page',
  imports: [CommonModule],
  templateUrl: './database-backups-page.component.html',
  styleUrl: './database-backups-page.component.css',
})
export class DatabaseBackupsPageComponent {
  private readonly api = inject(DatabaseBackupsApiService);

  readonly dumps = signal<DatabaseDump[]>([]);
  readonly selectedFileName = signal('');
  readonly loading = signal(false);
  readonly exporting = signal(false);
  readonly uploading = signal(false);
  readonly restoring = signal(false);
  readonly error = signal('');
  readonly success = signal('');

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set('');
    this.api.list().subscribe({
      next: (dumps) => {
        this.dumps.set(dumps);
        if (!dumps.some((dump) => dump.fileName === this.selectedFileName())) {
          this.selectedFileName.set('');
        }
        this.loading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.error.set(this.resolveError(error, 'Impossible de charger les dumps.'));
        this.loading.set(false);
      },
    });
  }

  exportDatabase(): void {
    this.exporting.set(true);
    this.clearMessages();
    this.api.exportDatabase().subscribe({
      next: (dump) => {
        this.selectedFileName.set(dump.fileName);
        this.success.set(`Dump ${dump.fileName} genere avec succes.`);
        this.exporting.set(false);
        this.load();
        this.download(dump.fileName);
      },
      error: (error: HttpErrorResponse) => {
        this.error.set(this.resolveError(error, 'Export impossible.'));
        this.exporting.set(false);
      },
    });
  }

  importDump(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    if (!file) {
      return;
    }
    if (!file.name.toLowerCase().endsWith('.dump')) {
      this.error.set('Le fichier doit utiliser extension .dump.');
      return;
    }
    this.uploading.set(true);
    this.clearMessages();
    this.api.upload(file).subscribe({
      next: (dump) => {
        this.selectedFileName.set(dump.fileName);
        this.success.set(`Dump ${dump.fileName} importe avec succes.`);
        this.uploading.set(false);
        this.load();
      },
      error: (error: HttpErrorResponse) => {
        this.error.set(this.resolveError(error, 'Import impossible.'));
        this.uploading.set(false);
      },
    });
  }

  select(fileName: string): void {
    this.selectedFileName.set(fileName);
  }

  restoreSelected(): void {
    const fileName = this.selectedFileName();
    if (!fileName) {
      return;
    }
    const confirmed = window.confirm(
      `Restaurer ${fileName} ? Les donnees actuelles du schema cybermanager seront remplacees.`,
    );
    if (!confirmed) {
      return;
    }
    this.restoring.set(true);
    this.clearMessages();
    this.api.restore(fileName).subscribe({
      next: () => {
        this.success.set(`La base a ete restauree depuis ${fileName}.`);
        this.restoring.set(false);
        this.load();
      },
      error: (error: HttpErrorResponse) => {
        this.error.set(this.resolveError(error, 'Restauration impossible.'));
        this.restoring.set(false);
      },
    });
  }

  download(fileName: string): void {
    this.api.download(fileName).subscribe({
      next: (response) => this.api.saveDownload(response, fileName),
      error: (error: HttpErrorResponse) => this.error.set(this.resolveError(error, 'Telechargement impossible.')),
    });
  }

  formatSize(size: number): string {
    if (size < 1024) {
      return `${size} o`;
    }
    if (size < 1024 * 1024) {
      return `${(size / 1024).toFixed(1)} Ko`;
    }
    return `${(size / (1024 * 1024)).toFixed(1)} Mo`;
  }

  private clearMessages(): void {
    this.error.set('');
    this.success.set('');
  }

  private resolveError(error: HttpErrorResponse, fallback: string): string {
    return error.error?.message ?? fallback;
  }
}
