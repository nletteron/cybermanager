import { CommonModule } from '@angular/common';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { catchError, debounceTime, distinctUntilChanged, forkJoin, interval, map, of, switchMap, timer } from 'rxjs';

import type { Customer, CustomerDetails, CustomerPurchase } from '../../customers/models/customer.models';
import { CustomersApiService } from '../../customers/services/customers-api.service';
import type { Product } from '../../products/models/product.models';
import { ProductsApiService } from '../../products/services/products-api.service';
import type { ConnectionPricingTier } from '../../sales/models/sales.models';
import type { SubscriptionOffer } from '../../subscriptions/models/subscription-offer.models';
import { SubscriptionOffersApiService } from '../../subscriptions/services/subscription-offers-api.service';
import { SalesApiService } from '../../sales/services/sales-api.service';
import { AppLoggerService } from '../../../core/services/app-logger.service';
import type { CafeSession } from '../models/session.models';
import type { SessionSortColumn, SessionState, SessionTableSort } from '../models/session-table.models';
import { SessionStateFilterComponent } from '../components/session-state-filter.component';
import type { SessionDetailEntry, SessionDetailRow } from '../models/session-detail-entry.models';
import type { SessionDisplayPreference } from '../models/session-display-preference.models';
import { SessionEntriesService } from '../services/session-entries.service';
import { SessionCustomerDetailsService } from '../services/session-customer-details.service';
import { SessionTableService } from '../services/session-table.service';
import { SessionsApiService } from '../services/sessions-api.service';
import { CustomerCommentComponent } from '../../customers/components/customer-comment.component';

