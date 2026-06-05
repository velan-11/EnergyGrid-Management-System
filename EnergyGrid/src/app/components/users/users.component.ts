import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../services/admin.service';
import { AdminUser } from '../../models/auth.models';
import { extractErrorMessage } from '../../utils/error-message';

interface UserRow extends AdminUser {
  rowMessage?: { text: string; tone: 'success' | 'error' };
  busy?: boolean;
}

@Component({
  selector: 'app-users',
  imports: [CommonModule, FormsModule],
  templateUrl: './users.component.html',
  styleUrl: './users.component.css',
})
export class UsersComponent implements OnInit {
  private adminService = inject(AdminService);

  rows = signal<UserRow[]>([]);
  loading = signal(true);
  loadError = signal<string | null>(null);

  searchId = '';
  searchedUser = signal<UserRow | null>(null);
  searchError = signal<string | null>(null);
  searching = signal(false);

  totalCount = computed(() => this.rows().length);
  pendingCount = computed(() =>
    this.rows().filter((u) => this.isPending(u)).length,
  );

  ngOnInit(): void {
    this.loadUsers();
  }

  private loadUsers(): void {
    this.loading.set(true);
    this.loadError.set(null);
    this.adminService.list().subscribe({
      next: (users) => {
        this.rows.set((users || []).map((u) => ({ ...u })));
        this.loading.set(false);
      },
      error: (err) => {
        this.loadError.set(extractErrorMessage(err, 'Failed to load users.'));
        this.loading.set(false);
      },
    });
  }

  searchById(): void {
    this.searchError.set(null);
    this.searchedUser.set(null);

    const idNumber = Number(this.searchId);
    if (!this.searchId || isNaN(idNumber) || idNumber <= 0) {
      this.searchError.set('Please enter a valid User ID (a positive number).');
      return;
    }

    this.searching.set(true);
    this.adminService.get(idNumber).subscribe({
      next: (user) => {
        this.searchedUser.set({ ...user });
        this.searching.set(false);
      },
      error: (err) => {
        this.searchError.set(extractErrorMessage(err, 'User not found.'));
        this.searching.set(false);
      },
    });
  }

  clearSearch(): void {
    this.searchId = '';
    this.searchedUser.set(null);
    this.searchError.set(null);
  }

  isPending(user: UserRow): boolean {
    return this.statusOf(user) === 'PENDING' && !user.deleted;
  }

  isActive(user: UserRow): boolean {
    return this.statusOf(user) === 'ACTIVE' && !user.deleted;
  }

  isCustomer(user: UserRow): boolean {
    return (user.role || '').toUpperCase() === 'CUSTOMER';
  }

  private statusOf(user: UserRow): string {
    return (user.status || '').toUpperCase();
  }

  pillClass(user: UserRow): string {
    if (user.deleted) return 'pill deleted';
    const status = this.statusOf(user);
    if (status === 'ACTIVE') return 'pill active';
    if (status === 'PENDING') return 'pill pending';
    if (status === 'INACTIVE') return 'pill inactive';
    return 'pill muted-pill';
  }

  pillLabel(user: UserRow): string {
    if (user.deleted) return 'deleted';
    return (user.status || '').toLowerCase() || 'unknown';
  }

  formatRegistered(user: UserRow): string {
    const date = this.parseRegisteredDate(user);
    if (!date || isNaN(date.getTime())) return '—';
    const datePart = date.toLocaleDateString('en-CA');
    const timePart = date.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' });
    return datePart + '  ' + timePart;
  }

  private parseRegisteredDate(user: UserRow): Date | null {
    const raw = user.createdAt;
    if (!raw) return null;
    const date = new Date(raw);
    return isNaN(date.getTime()) ? null : date;
  }

  approve(user: UserRow): void {
    this.runAction(user, this.adminService.approve(user.userId), 'User approved', (row) => {
      row.status = 'ACTIVE';
      row.deleted = false;
    });
  }

  reject(user: UserRow): void {
    this.runAction(user, this.adminService.softDelete(user.userId), 'User rejected', (row) => {
      row.status = 'INACTIVE';
      row.deleted = true;
    });
  }

  deactivate(user: UserRow): void {
    this.runAction(user, this.adminService.softDelete(user.userId), 'User deactivated', (row) => {
      row.status = 'INACTIVE';
      row.deleted = true;
    });
  }

  restore(user: UserRow): void {
    this.runAction(user, this.adminService.restore(user.userId), 'User restored', (row) => {
      row.deleted = false;
      row.status = 'ACTIVE';
    });
  }

  private runAction(
    user: UserRow,
    apiCall: any,
    successMessage: string,
    applyChange: (row: UserRow) => void,
  ): void {
    if (user.busy) return;

    this.markRowBusy(user.userId);

    apiCall.subscribe({
      next: () => {
        this.applySuccess(user.userId, successMessage, applyChange);
        setTimeout(() => this.clearRowMessage(user.userId), 3000);
      },
      error: (err: any) => {
        const text = extractErrorMessage(err, 'Action failed.');
        this.applyError(user.userId, text);
        setTimeout(() => this.clearRowMessage(user.userId), 5000);
      },
    });
  }

  private markRowBusy(userId: number): void {
    this.rows.update((list) =>
      list.map((row) =>
        row.userId === userId ? { ...row, busy: true, rowMessage: undefined } : row,
      ),
    );
    const searched = this.searchedUser();
    if (searched && searched.userId === userId) {
      this.searchedUser.set({ ...searched, busy: true, rowMessage: undefined });
    }
  }

  private applySuccess(
    userId: number,
    message: string,
    applyChange: (row: UserRow) => void,
  ): void {
    this.rows.update((list) =>
      list.map((row) => {
        if (row.userId !== userId) return row;
        const next = { ...row };
        applyChange(next);
        next.busy = false;
        next.rowMessage = { text: '✓ ' + message, tone: 'success' };
        return next;
      }),
    );
    const searched = this.searchedUser();
    if (searched && searched.userId === userId) {
      const next = { ...searched };
      applyChange(next);
      next.busy = false;
      next.rowMessage = { text: '✓ ' + message, tone: 'success' };
      this.searchedUser.set(next);
    }
  }

  private applyError(userId: number, text: string): void {
    this.rows.update((list) =>
      list.map((row) =>
        row.userId === userId
          ? { ...row, busy: false, rowMessage: { text, tone: 'error' } }
          : row,
      ),
    );
    const searched = this.searchedUser();
    if (searched && searched.userId === userId) {
      this.searchedUser.set({ ...searched, busy: false, rowMessage: { text, tone: 'error' } });
    }
  }

  private clearRowMessage(userId: number): void {
    this.rows.update((list) =>
      list.map((row) =>
        row.userId === userId ? { ...row, rowMessage: undefined } : row,
      ),
    );
    const searched = this.searchedUser();
    if (searched && searched.userId === userId) {
      this.searchedUser.set({ ...searched, rowMessage: undefined });
    }
  }
}
