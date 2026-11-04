import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../../../environments/environment';
import type { Category, CreateCategory } from '../models/category.models';
@Injectable({ providedIn: 'root' })
export class CategoriesApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.usersApiUrl}/categories`;
  search(term = '') { return this.http.get<Category[]>(this.baseUrl, { params: term ? new HttpParams().set('term', term) : undefined }); }
  create(payload: CreateCategory) { return this.http.post<Category>(this.baseUrl, payload); }
}