@Component({
  selector: 'app-sessions-page',
  imports: [CommonModule, ReactiveFormsModule, SessionStateFilterComponent, CustomerCommentComponent],
  template: `
    <section class="page">
      <header class="hero">
        <div>
          <p class="eyebrow">Flux comptoir</p>
          <h2>Sessions de connexion</h2>
        </div>
        <div class="hero-actions">
          <button type="button" class="ghost" (click)="restartSessionsDay()">Fin de journee</button>
        </div>
      </header>

      <p class="error" *ngIf="error()">{{ error() }}</p>
      <p class="notice" *ngIf="notice()">{{ notice() }}</p>

      <div class="grid">
        <article class="panel panel-left stack">
          <form [formGroup]="clientForm" (ngSubmit)="startClientSession()" class="stack client-search-form">
            <label class="field">
              <span>Client</span>
              <input formControlName="customerName" placeholder="Rechercher ou saisir un nom" autocomplete="off" aria-controls="session-customer-results" />
            </label>
            <p class="meta" role="status" *ngIf="searchingCustomers()">Recherche…</p>
            <p class="error" role="alert" *ngIf="customerSearchError()">{{ customerSearchError() }}</p>
            <div id="session-customer-results" class="customer-results" *ngIf="customerResults().length">
              <button type="button" class="customer-option" *ngFor="let customer of customerResults()" (click)="selectCustomer(customer)" [disabled]="customer.status !== 'ACTIVE' || hasCurrentSession(customer.customerId)">
                <span class="customer-name" [title]="customer.name">{{ customer.name }}</span>
                <small>{{ customerTypeLabel(customer.type) }}<span *ngIf="customer.type === 'SUBSCRIBER'"> · {{ customer.remainingMinutes }} min</span><span *ngIf="hasCurrentSession(customer.customerId)"> · Session déjà ouverte</span><span *ngIf="customer.status !== 'ACTIVE'"> · Inactif</span></small>
              </button>
            </div>
            <div class="selected-customer" *ngIf="selectedCustomer() as customer">
              <span class="meta">{{ customerTypeLabel(customer.type) }}<span *ngIf="customer.type === 'SUBSCRIBER'"> · {{ customer.remainingMinutes }} min disponibles</span></span>
              <button type="button" class="text-action" (click)="clearCustomerSelection()">Effacer la sélection</button>
            </div>
            <button type="submit" [disabled]="searchingCustomers() || startingSession() || !!customerSearchError()">{{ startingSession() ? 'Démarrage…' : 'Démarrer la session' }}</button>
          </form>

        </article>

        <article class="panel panel-right stack">
          <section class="stack current-section">
            <div class="section-head">
              <div class="section-title-group">
                <h3>Sessions en cours</h3>
                <button type="button" class="ghost filter-toggle" (click)="showCurrentFilters.set(!showCurrentFilters())" [attr.aria-expanded]="showCurrentFilters()">
                  <span class="filter-icon" aria-hidden="true"></span>
                  <span>Filtres</span>
                </button>
              </div>
              <div class="section-tools">
                <p class="meta">{{ filteredCurrent().length }} resultat(s)</p>
              </div>
            </div>
            <div class="filters-grid collapsible" [class.is-collapsed]="!showCurrentFilters()" [attr.inert]="showCurrentFilters() ? null : ''">
              <label class="field">
                <span>Recherche par nom</span>
                <input [formControl]="currentFilters.controls.term" placeholder="Nom du client" />
              </label>
              <label class="field">
                <span>Debut a partir de</span>
                <input type="datetime-local" [formControl]="currentFilters.controls.startedAfter" />
              </label>
              <app-session-state-filter [value]="currentState()" (valueChange)="currentState.set($event)" />
            </div>
            <div class="table-shell fixed-shell">
              <table class="table">
                <thead>
                  <tr>
                    <th class="center-cell indicator-head" scope="col"></th>
                    <th scope="col" [attr.aria-sort]="currentAriaSort('customerName')">
                      <button type="button" class="table-sort" (click)="sortCurrent('customerName')">Client <span aria-hidden="true">{{ currentSortIndicator('customerName') }}</span></button>
                    </th>
                    <th class="center-cell" scope="col">Type</th>
                    <th class="center-cell" scope="col" [attr.aria-sort]="currentAriaSort('state')">
                      <button type="button" class="table-sort" (click)="sortCurrent('state')">État <span aria-hidden="true">{{ currentSortIndicator('state') }}</span></button>
                    </th>
                    <th class="center-cell" scope="col">Credit</th>
                    <th class="center-cell" scope="col">A payer</th>
                    <th class="center-cell" scope="col">Consomme</th>
                    <th class="center-cell" scope="col" [attr.aria-sort]="currentAriaSort('startedAt')">
                      <button type="button" class="table-sort" (click)="sortCurrent('startedAt')">Début <span aria-hidden="true">{{ currentSortIndicator('startedAt') }}</span></button>
                    </th>
                    <th class="actions-head" colspan="5"></th>
                  </tr>
                </thead>
                <tbody>
                  <ng-container *ngFor="let item of filteredCurrent()">
                    <tr class="session-row" tabindex="0" (click)="onSessionRowInteraction(item, $event)" (keydown.enter)="onSessionRowInteraction(item, $event)" (keydown.space)="onSessionRowInteraction(item, $event)" [attr.aria-expanded]="expandedSessionIds().has(item.sessionId)" [attr.aria-controls]="'session-detail-' + item.sessionId" [class.active-row]="sessionState(item) === 'En cours'" [class.paused-row]="sessionState(item) === 'Pause'" [class.awaiting-payment-row]="sessionState(item) === 'A payer'" [class.paid-row]="sessionState(item) === 'Terminee'">
                      <td class="center-cell indicator-cell">
                        <span *ngIf="hasOpenDebt(item); else standardUserIndicator" class="session-indicator debt-session-indicator" title="Client avec dette ouverte" aria-label="Client avec dette ouverte">D</span>
                        <ng-template #standardUserIndicator>
                          <span class="session-indicator ok-session-indicator" title="Client sans dette ouverte" aria-label="Client sans dette ouverte">
                            ✓
                          </span>
                        </ng-template>
                      </td>
                      <td class="client-cell">
                        <button type="button" class="link-button client-link" (click)="openCustomerModal(item.customerId)">
                          {{ item.customerName }}
                        </button>
                      </td>
                      <td class="center-cell"><span [class]="customerTypeChipClass(item.customerType)">{{ customerTypeLabel(item.customerType) }}</span></td>
                      <td class="center-cell"><span class="inline-state" [class.paused-chip]="sessionState(item) === 'Pause'" [class.active-chip]="sessionState(item) === 'En cours'" [class.pending-chip]="sessionState(item) === 'A payer'" [class.paid-chip]="sessionState(item) === 'Terminee'">{{ sessionState(item) }}</span></td>
                      <td class="center-cell">
                        <span *ngIf="item.customerType === 'SUBSCRIBER'; else noCredit">{{ formatRemaining(item) }}</span>
                        <ng-template #noCredit><span class="empty-chip">Aucun</span></ng-template>
                      </td>
                      <td class="center-cell" [class.pending-amount]="isAwaitingPayment(item)">{{ item.totalAmountDue | number:'1.2-2' }} EUR</td>
                      <td class="center-cell">{{ formatElapsed(item) }}</td>
                      <td class="center-cell">{{ item.startedAt | date:'dd/MM/yyyy HH:mm' }}</td>
                      <td class="action-cell"><button type="button" class="ghost" *ngIf="canPauseOrResume(item)" (click)="togglePause(item)">{{ item.paused ? 'Reprendre' : 'Pause' }}</button></td>
                      <td class="action-cell"><button type="button" *ngIf="canStop(item)" (click)="stopSession(item)">Arreter</button></td>
                      <td class="action-cell"><button type="button" *ngIf="canPay(item)" (click)="openPaymentDialog(item)">Payer</button></td>
                      <td class="action-cell">
                        <div class="session-sale-actions">
                          <button type="button" class="secondary sale-action" (click)="openProductSaleModal(item)" aria-label="Vendre un produit" title="Vendre un produit">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
                              <path d="M3 3h2l2.5 12h10l3-9H6" />
                              <circle cx="9" cy="20" r="1" /><circle cx="17" cy="20" r="1" />
                            </svg>
                          </button>
                          <button type="button" class="secondary sale-action" (click)="openSubscriptionSaleModal(item)" aria-label="Vendre un abonnement" title="Vendre un abonnement">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
                              <rect x="3" y="4" width="18" height="16" rx="2" />
                              <path d="M7 8h10M7 12h6M7 16h4" />
                            </svg>
                          </button>
                        </div>
                      </td>
                      <td class="action-cell detail-toggle-cell"><button type="button" class="detail-toggle" (click)="toggleDetails(item)" [attr.aria-expanded]="expandedSessionIds().has(item.sessionId)" [attr.aria-controls]="'session-detail-' + item.sessionId" [attr.aria-label]="(expandedSessionIds().has(item.sessionId) ? 'Replier le détail de ' : 'Déplier le détail de ') + item.customerName">{{ expandedSessionIds().has(item.sessionId) ? '−' : '+' }}</button></td>
                    </tr>
                    <tr class="detail-row" *ngIf="expandedSessionIds().has(item.sessionId)" [id]="'session-detail-' + item.sessionId">
                      <td colspan="13">
                        <div class="detail-card" *ngIf="sessionDetails()[item.customerId] as details; else detailLoading">
                          <div class="detail-grid">
                            <section class="stack">
                              <div class="section-title-group">
                                <h4>Achats de la session et dettes</h4>
                                <button type="button" class="ghost detail-toggle entry-group-toggle" (click)="toggleEntryGrouping(item)" [disabled]="loadingGroupingPreference() || savingGroupingPreference()" [attr.aria-pressed]="isEntryGrouping(item)" [attr.aria-label]="isEntryGrouping(item) ? 'Afficher les lignes séparément' : 'Fusionner les lignes identiques'" [title]="isEntryGrouping(item) ? 'Afficher les lignes séparément' : 'Fusionner les lignes identiques (choix enregistré)'">
                                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"><path d="M4 5h4l8 7h4M4 19h4l8-7M17 9l3 3-3 3" /></svg>
                                </button>
                              </div>
                              <div class="purchase-list" *ngIf="sessionEntries(item, details).length > 0; else noPurchases">
                                <article class="purchase-item compact session-entry" [class.debt-entry]="entry.kind === 'debt'" *ngFor="let entry of sessionRows(item, details); trackBy: trackEntry">
                                  <strong [title]="entryLabel(entry)">
                                    <span class="money-alert-icon debt-entry-icon" *ngIf="entry.openDebt" title="Dette ouverte"><span class="bill back"></span><span class="bill front"></span><span class="slash"></span></span>
                                    {{ entryLabel(entry) }}
                                  </strong>
                                  <span class="entry-date">{{ entry.occurredAt | date:'dd/MM/yyyy HH:mm' }}</span>
                                  <span class="entry-amount">{{ entry.amount | number:'1.2-2' }} EUR</span>
                                  <span class="entry-actions-cell" *ngIf="entry.entries.length === 1; else groupedEntryActions">
                                    <label class="entry-check" title="Cocher pour mettre en dette ou regler la dette">
                                      <input type="checkbox" [checked]="entry.openDebt" (change)="toggleEntryDebt(item, entry, $event)" />
                                    </label>
                                    <button type="button" class="ghost compact-remove icon-remove" (click)="deleteEntry(item, entry)" [disabled]="pendingEntryIds().has(entry.id)" [attr.aria-label]="'Supprimer : ' + entry.label" title="Supprimer cette ligne">-</button>
                                  </span>
                                  <ng-template #groupedEntryActions>
                                    <span class="entry-actions-cell">
                                      <button type="button" class="ghost detail-toggle" (click)="showSeparateEntries(item)" [attr.aria-label]="'Afficher séparément les ' + entry.entries.length + ' lignes de ' + entry.label" title="Voir les lignes sans modifier le choix de fusion enregistré">{{ entry.entries.length }}</button>
                                    </span>
                                  </ng-template>
                                </article>
                              </div>
                            <ng-template #noPurchases><p class="muted">Aucun achat rattache a cette session ni dette ouverte.</p></ng-template>
                            </section>

                            <section class="stack customer-comment-column">
                              <app-customer-comment [customerId]="item.customerId" />
                            </section>
                          </div>
                        </div>
                        <ng-template #detailLoading>
                          <p class="muted">Chargement du detail...</p>
                        </ng-template>
                      </td>
                    </tr>
                  </ng-container>
                  <tr *ngIf="filteredCurrent().length === 0">
                    <td colspan="13" class="muted">{{ current().length ? 'Aucune session ne correspond aux filtres.' : 'Aucune session en cours.' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>
        </article>
      </div>

      <div class="modal-backdrop" *ngIf="activePayTargetSession() as session" (click)="closePaymentDialog()">
        <div class="confirm-modal" (click)="$event.stopPropagation()">
          <div class="section-head">
            <div>
              <h3>Regler la session</h3>
              <p class="meta">{{ session.customerName }}</p>
            </div>
            <button type="button" class="ghost" (click)="closePaymentDialog()">Fermer</button>
          </div>
          <div class="stack confirm-body">
            <p class="muted">Le compteur est arrete. Encaisse le montant regle maintenant et finalise la session.</p>
            <div class="confirm-summary">
              <p class="muted"><strong>Cout connexion :</strong> {{ paymentDialogConnectionAmount(session) | number:'1.2-2' }} EUR</p>
              <p class="muted"><strong>Achats de la session :</strong> {{ paymentDialogPurchaseAmount(session) | number:'1.2-2' }} EUR</p>
              <p class="muted" *ngIf="paymentDialogOpenDebtAmount(session) > 0">
                <strong>Dettes ouvertes :</strong> {{ paymentDialogOpenDebtAmount(session) | number:'1.2-2' }} EUR
                <span class="meta">(affichees, non incluses dans ce reglement)</span>
              </p>
              <p class="confirm-total"><strong>A payer :</strong> {{ paymentDialogTotalAmount(session) | number:'1.2-2' }} EUR</p>
              <div *ngIf="paymentSelectedOffers().length > 0">
                <p><strong>Nouveaux abonnements ajoutes :</strong></p>
                <div class="purchase-list">
                  <article class="purchase-item compact session-entry" *ngFor="let offer of paymentSelectedOffers(); let index = index">
                    <strong>{{ offer.name }}</strong>
                    <span class="entry-date">{{ offer.includedMinutes }} min</span>
                    <span class="entry-amount">{{ offer.price | number:'1.2-2' }} EUR</span>
                    <button type="button" class="ghost compact-remove" (click)="removePaymentOffer(index)">Retirer</button>
                  </article>
                </div>
                <p class="muted" *ngIf="paymentResolutionForm.controls.createSubscriptionDebt.value">Ces nouveaux abonnements seront mis en dette.</p>
              </div>
            </div>
            <form class="stack" [formGroup]="paymentResolutionForm">
              <label class="field">
                <span>Montant regle par le client</span>
                <input type="number" min="0" step="0.01" formControlName="amountPaid" />
              </label>
              <p class="muted">Montant conseille : {{ paymentDialogTotalAmount(session) | number:'1.2-2' }} EUR</p>
              <ng-container *ngIf="canAddSubscriptionDuringPayment()">
                <label class="field">
                  <span>Ajouter un abonnement</span>
                  <select formControlName="offerToAddId">
                    <option value="">Choisir une offre</option>
                    <option *ngFor="let offer of offers()" [value]="offer.offerId">{{ offer.name }} - {{ offer.includedMinutes }} min - {{ offer.price | number:'1.2-2' }} EUR</option>
                  </select>
                </label>
                <div class="actions inline-actions">
                  <button type="button" class="ghost" (click)="addPaymentOffer()" [disabled]="!paymentResolutionForm.controls.offerToAddId.value">Ajouter cet abonnement</button>
                </div>
                <p class="muted">Ajoute un ou plusieurs abonnements pendant le reglement si besoin.</p>
                <label class="checkbox" *ngIf="paymentSelectedOffers().length > 0">
                  <input type="checkbox" formControlName="createSubscriptionDebt" />
                  <span>Mettre les nouveaux abonnements en dette</span>
                </label>
                <p class="muted" *ngIf="paymentSelectedOffers().length === 0 && paymentDialogRawOvertimeMinutes(session) === 0">
                  Aucun depassement en cours. Un abonnement ajoute sera simplement ajoute au montant du reglement ou mis en dette.
                </p>
                <p class="muted" *ngIf="paymentSelectedOffers().length > 0 && paymentDialogOvertimeMinutes(session) === 0">
                  Les nouveaux abonnements couvrent completement le depassement.
                </p>
                <p class="muted" *ngIf="paymentSelectedOffers().length > 0 && paymentDialogOvertimeMinutes(session) > 0">
                  Apres ces abonnements, il restera {{ paymentDialogOvertimeMinutes(session) }} min en trop a payer.
                </p>
                <p class="muted" *ngIf="paymentSelectedOffers().length === 0 && paymentDialogRawOvertimeMinutes(session) > 0">
                  Sans nouvel abonnement, le temps en trop restera facture comme depassement.
                </p>
              </ng-container>
            </form>
            <div class="actions">
              <button type="button" class="ghost" (click)="closePaymentDialog()">Annuler</button>
              <button type="button" class="ghost" (click)="confirmPaymentWithInvoice()">Valider et generer la facture PDF</button>
              <button type="button" (click)="confirmPayment()">Valider le paiement</button>
            </div>
          </div>
        </div>
      </div>

      <div class="modal-backdrop" *ngIf="activeCustomerModal() as customer" (click)="closeCustomerModal()">
        <div class="confirm-modal customer-modal" (click)="$event.stopPropagation()">
          <div class="section-head">
            <div>
              <h3>Fiche client</h3>
              <p class="meta">{{ customer.name }}</p>
            </div>
            <button type="button" class="ghost" (click)="closeCustomerModal()">Fermer</button>
          </div>

          <div class="stack confirm-body">
            <div class="facts customer-facts">
              <p><strong>Type :</strong> <span [class]="customerTypeChipClass(customer.type)">{{ customerTypeLabel(customer.type) }}</span></p>
              <p><strong>Abonnements :</strong> {{ customer.currentSubscriptionLabel ?? 'Aucun' }}</p>
              <p *ngIf="customer.type === 'SUBSCRIBER'" class="muted">Les abonnements se cumulent.</p>
              <p><strong>Credit disponible :</strong> {{ customer.remainingMinutes }} min</p>
              <p><strong>Total paye sur tous les achats :</strong> {{ totalPaidPurchases(customer) | number:'1.2-2' }} EUR</p>
              <p><strong>Total paye sur forfaits et produits :</strong> {{ totalPaidSales(customer) | number:'1.2-2' }} EUR</p>
              <p *ngIf="customer.debts.length > 0" class="debt-summary"><strong>Dettes :</strong> <span class="money-alert-icon debt-indicator" title="Dettes ouvertes"><span class="bill back"></span><span class="bill front"></span><span class="slash"></span></span></p>
            </div>

            <div class="stack">
              <h4>Achats du client</h4>
              <table class="table compact" *ngIf="customerPurchaseEntries(customer).length > 0; else noCustomerPurchases">
                <thead>
                  <tr><th>Quand</th><th>Type</th><th>Detail</th><th>Total</th></tr>
                </thead>
                <tbody>
                  <tr *ngFor="let entry of visibleCustomerPurchaseEntries(customer); let index = index" [class.debt-row]="entry.isDebt">
                    <td>{{ entry.occurredAt | date:'dd/MM/yyyy HH:mm' }}</td>
                    <td>{{ entry.type }}</td>
                    <td>{{ entry.detail }}</td>
                    <td class="customer-entry-total">
                      <strong>{{ entry.total | number:'1.2-2' }} EUR</strong>
                      <button
                        type="button"
                        class="ghost compact-remove entry-expand-toggle"
                        *ngIf="showMoreCustomerPurchasesButton(customer, index)"
                        (click)="showMoreCustomerPurchases()"
                        aria-label="Afficher 10 achats de plus"
                        title="Afficher 10 achats de plus"
                      >+</button>
                      <button
                        type="button"
                        class="ghost compact-remove entry-expand-toggle"
                        *ngIf="showLessCustomerPurchasesButton(customer, index)"
                        (click)="showLessCustomerPurchases()"
                        aria-label="Replier la liste des achats"
                        title="Replier la liste des achats"
                      >-</button>
                    </td>
                  </tr>
                </tbody>
                <tfoot>
                  <tr>
                    <td colspan="3"><strong>Total paye</strong></td>
                    <td><strong>{{ totalPaidPurchases(customer) | number:'1.2-2' }} EUR</strong></td>
                  </tr>
                </tfoot>
              </table>
              <ng-template #noCustomerPurchases><p class="muted">Aucun achat ni dette ouverte.</p></ng-template>
            </div>
          </div>
        </div>
      </div>
      <div class="modal-backdrop" *ngIf="saleTargetSession() as session" (click)="closeSaleModal()">
        <div class="confirm-modal" (click)="$event.stopPropagation()">
          <div class="section-head">
            <div>
              <h3>{{ saleModalType() === 'subscription' ? 'Vendre un abonnement' : 'Vendre un produit' }}</h3>
              <p class="meta">{{ session.customerName }} - <span [class]="customerTypeChipClass(session.customerType)">{{ customerTypeLabel(session.customerType) }}</span></p>
            </div>
            <button type="button" class="ghost" (click)="closeSaleModal()">Fermer</button>
          </div>
          <form *ngIf="saleModalType() === 'product'; else subscriptionSaleModal" [formGroup]="productSaleForm" (ngSubmit)="sellProductForSession()" class="stack confirm-body">
            <label class="field">
              <span>Produit</span>
              <select formControlName="productId">
                <option value="">Choisir un produit</option>
                <option *ngFor="let item of products()" [value]="item.productId">{{ item.name }} - {{ item.price | number:'1.2-2' }} EUR</option>
              </select>
            </label>
            <label class="field">
              <span>Quantite</span>
              <input type="number" min="1" formControlName="quantity" />
            </label>
            <label class="checkbox">
              <input type="checkbox" formControlName="createDebt" />
              <span>Creer une dette au lieu d'encaisser</span>
            </label>
            <button type="submit" [disabled]="productSaleForm.invalid">Enregistrer la vente</button>
          </form>
          <ng-template #subscriptionSaleModal>
            <form [formGroup]="subscriptionSaleForm" (ngSubmit)="sellSubscriptionForSession()" class="stack confirm-body">
              <label class="field">
                <span>Offre d'abonnement</span>
                <select formControlName="subscriptionOfferId">
                  <option value="">Choisir une offre</option>
                  <option *ngFor="let offer of offers()" [value]="offer.offerId">{{ offer.name }} - {{ offer.includedMinutes }} min</option>
                </select>
              </label>
              <label class="checkbox">
                <input type="checkbox" formControlName="createDebt" />
                <span>Creer une dette au lieu d'encaisser</span>
              </label>
              <button type="submit" [disabled]="subscriptionSaleForm.invalid">Enregistrer la vente</button>
            </form>
          </ng-template>
        </div>
      </div>
    </section>
  `,
  styleUrl: './sessions-page.component.css',
})
export class SessionsPageComponent {
  private readonly customersApi = inject(CustomersApiService);
  private readonly offersApi = inject(SubscriptionOffersApiService);
  private readonly productsApi = inject(ProductsApiService);
  private readonly salesApi = inject(SalesApiService);
  private readonly api = inject(SessionsApiService);
  private readonly logger = inject(AppLoggerService);
  private readonly fb = inject(FormBuilder);
  private readonly sessionTable = inject(SessionTableService);
  private readonly entryService = inject(SessionEntriesService);
  private readonly customerDetailsService = inject(SessionCustomerDetailsService);

