import { Component, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TokenService } from '../../services/token.service';

interface ModuleCard {
  label: string;
  description: string;
  route: string;
  allowedRoles: string[];
  icon: string;
}

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent {
  tokens = inject(TokenService);
  user = this.tokens.user;

  private allCards: ModuleCard[] = [
    { label: 'Assets', description: 'Browse and manage generating assets.',
      route: '/app/assets',
      allowedRoles: ['ADMIN', 'OPERATOR', 'PRODUCER', 'AUDITOR'],
      icon: 'M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5' },
    { label: 'Schedules', description: 'Plan and review generation schedules.',
      route: '/app/schedules',
      allowedRoles: ['ADMIN', 'OPERATOR', 'TECHNICIAN', 'PRODUCER', 'AUDITOR'],
      icon: 'M8 2v4M16 2v4M3 9h18M5 5h14a2 2 0 0 1 2 2v13a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2z' },
    { label: 'Dispatch', description: 'Execute and audit dispatch records.',
      route: '/app/dispatch',
      allowedRoles: ['ADMIN', 'OPERATOR', 'AUDITOR'],
      icon: 'M22 12h-4l-3 9L9 3l-3 9H2' },
    { label: 'Demand Response', description: 'Run DR programs, events, and verify reductions.',
      route: '/app/demand-response',
      allowedRoles: ['ADMIN', 'OPERATOR', 'PRODUCER', 'CUSTOMER', 'AUDITOR'],
      icon: 'M13 2L3 14h9l-1 8 10-12h-9l1-8z' },
    { label: 'Outages', description: 'Report outages and track resolution.',
      route: '/app/outages',
      allowedRoles: ['ADMIN', 'OPERATOR', 'TECHNICIAN', 'AUDITOR'],
      icon: 'M12 9v4M12 17h.01M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z' },
    { label: 'Incident Tasks', description: 'Triage outage tasks with evidence.',
      route: '/app/incident-tasks',
      allowedRoles: ['ADMIN', 'OPERATOR', 'TECHNICIAN', 'AUDITOR'],
      icon: 'M9 11l3 3 8-8 M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11' },
    { label: 'Work Orders', description: 'Create and assign field work orders.',
      route: '/app/work-orders',
      allowedRoles: ['ADMIN', 'OPERATOR', 'TECHNICIAN', 'AUDITOR'],
      icon: 'M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z' },
    { label: 'Billing', description: 'View invoices and record payments.',
      route: '/app/billing',
      allowedRoles: ['ADMIN', 'AUDITOR', 'CUSTOMER'],
      icon: 'M14 2H6a2 2 0 0 0-2 2v16l4-2 4 2 4-2 4 2V8z M14 2v6h6' },
    { label: 'Reports', description: 'Export CSV reports across scopes.',
      route: '/app/reports',
      allowedRoles: ['ADMIN', 'OPERATOR', 'AUDITOR'],
      icon: 'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z M14 2v6h6 M16 13H8 M16 17H8' },
    { label: 'Notifications', description: 'See system alerts and updates.',
      route: '/app/notifications',
      allowedRoles: ['ADMIN', 'OPERATOR', 'TECHNICIAN', 'PRODUCER', 'CUSTOMER', 'AUDITOR'],
      icon: 'M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9 M13.73 21a2 2 0 0 1-3.46 0' },
    { label: 'Users', description: 'Approve registrations and manage access.',
      route: '/app/users',
      allowedRoles: ['ADMIN'],
      icon: 'M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2 M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z M23 21v-2a4 4 0 0 0-3-3.87 M16 3.13a4 4 0 0 1 0 7.75' },
  ];

  cards = computed<ModuleCard[]>(() => {
    const role = this.user()?.role;
    if (!role) return [];
    return this.allCards.filter((card) => card.allowedRoles.includes(role));
  });
}
