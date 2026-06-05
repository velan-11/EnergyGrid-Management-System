import { Component, HostListener, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs/operators';
import { TokenService } from '../../services/token.service';
import { AuthService } from '../../services/auth.service';

interface NavItem {
  label: string;
  route: string;
  allowedRoles?: string[];
  icon: string;
}

@Component({
  selector: 'app-shell',
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.css',
})
export class ShellComponent {
  private auth = inject(AuthService);
  private router = inject(Router);
  tokens = inject(TokenService);
  user = this.tokens.user;

  role = computed<any>(() => this.user()?.role || null);

  dropdownOpen = signal(false);
  sidebarOpen = signal(false);
  pageTitle = signal('Dashboard');

  allNav: NavItem[] = [
    { label: 'Dashboard', route: '/app/dashboard',
      icon: 'M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z' },
    { label: 'Assets', route: '/app/assets',
      allowedRoles: ['ADMIN', 'OPERATOR', 'PRODUCER', 'AUDITOR'],
      icon: 'M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5' },
    { label: 'Schedules', route: '/app/schedules',
      allowedRoles: ['ADMIN', 'OPERATOR', 'TECHNICIAN', 'PRODUCER', 'AUDITOR'],
      icon: 'M8 2v4M16 2v4M3 9h18M5 5h14a2 2 0 0 1 2 2v13a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2z' },
    { label: 'Dispatch', route: '/app/dispatch',
      allowedRoles: ['ADMIN', 'OPERATOR', 'AUDITOR'],
      icon: 'M22 12h-4l-3 9L9 3l-3 9H2' },
    { label: 'Demand Response', route: '/app/demand-response',
      allowedRoles: ['ADMIN', 'OPERATOR', 'PRODUCER', 'CUSTOMER', 'AUDITOR'],
      icon: 'M13 2L3 14h9l-1 8 10-12h-9l1-8z' },
    { label: 'Outages', route: '/app/outages',
      allowedRoles: ['ADMIN', 'OPERATOR', 'TECHNICIAN', 'AUDITOR'],
      icon: 'M12 9v4M12 17h.01M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z' },
    { label: 'Incident Tasks', route: '/app/incident-tasks',
      allowedRoles: ['ADMIN', 'OPERATOR', 'TECHNICIAN', 'AUDITOR'],
      icon: 'M9 11l3 3 8-8 M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11' },
    { label: 'Work Orders', route: '/app/work-orders',
      allowedRoles: ['ADMIN', 'OPERATOR', 'TECHNICIAN', 'AUDITOR'],
      icon: 'M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z' },
    { label: 'Billing', route: '/app/billing',
      allowedRoles: ['ADMIN', 'AUDITOR', 'CUSTOMER'],
      icon: 'M14 2H6a2 2 0 0 0-2 2v16l4-2 4 2 4-2 4 2V8z M14 2v6h6 M12 18v-2 M12 12v-2' },
    { label: 'Reports', route: '/app/reports',
      allowedRoles: ['ADMIN', 'OPERATOR', 'AUDITOR'],
      icon: 'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z M14 2v6h6 M16 13H8 M16 17H8 M10 9H8' },
    { label: 'Notifications', route: '/app/notifications',
      allowedRoles: ['ADMIN', 'OPERATOR', 'TECHNICIAN', 'PRODUCER', 'CUSTOMER', 'AUDITOR'],
      icon: 'M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9 M13.73 21a2 2 0 0 1-3.46 0' },
    { label: 'Users', route: '/app/users',
      allowedRoles: ['ADMIN'],
      icon: 'M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2 M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z M23 21v-2a4 4 0 0 0-3-3.87 M16 3.13a4 4 0 0 1 0 7.75' },
    { label: 'Audit Log', route: '/app/audit-log',
      allowedRoles: ['ADMIN', 'AUDITOR'],
      icon: 'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z M14 2v6h6 M9 13h6 M9 17h6 M9 9h2' },
  ];

  nav = computed<NavItem[]>(() => {
    const userRole = this.role();
    if (!userRole) return [];
    return this.allNav.filter((item) => !item.allowedRoles || item.allowedRoles.includes(userRole));
  });

  initials = computed(() => {
    const name: string = this.user()?.name || '';
    const parts = name.split(/\s+/).filter(Boolean).slice(0, 2);
    if (parts.length === 0) return 'EG';
    return parts.map((p) => p.charAt(0).toUpperCase()).join('');
  });

  constructor() {
    this.router.events.pipe(filter((e) => e instanceof NavigationEnd)).subscribe((e: any) => {
      this.updateTitle(e.urlAfterRedirects || e.url);
      this.closeAllPopovers();
    });
    this.updateTitle(this.router.url);
  }

  private updateTitle(url: string): void {
    const match = this.allNav.find((n) => url.startsWith(n.route));
    if (match) {
      this.pageTitle.set(match.label);
    } else if (url.indexOf('/profile') >= 0) {
      this.pageTitle.set('Profile');
    } else {
      this.pageTitle.set('EnergyGrid');
    }
  }

  private closeAllPopovers(): void {
    this.dropdownOpen.set(false);
    this.sidebarOpen.set(false);
  }

  toggleDropdown(event: Event): void {
    event.stopPropagation();
    this.dropdownOpen.update((open) => !open);
  }

  toggleSidebar(event: Event): void {
    event.stopPropagation();
    this.sidebarOpen.update((open) => !open);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.dropdownOpen() && !this.sidebarOpen()) return;
    const target = event.target as HTMLElement | null;
    if (!target?.closest?.('.user-block')) this.dropdownOpen.set(false);
    if (!target?.closest?.('.sidebar') && !target?.closest?.('.sidebar-toggle')) {
      this.sidebarOpen.set(false);
    }
  }

  goProfile(): void {
    this.dropdownOpen.set(false);
    this.router.navigate(['/app/profile']);
  }

  signOut(): void {
    this.dropdownOpen.set(false);
    this.auth.logout();
  }
}