  readonly current = signal<CafeSession[]>([]);
  readonly day = signal<CafeSession[]>([]);
  readonly customerResults = signal<Customer[]>([]);
  readonly selectedCustomer = signal<Customer | null>(null);
  readonly searchingCustomers = signal(false);
  readonly startingSession = signal(false);
  readonly customerSearchError = signal('');
  readonly offers = signal<SubscriptionOffer[]>([]);
  readonly products = signal<Product[]>([]);
  readonly pricingTiers = signal<ConnectionPricingTier[]>([]);
  readonly saleTargetSession = signal<CafeSession | null>(null);
  readonly saleModalType = signal<'product' | 'subscription' | null>(null);
  readonly payTargetSession = signal<CafeSession | null>(null);
  readonly customerModalId = signal<string | null>(null);
  readonly customerPurchasesVisibleCount = signal(10);
  readonly paymentOfferIds = signal<string[]>([]);
  readonly expandedSessionIds = signal<ReadonlySet<string>>(new Set());
  readonly groupingPreference = signal<SessionDisplayPreference | null>(null);
  readonly loadingGroupingPreference = signal(true);
  readonly savingGroupingPreference = signal(false);
  readonly separateEntrySessionIds = signal<ReadonlySet<string>>(new Set());
  readonly expandedDaySessionId = signal<string | null>(null);
  readonly sessionDetails = signal<Record<string, CustomerDetails>>({});
  readonly sessionObservedAt = signal<Record<string, number>>({});
  readonly pendingEntryIds = signal<ReadonlySet<string>>(new Set());
  readonly error = signal('');
  readonly notice = signal('');
  readonly dayPage = signal(1);
  readonly currentTerm = signal('');
  readonly currentStartedAfter = signal('');
  readonly currentState = signal<SessionState | ''>('');
  readonly currentSort = signal<SessionTableSort>({ column: 'startedAt', direction: 'desc' });
  readonly dayTerm = signal('');
  readonly dayStartedAfter = signal('');
  readonly now = signal(Date.now());
  readonly showCurrentFilters = signal(true);
  readonly showDayFilters = signal(false);

