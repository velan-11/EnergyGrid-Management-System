import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { roleGuard } from './guards/role.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },

  {
    path: 'login',
    loadComponent: () => import('./components/auth/login/login.component').then(m => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () => import('./components/auth/register/register.component').then(m => m.RegisterComponent),
  },
  { path: 'forget-password', redirectTo: 'login' },
  { path: 'reset-password', redirectTo: 'login' },

  {
    path: 'app',
    canActivate: [authGuard],
    canActivateChild: [authGuard],
    loadComponent: () => import('./components/layout/shell.component').then(m => m.ShellComponent),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },

      {
        path: 'dashboard',
        loadComponent: () => import('./components/dashboard/dashboard.component').then(m => m.DashboardComponent),
      },
      {
        path: 'assets',
        canActivate: [roleGuard(['ADMIN', 'OPERATOR', 'PRODUCER', 'AUDITOR'])],
        loadComponent: () => import('./components/assets/assets.component').then(m => m.AssetsComponent),
      },
      {
        path: 'schedules',
        canActivate: [roleGuard(['ADMIN', 'OPERATOR', 'TECHNICIAN', 'PRODUCER', 'AUDITOR'])],
        loadComponent: () => import('./components/schedules/schedules.component').then(m => m.SchedulesComponent),
      },
      {
        path: 'dispatch',
        canActivate: [roleGuard(['ADMIN', 'OPERATOR', 'AUDITOR'])],
        loadComponent: () => import('./components/dispatch/dispatch.component').then(m => m.DispatchComponent),
      },
      {
        path: 'demand-response',
        canActivate: [roleGuard(['ADMIN', 'OPERATOR', 'PRODUCER', 'CUSTOMER', 'AUDITOR'])],
        loadComponent: () => import('./components/demand-response/demand-response.component').then(m => m.DemandResponseComponent),
      },
      {
        path: 'outages',
        canActivate: [roleGuard(['ADMIN', 'OPERATOR', 'TECHNICIAN', 'AUDITOR'])],
        loadComponent: () => import('./components/outages/outages.component').then(m => m.OutagesComponent),
      },
      {
        path: 'incident-tasks',
        canActivate: [roleGuard(['ADMIN', 'OPERATOR', 'TECHNICIAN', 'AUDITOR'])],
        loadComponent: () => import('./components/incident-tasks/incident-tasks.component').then(m => m.IncidentTasksComponent),
      },
      {
        path: 'work-orders',
        canActivate: [roleGuard(['ADMIN', 'OPERATOR', 'TECHNICIAN', 'AUDITOR'])],
        loadComponent: () => import('./components/work-orders/work-orders.component').then(m => m.WorkOrdersComponent),
      },
      {
        path: 'billing',
        canActivate: [roleGuard(['ADMIN', 'AUDITOR', 'CUSTOMER'])],
        loadComponent: () => import('./components/billing/billing.component').then(m => m.BillingComponent),
      },
      {
        path: 'reports',
        canActivate: [roleGuard(['ADMIN', 'OPERATOR', 'AUDITOR'])],
        loadComponent: () => import('./components/reports/reports.component').then(m => m.ReportsComponent),
      },
      {
        path: 'notifications',
        canActivate: [roleGuard(['ADMIN', 'OPERATOR', 'TECHNICIAN', 'PRODUCER', 'CUSTOMER', 'AUDITOR'])],
        loadComponent: () => import('./components/notifications/notifications.component').then(m => m.NotificationsComponent),
      },
      {
        path: 'users',
        canActivate: [roleGuard(['ADMIN'])],
        loadComponent: () => import('./components/users/users.component').then(m => m.UsersComponent),
      },
      {
        path: 'audit-log',
        canActivate: [roleGuard(['ADMIN', 'AUDITOR'])],
        loadComponent: () => import('./components/audit-logs/audit-log.component').then(m => m.AuditLogComponent),
      },
    ],
  },

  { path: '**', redirectTo: 'app/dashboard' },
];
