import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuditService } from '../../services/audit.service';
import { AuditLogEntry } from '../../models/auth.models';
import { extractErrorMessage } from '../../utils/error-message';

@Component({
  selector: 'app-audit-log',
  imports: [CommonModule, FormsModule],
  templateUrl: './audit-log.component.html',
  styleUrl: './audit-log.component.css',
})
export class AuditLogComponent implements OnInit {
  private auditService = inject(AuditService);

  rows = signal<AuditLogEntry[]>([]);
  loading = signal(true);
  loadError = signal<string | null>(null);

  searchId = '';
  searchedRows = signal<AuditLogEntry[] | null>(null);
  searchError = signal<string | null>(null);
  searching = signal(false);

  ngOnInit(): void {
    this.auditService.listAll().subscribe({
      next: (rows) => {
        this.rows.set(rows);
        this.loading.set(false);
      },
      error: (err) => {
        this.loadError.set(extractErrorMessage(err, 'Failed to load audit log.'));
        this.loading.set(false);
      },
    });
  }

  searchByUserId(): void {
    this.searchError.set(null);
    this.searchedRows.set(null);

    const idNumber = Number(this.searchId);
    if (!this.searchId || isNaN(idNumber) || idNumber <= 0) {
      this.searchError.set('Please enter a valid User ID (a positive number).');
      return;
    }

    this.searching.set(true);
    this.auditService.getByUser(idNumber).subscribe({
      next: (rows) => {
        this.searchedRows.set(rows || []);
        this.searching.set(false);
      },
      error: (err) => {
        this.searchError.set(extractErrorMessage(err, 'No audit logs found for this user.'));
        this.searching.set(false);
      },
    });
  }

  clearSearch(): void {
    this.searchId = '';
    this.searchedRows.set(null);
    this.searchError.set(null);
  }

  actorOf(entry: AuditLogEntry): string {
    if (entry.name?.trim()) return entry.name.trim();
    if (entry.userId) return 'User #' + entry.userId;
    return '—';
  }

  actionLabel(action: string): string {
    const value = (action || '').toUpperCase();
    if (value === 'LOGIN') return 'Login Success';
    if (value === 'LOGIN_FAILED') return 'Login Failed';
    return action;
  }

  formatTime(raw: any): string {
    if (raw == null || raw === '') return '—';
    const date = typeof raw === 'number' ? new Date(raw) : new Date(String(raw));
    if (isNaN(date.getTime())) return '—';
    const datePart = date.toLocaleDateString('en-CA');
    const timePart = date.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' });
    return datePart + ', ' + timePart;
  }

  truncate(text: string, max = 80): string {
    if (!text) return '';
    if (text.length <= max) return text;
    return text.slice(0, max - 1) + '…';
  }
}