  readonly clientForm = this.fb.nonNullable.group({ customerName: [''] });
  readonly productSaleForm = this.fb.nonNullable.group({ productId: ['', Validators.required], quantity: [1, [Validators.required, Validators.min(1)]], createDebt: [false] });
  readonly subscriptionSaleForm = this.fb.nonNullable.group({ subscriptionOfferId: ['', Validators.required], createDebt: [false] });
  readonly paymentResolutionForm = this.fb.nonNullable.group({ offerToAddId: [''], createSubscriptionDebt: [false], amountPaid: [0, [Validators.required, Validators.min(0)]] });
  readonly currentFilters = this.fb.nonNullable.group({ term: [''], startedAfter: [''] });
  readonly dayFilters = this.fb.nonNullable.group({ term: [''], startedAfter: [''] });

  readonly filteredCurrent = computed(() => this.sessionTable.filterAndSort(this.current(), {
    term: this.currentTerm(),
    startedAfter: this.currentStartedAfter(),
    state: this.currentState(),
  }, this.currentSort()));
  readonly filteredDay = computed(() => this.filterSessions(this.day().filter((session) => session.paid), this.dayTerm(), this.dayStartedAfter()));
  readonly activePayTargetSession = computed(() => {
    const target = this.payTargetSession();
    if (!target) {
      return null;
    }
    return this.current().find((session) => session.sessionId === target.sessionId) ?? target;
  });
  readonly activeCustomerModal = computed(() => {
    const customerId = this.customerModalId();
    if (!customerId) {
      return null;
    }
    return this.sessionDetails()[customerId] ?? null;
  });
  constructor() {
    this.logger.info('sessions-ui', 'Sessions page initialized');
    this.loadGroupingPreference();
    this.reload();
    this.loadOffers();
    this.salesApi.pricing().subscribe({
      next: (pricing) => this.pricingTiers.set(pricing.tiers),
      error: () => this.pricingTiers.set([]),
    });
    this.productsApi.search('', 'ACTIVE', '').subscribe({
      next: (value) => {
        this.products.set(value);
        this.logger.debug('sessions-ui', 'Products catalog loaded for session flows', { productCount: value.length });
      },
      error: (error: HttpErrorResponse) => {
        this.logger.warn('sessions-ui', 'Unable to preload products catalog', error);
      },
    });

    this.clientForm.controls.customerName.valueChanges.pipe(
      map((value) => value.trim()),
      switchMap((term) => {
        this.selectedCustomer.set(null);
        this.customerResults.set([]);
        this.customerSearchError.set('');
        this.searchingCustomers.set(!!term);
        return term ? timer(150).pipe(
          switchMap(() => this.customersApi.search(term)),
          catchError(() => {
            this.customerSearchError.set('Recherche des clients impossible.');
            return of<Customer[]>([]);
          }),
        ) : of<Customer[]>([]);
      }),
      takeUntilDestroyed(),
    ).subscribe((customers) => {
      this.customerResults.set(customers);
      this.searchingCustomers.set(false);
    });

    this.currentFilters.controls.term.valueChanges.pipe(
      map((value) => value.trim()),
      debounceTime(120),
      distinctUntilChanged(),
      takeUntilDestroyed(),
    ).subscribe((value) => {
      this.currentTerm.set(value);
      this.logger.debug('sessions-ui', 'Current sessions filters updated', { termLength: value.length, startedAfter: this.currentStartedAfter() || null });
    });

    this.currentFilters.controls.startedAfter.valueChanges.pipe(
      distinctUntilChanged(),
      takeUntilDestroyed(),
    ).subscribe((value) => {
      this.currentStartedAfter.set(value);
      this.logger.debug('sessions-ui', 'Current sessions date filter updated', { startedAfter: value || null, termLength: this.currentTerm().length });
    });

    this.dayFilters.controls.term.valueChanges.pipe(
      map((value) => value.trim()),
      debounceTime(120),
      distinctUntilChanged(),
      takeUntilDestroyed(),
    ).subscribe((value) => {
      this.dayTerm.set(value);
      this.dayPage.set(1);
      this.logger.debug('sessions-ui', 'Day sessions filters updated', { termLength: value.length, startedAfter: this.dayStartedAfter() || null });
    });

    this.dayFilters.controls.startedAfter.valueChanges.pipe(
      distinctUntilChanged(),
      takeUntilDestroyed(),
    ).subscribe((value) => {
      this.dayStartedAfter.set(value);
      this.dayPage.set(1);
      this.logger.debug('sessions-ui', 'Day sessions date filter updated', { startedAfter: value || null, termLength: this.dayTerm().length });
    });

    this.paymentResolutionForm.controls.createSubscriptionDebt.valueChanges.pipe(
      distinctUntilChanged(),
      takeUntilDestroyed(),
    ).subscribe(() => {
      this.syncPaymentDialogAmount();
    });

    interval(1000).pipe(takeUntilDestroyed()).subscribe(() => {
      this.now.set(Date.now());
    });
  }

  sortCurrent(column: SessionSortColumn): void {
    this.currentSort.update((sort) => ({
      column,
      direction: sort.column === column
        ? (sort.direction === 'asc' ? 'desc' : 'asc')
        : (column === 'startedAt' ? 'desc' : 'asc'),
    }));
  }

  currentAriaSort(column: SessionSortColumn): 'none' | 'ascending' | 'descending' {
    const sort = this.currentSort();
    return sort.column !== column ? 'none' : sort.direction === 'asc' ? 'ascending' : 'descending';
  }

