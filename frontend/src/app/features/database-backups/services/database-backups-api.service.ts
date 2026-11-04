import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { environment } from '../../../../environments/environment';
import type { DatabaseDump } from '../models/database-backup.models';

@Injectable({ providedIn: 'root' })
export class DatabaseBackupsApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.usersApiUrl}/database-backups`;

  list() {
    return this.http.get<DatabaseDump[]>(this.baseUrl);
  }

  exportDatabase() {
    return this.http.post<DatabaseDump>(`${this.baseUrl}/export`, {});
  }

  upload(file: File) {
    const body = new FormData();
    body.append('file', file, file.name);
    return this.http.post<DatabaseDump>(`${this.baseUrl}/upload`, body);
  }

  restore(fileName: string) {
    return this.http.post<void>(`${this.baseUrl}/${encodeURIComponent(fileName)}/restore`, {});
  }

  download(fileName: string) {
    return this.http.get(`${this.baseUrl}/${encodeURIComponent(fileName)}/content`, {
      observe: 'response',
      responseType: 'blob',
    });
  }

  saveDownload(response: HttpResponse<Blob>, fallbackName: string): void {
    if (!response.body) {
      return;
    }
    const disposition = response.headers.get('content-disposition') ?? '';
    const match = disposition.match(/filename\*?=(?:UTF-8'')?"?([^";]+)"?/i);
    const fileName = match?.[1] ? decodeURIComponent(match[1]) : fallbackName;
    const url = URL.createObjectURL(response.body);
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    link.click();
    URL.revokeObjectURL(url);
  }
}
