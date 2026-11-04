import { Injectable, inject } from '@angular/core';
import { from, map, mergeMap, of, switchMap, toArray, type Observable } from 'rxjs';

import type { CustomerDetails } from '../../customers/models/customer.models';
import { CustomersApiService } from '../../customers/services/customers-api.service';
import { SalesApiService } from '../../sales/services/sales-api.service';

@Injectable({ providedIn: 'root' })
export class SessionCustomerDetailsService {
  private readonly customersApi = inject(CustomersApiService);
  private readonly salesApi = inject(SalesApiService);

  get(customerId: string): Observable<CustomerDetails> {
    return this.customersApi.get(customerId).pipe(
      switchMap((details) => {
        const missingIds = [...new Set(details.purchases
          .filter((purchase) => !purchase.lines?.length)
          .map((purchase) => purchase.saleId))];
        if (!missingIds.length) return of(details);
        return from(missingIds).pipe(
          mergeMap((saleId) => this.salesApi.get(saleId), 4),
          toArray(),
          map((sales) => {
            const linesBySaleId = new Map(sales.map((sale) => [sale.saleId, sale.lines]));
            if (missingIds.some((saleId) => !linesBySaleId.get(saleId)?.length)) {
              throw new Error('Les lignes enregistrees de la vente sont absentes.');
            }
            return {
              ...details,
              purchases: details.purchases.map((purchase) => purchase.lines?.length
                ? purchase
                : { ...purchase, lines: linesBySaleId.get(purchase.saleId)! }),
            };
          }),
        );
      }),
    );
  }
}