  currentSortIndicator(column: SessionSortColumn): string {
    const direction = this.currentAriaSort(column);
    return direction === 'none' ? '↕' : direction === 'ascending' ? '↑' : '↓';
  }

  loadOffers(): void {
    this.offersApi.search('', 'ACTIVE').subscribe({
      next: (offers) => {
        this.offers.set(offers);
        this.logger.debug('sessions-ui', 'Subscription offers loaded for session flows', { offerCount: offers.length });
      },
      error: (error: HttpErrorResponse) => {
        this.logger.warn('sessions-ui', 'Subscription offers loading failed', error);
        this.error.set(error.error?.message ?? 'Chargement des offres impossible');
      },
    });
  }

  reload(): void {
    this.logger.debug('sessions-ui', 'Reloading sessions page datasets');
    this.api.current().subscribe({
      next: (value) => {
        this.current.set(value.sessions);
        this.sessionObservedAt.set(Object.fromEntries(value.sessions.map((session) => [session.sessionId, Date.now()])));
        this.error.set('');
        this.logger.info('sessions-ui', 'Current sessions loaded', { count: value.sessions.length });
      },
      error: (error: HttpErrorResponse) => {
        this.logger.warn('sessions-ui', 'Current sessions loading failed', error);
        this.error.set(error.error?.message ?? 'Chargement des sessions en cours impossible');
      },
    });
  }

  restartSessionsDay(): void {
    this.api.restartDay().subscribe({
      next: (value) => {
        this.notice.set(
          value.archivedSessions > 0
            ? `${value.archivedSessions} session(s) ont ete cloturee(s) et passee(s) en dette de fin de journee.`
            : 'Aucune session a cloturer pour la fin de journee.'
        );
        this.reload();
      },
      error: (error: HttpErrorResponse) => this.error.set(this.resolveHttpError(error, 'Redemarrage des sessions impossible')),
    });
  }

  selectCustomer(customer: Customer): void {
    this.selectedCustomer.set(customer);
    this.clientForm.controls.customerName.setValue(customer.name, { emitEvent: false });
    this.customerResults.set([]);
    this.customerSearchError.set('');
  }

  clearCustomerSelection(): void {
    this.selectedCustomer.set(null);
    this.clientForm.reset({ customerName: '' });
  }

  startClientSession(): void {
    if (this.searchingCustomers() || this.startingSession() || this.customerSearchError()) return;
    const customerName = this.clientForm.controls.customerName.value.trim();
    const customer = this.selectedCustomer();
    if (!customerName) {
      this.error.set('Le nom du client est requis.');
      return;
    }
    if (customer && (customer.status !== 'ACTIVE' || this.hasCurrentSession(customer.customerId))) {
      this.error.set(customer.status !== 'ACTIVE' ? 'Ce client est inactif.' : 'Ce client a déjà une session ouverte.');
      return;
    }
    if (!customer && this.customerResults().some((item) => item.name.trim().toLocaleLowerCase('fr') === customerName.toLocaleLowerCase('fr'))) {
      this.error.set('Sélectionne le client existant dans la liste pour réutiliser sa fiche.');
      return;
    }
    this.startingSession.set(true);
    this.api.start(customer ? { customerId: customer.customerId } : { customerName }).subscribe({
      next: () => {
        this.startingSession.set(false);
        this.clearCustomerSelection();
        this.notice.set('');
        this.error.set('');
        this.reload();
      },
      error: (error: HttpErrorResponse) => {
        this.startingSession.set(false);
        this.error.set(error.error?.message ?? 'Démarrage impossible.');
      },
    });
  }

  openPaymentDialog(session: CafeSession): void {
    const suggestedAmount = Number(Number(session.totalAmountDue ?? 0).toFixed(2));
    this.payTargetSession.set(session);
    this.paymentOfferIds.set([]);
    this.paymentResolutionForm.reset({ offerToAddId: '', createSubscriptionDebt: false, amountPaid: suggestedAmount });
    this.loadCustomerDetails(session.customerId);
    this.error.set('');
    this.notice.set('');
  }

  closePaymentDialog(): void {
    this.payTargetSession.set(null);
    this.paymentOfferIds.set([]);
    this.paymentResolutionForm.reset({ offerToAddId: '', createSubscriptionDebt: false, amountPaid: 0 });
  }

  openCustomerModal(customerId: string): void {
    this.customerModalId.set(customerId);
    this.customerPurchasesVisibleCount.set(10);
    this.loadCustomerDetails(customerId);
    this.error.set('');
  }

  closeCustomerModal(): void {
    this.customerModalId.set(null);
    this.customerPurchasesVisibleCount.set(10);
  }

  stopSession(session: CafeSession): void {
    this.api.stop(session.sessionId).subscribe({
      next: (stoppedSession) => {
        this.notice.set(`Session arretee. Montant a payer : ${Number(stoppedSession.totalAmountDue ?? 0).toFixed(2)} EUR.`);
        this.current.update((sessions) => sessions.map((item) => item.sessionId === stoppedSession.sessionId ? stoppedSession : item));
        this.sessionObservedAt.update((observedAt) => ({ ...observedAt, [stoppedSession.sessionId]: Date.now() }));
        this.refreshCustomerDetails(session.customerId);
        this.reload();
      },
      error: (error: HttpErrorResponse) => this.error.set(this.resolveHttpError(error, 'Arret impossible')),
    });
  }

  confirmPayment(): void {
    this.submitPayment(false);
  }

  confirmPaymentWithInvoice(): void {
    this.submitPayment(true);
  }

  private submitPayment(withInvoice: boolean): void {
    const session = this.activePayTargetSession();
    if (!session) {
      return;
    }
    if (this.paymentResolutionForm.invalid) {
      this.paymentResolutionForm.markAllAsTouched();
      return;
    }
    const selectedOffers = this.paymentSelectedOffers();
    const createSubscriptionDebt = this.paymentResolutionForm.controls.createSubscriptionDebt.value;
    const amountPaid = Number(this.paymentResolutionForm.controls.amountPaid.value ?? 0);
    const requiredAmount = this.paymentDialogTotalAmount(session);

    if (amountPaid + 0.001 < requiredAmount) {
      this.error.set(`Le montant regle ne couvre pas le total a payer (${requiredAmount.toFixed(2)} EUR).`);
      return;
    }

    const payload = {
      amountPaid,
      subscriptionOfferIds: selectedOffers.map((offer) => offer.offerId),
      createSubscriptionDebt,
    };

    if (withInvoice) {
      this.api.payWithInvoice(session.sessionId, payload).subscribe({
        next: (response: HttpResponse<Blob>) => {
          this.downloadPdf(response);
          this.onPaymentSuccess(session, selectedOffers, createSubscriptionDebt, true);
        },
        error: (error: HttpErrorResponse) => this.error.set(this.resolveHttpError(error, 'Paiement impossible')),
      });
      return;
    }

    this.api.pay(session.sessionId, payload).subscribe({
      next: () => {
        this.onPaymentSuccess(session, selectedOffers, createSubscriptionDebt, false);
      },
      error: (error: HttpErrorResponse) => this.error.set(this.resolveHttpError(error, 'Paiement impossible')),
    });
  }

  togglePause(session: CafeSession): void {
    const request = session.paused ? this.api.resume(session.sessionId) : this.api.pause(session.sessionId);
    request.subscribe({
      next: (updatedSession) => {
        this.notice.set(updatedSession.paused ? 'Session mise en pause.' : 'Session reprise.');
        this.current.update((sessions) => sessions.map((item) => item.sessionId === updatedSession.sessionId ? updatedSession : item));
        this.sessionObservedAt.update((observedAt) => ({ ...observedAt, [updatedSession.sessionId]: Date.now() }));
      },
      error: (error: HttpErrorResponse) => this.error.set(this.resolveHttpError(error, session.paused ? 'Reprise impossible' : 'Pause impossible')),
    });
  }

  openProductSaleModal(session: CafeSession): void {
    this.saleTargetSession.set(session);
    this.saleModalType.set('product');
    this.productSaleForm.reset({ productId: '', quantity: 1, createDebt: false });
    this.error.set('');
    this.notice.set('');
  }

