import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { guestGuard } from './core/guards/guest.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/pages/login-page.component').then((module) => module.LoginPageComponent),
    canActivate: [guestGuard],
  },
  {
    path: '',
    loadComponent: () => import('./layout/app-shell.component').then((module) => module.AppShellComponent),
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', loadComponent: () => import('./features/monitoring/pages/monitoring-page.component').then((module) => module.MonitoringPageComponent) },
      { path: 'users', loadComponent: () => import('./features/users/pages/users-page.component').then((module) => module.UsersPageComponent) },
      { path: 'products', loadComponent: () => import('./features/products/pages/products-page.component').then((module) => module.ProductsPageComponent) },
      { path: 'categories', loadComponent: () => import('./features/categories/pages/categories-page.component').then((module) => module.CategoriesPageComponent) },
      { path: 'subscription-offers', loadComponent: () => import('./features/subscriptions/pages/subscription-offers-page.component').then((module) => module.SubscriptionOffersPageComponent) },
      { path: 'customers', loadComponent: () => import('./features/customers/pages/customers-page.component').then((module) => module.CustomersPageComponent) },
      { path: 'sales', loadComponent: () => import('./features/sales/pages/sales-page.component').then((module) => module.SalesPageComponent) },
      { path: 'sales/pricing', loadComponent: () => import('./features/sales/pages/connection-pricing-page.component').then((module) => module.ConnectionPricingPageComponent) },
      { path: 'sessions', loadComponent: () => import('./features/sessions/pages/sessions-page.component').then((module) => module.SessionsPageComponent) },
      { path: 'sessions/settings', loadComponent: () => import('./features/sessions/pages/session-display-settings-page.component').then((module) => module.SessionDisplaySettingsPageComponent) },
      { path: 'debts', loadComponent: () => import('./features/debts/pages/debts-page.component').then((module) => module.DebtsPageComponent) },
      { path: 'archiving', loadComponent: () => import('./features/archiving/pages/archiving-page.component').then((module) => module.ArchivingPageComponent) },
      { path: 'invoices', loadComponent: () => import('./features/invoices/pages/invoices-page.component').then((module) => module.InvoicesPageComponent) },
      { path: 'company', loadComponent: () => import('./features/company/pages/company-page.component').then((module) => module.CompanyPageComponent) },
      {
        path: 'database-backups',
        loadComponent: () => import('./features/database-backups/pages/database-backups-page.component').then((module) => module.DatabaseBackupsPageComponent),
        canActivate: [adminGuard],
      },
      { path: 'monitoring', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'reporting', loadComponent: () => import('./features/reporting/pages/reporting-page.component').then((module) => module.ReportingPageComponent) },
    ],
  },
  { path: '**', redirectTo: '' },
];