  openSubscriptionSaleModal(session: CafeSession): void {
    this.saleTargetSession.set(session);
    this.saleModalType.set('subscription');
    this.subscriptionSaleForm.reset({ subscriptionOfferId: '', createDebt: false });
    this.error.set('');
    this.notice.set('');
  }

  closeSaleModal(): void {
    this.saleTargetSession.set(null);
    this.saleModalType.set(null);
    this.productSaleForm.reset({ productId: '', quantity: 1, createDebt: false });
    this.subscriptionSaleForm.reset({ subscriptionOfferId: '', createDebt: false });
  }

  sellProductForSession(): void {
    const session = this.saleTargetSession();
    if (!session || this.productSaleForm.invalid) {
      this.productSaleForm.markAllAsTouched();
      return;
    }
    const { productId, quantity, createDebt } = this.productSaleForm.getRawValue();
    this.salesApi.productSale({ customerId: session.customerId, sessionId: session.sessionId, lines: [{ productId, quantity }], createDebt }).subscribe({
      next: () => {
        this.refreshCustomerDetails(session.customerId);
        this.closeSaleModal();
        this.reload();
      },
      error: (error: HttpErrorResponse) => this.error.set(this.resolveHttpError(error, 'Vente produit impossible')),
    });
  }

  sellSubscriptionForSession(): void {
    const session = this.saleTargetSession();
    if (!session || this.subscriptionSaleForm.invalid) {
      this.subscriptionSaleForm.markAllAsTouched();
      return;
    }
    const { subscriptionOfferId, createDebt } = this.subscriptionSaleForm.getRawValue();
    this.salesApi.subscriptionSale({ customerId: session.customerId, sessionId: session.sessionId, subscriptionOfferId, createDebt }).subscribe({
      next: () => {
        this.refreshCustomerDetails(session.customerId);
        this.closeSaleModal();
        this.reload();
      },
      error: (error: HttpErrorResponse) => this.error.set(this.resolveHttpError(error, 'Vente abonnement impossible')),
    });
  }

  onSessionRowInteraction(session: CafeSession, event: Event): void {
    const target = event.target;
    if (!(target instanceof Element) || target.closest('button, a, input, select, textarea, label, summary, [role="button"]')) {
      return;
    }
    if (event instanceof KeyboardEvent) event.preventDefault();
    this.toggleDetails(session);
  }

  toggleDetails(session: CafeSession): void {
    const expanded = new Set(this.expandedSessionIds());
    if (expanded.has(session.sessionId)) {
      expanded.delete(session.sessionId);
      this.clearSeparateEntries(session.sessionId);
    } else {
      expanded.add(session.sessionId);
      this.loadCustomerDetails(session.customerId);
    }
    this.expandedSessionIds.set(expanded);
  }

  toggleDayDetails(session: CafeSession): void {
    if (this.expandedDaySessionId() === session.sessionId) {
      this.expandedDaySessionId.set(null);
      return;
    }
    this.expandedDaySessionId.set(session.sessionId);
    this.loadCustomerDetails(session.customerId);
  }

  changeDayPage(step: number): void {
    this.dayPage.set(Math.max(1, this.dayPage() + step));
  }

  todayPurchases(purchases: CustomerPurchase[]): CustomerPurchase[] {
    const today = new Date().toDateString();
    return purchases.filter((purchase) => new Date(purchase.soldAt).toDateString() === today);
  }

  sessionEntries(session: CafeSession, details: CustomerDetails): SessionDetailEntry[] {
    return this.entryService.entries(session, details);
  }

  sessionRows(session: CafeSession, details: CustomerDetails): SessionDetailRow[] {
    return this.entryService.rows(session, details, this.isEntryGrouping(session));
  }

  isEntryGrouping(session: CafeSession): boolean {
    return this.groupingPreference()?.groupEntries === true && !this.separateEntrySessionIds().has(session.sessionId);
  }

  private loadGroupingPreference(): void {
    this.api.displayPreference().subscribe({
      next: (preference) => {
        this.groupingPreference.set(preference);
        this.loadingGroupingPreference.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.loadingGroupingPreference.set(false);
        this.error.set(this.resolveHttpError(error, 'Chargement du choix de fusion impossible.'));
      },
    });
  }

  toggleEntryGrouping(session: CafeSession): void {
    if (this.loadingGroupingPreference() || this.savingGroupingPreference()) return;
    const groupEntries = !this.isEntryGrouping(session);
    if (this.groupingPreference()?.groupEntries === groupEntries) {
      this.clearSeparateEntries(session.sessionId);
      return;
    }
    this.savingGroupingPreference.set(true);
    this.error.set('');
    this.notice.set('');
    this.api.configureDisplayPreference(groupEntries).subscribe({
      next: (preference) => {
        this.groupingPreference.set(preference);
        this.separateEntrySessionIds.set(new Set());
        this.savingGroupingPreference.set(false);
        this.notice.set(preference.groupEntries ? 'Affichage fusionné enregistré.' : 'Affichage séparé enregistré.');
      },
      error: (error: HttpErrorResponse) => {
        this.savingGroupingPreference.set(false);
        this.error.set(this.resolveHttpError(error, 'Enregistrement du choix de fusion impossible.'));
      },
    });
  }

  showSeparateEntries(session: CafeSession): void {
    this.separateEntrySessionIds.update((ids) => new Set([...ids, session.sessionId]));
  }

  private clearSeparateEntries(sessionId: string): void {
    this.separateEntrySessionIds.update((ids) => {
      const next = new Set(ids);
      next.delete(sessionId);
      return next;
    });
  }

  entryLabel(entry: SessionDetailEntry): string {
    return entry.lines.length
      ? entry.lines.map((line) => `${line.quantity} x ${line.label}`).join(', ')
      : this.formatEntryLabel(entry.label);
  }

  trackEntry(_index: number, entry: SessionDetailEntry): string {
    return entry.id;
  }

  formatEntryLabel(label: string): string {
    const match = label.match(/^(.*\sdu\s)(\d{4}-\d{2}-\d{2}T\d{2}:\d{2})(?::\d{2}(?:\.\d+)?)?$/);
    if (!match) {
      return label;
    }

    const parsed = new Date(match[2]);
    if (Number.isNaN(parsed.getTime())) {
      return label;
    }

    const day = String(parsed.getDate()).padStart(2, '0');
    const month = String(parsed.getMonth() + 1).padStart(2, '0');
    const year = parsed.getFullYear();
    const hours = String(parsed.getHours()).padStart(2, '0');
    const minutes = String(parsed.getMinutes()).padStart(2, '0');
    return `${match[1]}${day}/${month}/${year} ${hours}:${minutes}`;
  }

  formatElapsed(session: CafeSession): string {
    return this.formatSeconds(this.liveConsumedSeconds(session));
  }

  formatRemaining(session: CafeSession): string {
    return this.formatSeconds(this.liveDisplayRemainingSeconds(session));
  }

  sessionState(session: CafeSession): SessionState {
    return this.sessionTable.stateOf(session);
  }

  customerTypeLabel(type: string): string {
    return type === 'SUBSCRIBER' || type.toLowerCase().includes('abonn') ? 'Abonne' : 'Client';
  }

  customerTypeChipClass(type: string): string {
    return this.customerTypeLabel(type) === 'Abonne' ? 'type-chip subscriber-chip' : 'type-chip walk-in-chip';
  }

  canAddSubscriptionDuringPayment(): boolean {
    return this.offers().length > 0;
  }

  totalPaidPurchases(customer: CustomerDetails): number {
    return customer.purchases
      .filter((purchase) => !purchase.openDebt)
      .reduce((total, purchase) => total + purchase.totalAmount, 0);
  }

  totalPaidSales(customer: CustomerDetails): number {
    return customer.purchases
      .filter((purchase) => !purchase.openDebt)
      .filter((purchase) => purchase.type === 'SUBSCRIPTION' || purchase.type === 'PRODUCTS')
      .reduce((total, purchase) => total + purchase.totalAmount, 0);
  }

  customerPurchaseEntries(customer: CustomerDetails): Array<{ occurredAt: string; type: string; detail: string; total: number; isDebt: boolean }> {
    const uniquePurchases = Array.from(new Map(customer.purchases.map((purchase) => [purchase.saleId, purchase] as const)).values());
    const paidPurchases = uniquePurchases
      .filter((purchase) => !purchase.openDebt)
      .map((purchase) => ({
        occurredAt: purchase.soldAt,
        type: this.purchaseTypeLabel(purchase.type),
        detail: purchase.label,
        total: purchase.totalAmount,
        isDebt: false,
      }));
    const openDebts = customer.debts
      .filter((debt) => debt.status === 'OPEN')
      .map((debt) => ({
        occurredAt: debt.createdAt,
        type: this.debtTypeLabel(debt.label),
        detail: debt.label,
        total: debt.amount,
        isDebt: true,
      }));

    return [...paidPurchases, ...openDebts]
      .sort((left, right) => new Date(right.occurredAt).getTime() - new Date(left.occurredAt).getTime());
  }

  visibleCustomerPurchaseEntries(customer: CustomerDetails): Array<{ occurredAt: string; type: string; detail: string; total: number; isDebt: boolean }> {
    return this.customerPurchaseEntries(customer).slice(0, this.customerPurchasesVisibleCount());
  }

  showMoreCustomerPurchases(): void {
    this.customerPurchasesVisibleCount.update((count) => count + 10);
  }

  showLessCustomerPurchases(): void {
    this.customerPurchasesVisibleCount.set(10);
  }

  showMoreCustomerPurchasesButton(customer: CustomerDetails, index: number): boolean {
    const visibleCount = this.visibleCustomerPurchaseEntries(customer).length;
    return index === visibleCount - 1 && visibleCount < this.customerPurchaseEntries(customer).length;
  }

  showLessCustomerPurchasesButton(customer: CustomerDetails, index: number): boolean {
    const visibleCount = this.visibleCustomerPurchaseEntries(customer).length;
    return index === visibleCount - 1 && this.customerPurchasesVisibleCount() > 10;
  }

  hasCurrentSession(customerId: string): boolean {
    return this.current().some((session) => session.customerId === customerId && (!session.endedAt || !session.paid));
  }

  canPauseOrResume(session: CafeSession): boolean {
    return !session.endedAt;
  }

  canStop(session: CafeSession): boolean {
    return !session.endedAt;
  }

  canPay(session: CafeSession): boolean {
    return !!session.endedAt && !session.paid;
  }

  isAwaitingPayment(session: CafeSession): boolean {
    return this.sessionState(session) === 'A payer';
  }

  hasOpenDebt(session: CafeSession): boolean {
    return Number(session.openDebtAmount ?? 0) > 0.001;
  }

  addPaymentOffer(): void {
    const offerId = this.paymentResolutionForm.controls.offerToAddId.value;
    if (!offerId) {
      this.error.set('Choisis un abonnement a ajouter.');
      return;
    }
    this.paymentOfferIds.update((offerIds) => [...offerIds, offerId]);
    this.paymentResolutionForm.patchValue({ offerToAddId: '' });
    this.syncPaymentDialogAmount();
    this.error.set('');
  }

  removePaymentOffer(index: number): void {
    this.paymentOfferIds.update((offerIds) => offerIds.filter((_, currentIndex) => currentIndex !== index));
    this.syncPaymentDialogAmount();
  }

  paymentDialogPurchaseAmount(session: CafeSession): number {
    return Math.max(0, Number(session.totalAmountDue ?? 0) - this.paymentDialogBaseConnectionAmount(session));
  }

  paymentDialogOpenDebtAmount(session: CafeSession): number {
    return Math.max(0, Number(session.openDebtAmount ?? 0));
  }

  paymentSelectedOffers(): SubscriptionOffer[] {
    return this.paymentOfferIds()
      .map((offerId) => this.offers().find((offer) => offer.offerId === offerId) ?? null)
      .filter((offer): offer is SubscriptionOffer => offer != null);
  }

  paymentDialogRawOvertimeMinutes(session: CafeSession): number {
    return Math.max(0, Number(session.consumedMinutes ?? 0) - Number(session.remainingMinutes ?? 0));
  }

  paymentDialogOvertimeMinutes(session: CafeSession): number {
    const selectedOfferMinutes = this.paymentSelectedOffers().reduce((total, offer) => total + offer.includedMinutes, 0);
    return Math.max(0, Number(session.consumedMinutes ?? 0) - (Number(session.remainingMinutes ?? 0) + selectedOfferMinutes));
  }

  paymentDialogConnectionAmount(session: CafeSession): number {
    if (this.paymentSelectedOffers().length === 0 && this.customerTypeLabel(session.customerType) !== 'Abonne') {
      return this.paymentDialogBaseConnectionAmount(session);
    }
    return this.calculateConnectionPrice(this.paymentDialogOvertimeMinutes(session));
  }

  paymentDialogSubscriptionImmediateAmount(): number {
    const selectedOffers = this.paymentSelectedOffers();
    if (selectedOffers.length === 0 || this.paymentResolutionForm.controls.createSubscriptionDebt.value) {
      return 0;
    }
    return selectedOffers.reduce((total, offer) => total + Number(offer.price ?? 0), 0);
  }

  paymentDialogTotalAmount(session: CafeSession): number {
    return this.paymentDialogConnectionAmount(session) + this.paymentDialogPurchaseAmount(session) + this.paymentDialogSubscriptionImmediateAmount();
  }

  private paymentDialogBaseConnectionAmount(session: CafeSession): number {
    return Number(session.calculatedPrice ?? 0);
  }

  private syncPaymentDialogAmount(): void {
    const session = this.activePayTargetSession();
    if (!session) {
      return;
    }
    const nextAmount = Number(this.paymentDialogTotalAmount(session).toFixed(2));
    this.paymentResolutionForm.controls.amountPaid.setValue(nextAmount);
  }

  private downloadPdf(response: HttpResponse<Blob>): void {
    if (!response.body) {
      return;
    }
    const blobUrl = URL.createObjectURL(response.body);
    const link = document.createElement('a');
    link.href = blobUrl;
    link.download = this.api.fileName(response);
    link.click();
    URL.revokeObjectURL(blobUrl);
  }

  private onPaymentSuccess(session: CafeSession, selectedOffers: SubscriptionOffer[], createSubscriptionDebt: boolean, withInvoice: boolean): void {
    const connection = this.paymentDialogConnectionAmount(session);
    const purchases = this.paymentDialogPurchaseAmount(session);
    const subscriptionPaid = !createSubscriptionDebt
      ? selectedOffers.reduce((total, offer) => total + Number(offer.price ?? 0), 0)
      : 0;
    const dayTotal = connection + purchases + subscriptionPaid;
    const subscriptionNotice = createSubscriptionDebt && selectedOffers.length > 0
      ? selectedOffers.length > 1
        ? 'Paiement valide. Nouveaux abonnements ajoutes en dette.'
        : 'Paiement valide. Nouvel abonnement ajoute en dette.'
      : 'Paiement valide.';
    this.notice.set(
      `${subscriptionNotice}${withInvoice ? ' Facture PDF generee.' : ''} Total paye : ${dayTotal.toFixed(2)} EUR.`
    );
    this.closePaymentDialog();
    this.closeSaleModal();
    this.refreshCustomerDetails(session.customerId);
    this.reload();
  }

  toggleEntryDebt(session: CafeSession, entry: SessionDetailEntry, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    const requests = checked
      ? entry.saleId ? [this.customersApi.createDebtFromSale(entry.saleId)] : []
      : entry.debtId ? [this.customersApi.reattachDebtToSession(entry.debtId, session.sessionId)] : [];

    if (requests.length === 0) {
      (event.target as HTMLInputElement).checked = entry.openDebt;
      this.error.set('Action impossible sur cette ligne.');
      this.logger.warn('sessions-ui', 'Debt toggle ignored because no actionable request was produced', {
        label: entry.label,
        saleCount: Number(!!entry.saleId),
        debtCount: Number(!!entry.debtId),
      });
      return;
    }

    this.logger.info('sessions-ui', 'Toggling debt state from session detail', {
      action: checked ? 'create-debt' : 'reattach-to-session',
      sessionId: session.sessionId,
      label: entry.label,
      saleCount: Number(!!entry.saleId),
      debtCount: Number(!!entry.debtId),
    });
    forkJoin(requests).subscribe({
      next: () => {
        this.notice.set(checked ? 'Ligne passee en dette.' : 'Dette retransformee en achat de la session.');
        this.logger.info('sessions-ui', 'Debt state updated from session detail', {
          action: checked ? 'create-debt' : 'reattach-to-session',
          requestCount: requests.length,
        });
        this.refreshAfterDebtChange();
      },
      error: (error: HttpErrorResponse) => {
        (event.target as HTMLInputElement).checked = entry.openDebt;
        this.logger.warn('sessions-ui', 'Debt state update failed from session detail', {
          action: checked ? 'create-debt' : 'reattach-to-session',
          requestCount: requests.length,
          error,
        });
        this.error.set(this.resolveHttpError(error, checked ? 'Creation de dette impossible' : 'Reintegration de la dette impossible'));
      },
    });
  }

  deleteEntry(session: CafeSession, entry: SessionDetailEntry): void {
    if (this.pendingEntryIds().has(entry.id)) return;
    const request = entry.debtId
      ? this.api.deleteDebt(session.sessionId, entry.debtId)
      : entry.saleId ? this.api.deletePurchase(session.sessionId, entry.saleId) : null;
    if (!request) {
      this.error.set('Suppression impossible pour cette ligne.');
      return;
    }
    this.pendingEntryIds.update((ids) => new Set([...ids, entry.id]));
    request.subscribe({
      next: () => {
        this.finishEntryMutation(entry.id);
        this.notice.set('Ligne supprimée.');
        this.refreshCustomerDetails(session.customerId);
        this.reload();
      },
      error: (error: HttpErrorResponse) => {
        this.finishEntryMutation(entry.id);
        this.error.set(this.resolveHttpError(error, 'Suppression de la ligne impossible'));
      },
    });
  }

  private finishEntryMutation(entryId: string): void {
    this.pendingEntryIds.update((ids) => {
      const next = new Set(ids);
      next.delete(entryId);
      return next;
    });
  }

  private purchaseTypeLabel(type: CustomerPurchase['type']): string {
    switch (type) {
      case 'PRODUCTS':
        return 'Produit';
      case 'SUBSCRIPTION':
        return 'Abonnement';
      case 'CONNECTION_TIME':
        return 'Connexion';
      default:
        return type;
    }
  }

  private debtTypeLabel(label: string): string {
    const normalized = label.toLowerCase();
    if (normalized.startsWith('vente produits')) {
      return 'Produit';
    }
    if (normalized.startsWith('vente abonnements') || normalized.startsWith('vente abonnement')) {
      return 'Abonnement';
    }
    if (normalized.startsWith('session du') || normalized.startsWith('depassement abonnement du')) {
      return 'Connexion';
    }
    return 'Dette';
  }

  private liveConsumedSeconds(session: CafeSession): number {
    if (session.endedAt) {
      return Math.max(0, session.consumedSeconds);
    }
    const observedAt = this.sessionObservedAt()[session.sessionId] ?? this.now();
    const extraSeconds = session.paused ? 0 : Math.max(0, Math.floor((this.now() - observedAt) / 1000));
    return Math.max(0, session.consumedSeconds + extraSeconds);
  }

  private liveDisplayRemainingSeconds(session: CafeSession): number {
    const displayRemainingMinutes = Number(session.displayRemainingMinutes ?? session.remainingMinutes ?? 0);
    if (session.endedAt) {
      return Math.max(0, displayRemainingMinutes * 60);
    }
    return Math.max(0, displayRemainingMinutes * 60 - this.liveConsumedSeconds(session));
  }

  private formatSeconds(totalSeconds: number): string {
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;
    return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
  }

  private filterSessions(sessions: CafeSession[], term: string, startedAfter: string): CafeSession[] {
    const normalizedTerm = term.trim().toLowerCase();
    const normalizedDate = startedAfter ? new Date(startedAfter).getTime() : null;
    return sessions
      .filter((session) => {
        const matchesTerm = !normalizedTerm || session.customerName.toLowerCase().includes(normalizedTerm);
        const matchesDate = !normalizedDate || new Date(session.startedAt).getTime() >= normalizedDate;
        return matchesTerm && matchesDate;
      })
      .sort((left, right) => {
        const dateDiff = new Date(right.startedAt).getTime() - new Date(left.startedAt).getTime();
        if (dateDiff !== 0) {
          return dateDiff;
        }
        return left.customerName.localeCompare(right.customerName, 'fr');
      });
  }

  private calculateConnectionPrice(minutes: number): number {
    if (minutes <= 0) {
      return 0;
    }
    const tiers = this.pricingTiers();
    if (tiers.length === 0) {
      return 0;
    }
    const sortedTiers = [...tiers].sort((left, right) => left.durationMinutes - right.durationMinutes);
    const maxDuration = sortedTiers[sortedTiers.length - 1].durationMinutes;
    const limit = minutes + maxDuration;
    const prices: Array<number | null> = Array.from({ length: limit + 1 }, () => null);
    prices[0] = 0;

    for (let coveredMinutes = 1; coveredMinutes <= limit; coveredMinutes += 1) {
      let bestPrice: number | null = null;
      for (const tier of sortedTiers) {
        const previousMinutes = coveredMinutes - tier.durationMinutes;
        if (previousMinutes < 0) {
          continue;
        }
        const previousPrice = prices[previousMinutes];
        if (previousPrice == null) {
          continue;
        }
        const candidate = previousPrice + Number(tier.price);
        if (bestPrice == null || candidate < bestPrice) {
          bestPrice = candidate;
        }
      }
      prices[coveredMinutes] = bestPrice;
    }

    let bestPrice: number | null = null;
    for (let coveredMinutes = minutes; coveredMinutes <= limit; coveredMinutes += 1) {
      const candidate = prices[coveredMinutes];
      if (candidate != null && (bestPrice == null || candidate < bestPrice)) {
        bestPrice = candidate;
      }
    }
    return bestPrice ?? 0;
  }

  private loadCustomerDetails(customerId: string): void {
    if (this.sessionDetails()[customerId]) {
      return;
    }
    this.fetchCustomerDetails(customerId);
  }

  private refreshCustomerDetails(customerId: string): void {
    const nextDetails = { ...this.sessionDetails() };
    delete nextDetails[customerId];
    this.sessionDetails.set(nextDetails);
    this.fetchCustomerDetails(customerId);
  }

  private refreshAfterDebtChange(): void {
    const customerIds = new Set<string>();
    for (const session of this.current()) {
      if (this.expandedSessionIds().has(session.sessionId)) customerIds.add(session.customerId);
    }
    if (this.saleTargetSession()) {
      customerIds.add(this.saleTargetSession()!.customerId);
    }
    customerIds.forEach((customerId) => this.refreshCustomerDetails(customerId));
    this.reload();
  }

  private fetchCustomerDetails(customerId: string): void {
    this.logger.debug('sessions-ui', 'Loading customer detail for session drawer', { customerId });
    this.customerDetailsService.get(customerId).subscribe({
      next: (details) => {
        this.sessionDetails.set({
          ...this.sessionDetails(),
          [customerId]: details,
        });
        const currentSession = this.current().find((item) => item.customerId === customerId && this.expandedSessionIds().has(item.sessionId)) ?? null;
        const entries = currentSession ? this.sessionEntries(currentSession, details) : [];
        this.logger.debug('sessions-ui', 'Session detail normalized for session entries', {
          customerId,
          sessionId: currentSession?.sessionId,
          purchaseCount: details.purchases.length,
          debtCount: details.debts.length,
          normalizedEntryCount: entries.length,
          openDebtCount: entries.filter((entry) => entry.openDebt).length,
        });
      },
      error: (error: HttpErrorResponse) => {
        this.logger.warn('sessions-ui', 'Customer detail loading failed for session drawer', { customerId, error });
        this.error.set(this.resolveHttpError(error, 'Chargement du detail client impossible'));
      },
    });
  }

  private resolveHttpError(error: HttpErrorResponse, fallback: string): string {
    if (typeof error.error === 'string' && error.error.trim()) {
      return error.error;
    }
    if (error.error?.message) {
      return error.error.message;
    }
    if (error.status === 0) {
      return 'Serveur inaccessible ou non redemarre.';
    }
    if (error.status === 401) {
      return 'Session expiree. Reconnecte-toi.';
    }
    if (error.status === 403) {
      return 'Action reservee a un administrateur.';
    }
    if (error.status === 404) {
      return 'Endpoint introuvable. Redemarre probablement le backend.';
    }
    return fallback;
  }
}
