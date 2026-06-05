import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Observable } from 'rxjs';
import { AssetService } from '../../services/asset.service';
import { SchedulingService } from '../../services/scheduling.service';
import { OutageService } from '../../services/outage.service';
import { WorkOrderService } from '../../services/workorder.service';
import { BillingService } from '../../services/billing.service';
import { extractErrorMessage } from '../../utils/error-message';

type Scope = 'assets' | 'schedules' | 'dispatch' | 'outages' | 'work-orders' | 'invoices';

@Component({
  selector: 'app-reports',
  imports: [CommonModule, FormsModule],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.css',
})
export class ReportsComponent {
  private assets = inject(AssetService);
  private sched = inject(SchedulingService);
  private outage = inject(OutageService);
  private workorder = inject(WorkOrderService);
  private billing = inject(BillingService);

  scope: Scope = 'assets';
  from = '';
  to = '';

  downloading = signal(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);

  download(): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);

    if (this.from && this.to && this.from > this.to) {
      this.errorMessage.set('"From" date must be before "To" date.');
      return;
    }

    this.downloading.set(true);
    this.fetch(this.scope).subscribe({
      next: (rows) => {
        const filtered = this.filterByDate(rows, this.scope, this.from, this.to);
        const csv = this.toCsv(filtered);
        this.triggerDownload(csv, this.scope + '-' + this.timestamp() + '.csv');
        this.successMessage.set('Exported ' + filtered.length + ' row(s).');
        this.downloading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(extractErrorMessage(err, 'Failed to fetch data.'));
        this.downloading.set(false);
      },
    });
  }

  private fetch(scope: Scope): Observable<any[]> {
    if (scope === 'assets') return this.assets.list() as any;
    if (scope === 'schedules') return this.sched.listSchedules() as any;
    if (scope === 'dispatch') return this.sched.listDispatches() as any;
    if (scope === 'outages') return this.outage.listOutages() as any;
    if (scope === 'work-orders') return this.workorder.list() as any;
    return this.billing.listInvoices() as any;
  }

  private dateFieldFor(scope: Scope): string {
    if (scope === 'assets') return 'CommissionedAt';
    if (scope === 'schedules') return 'startAt';
    if (scope === 'dispatch') return 'executedAt';
    if (scope === 'outages') return 'reportedAt';
    return 'createdAt';
  }

  private filterByDate(rows: any[], scope: Scope, from: string, to: string): any[] {
    if (!from && !to) return rows;
    const field = this.dateFieldFor(scope);
    const fromMs = from ? Date.parse(from + 'T00:00:00') : null;
    const toMs = to ? Date.parse(to + 'T23:59:59') : null;
    return rows.filter((r) => {
      const raw = r[field];
      if (typeof raw !== 'string' || !raw) return true;
      const ms = Date.parse(raw);
      if (!isFinite(ms)) return true;
      if (fromMs !== null && ms < fromMs) return false;
      if (toMs !== null && ms > toMs) return false;
      return true;
    });
  }

  private toCsv(rows: any[]): string {
    if (rows.length === 0) return '';
    const headers = this.collectHeaders(rows);
    const lines = [headers.join(',')];
    for (const row of rows) {
      lines.push(headers.map((header) => this.csvCell(row[header])).join(','));
    }
    return lines.join('\r\n');
  }

  private collectHeaders(rows: any[]): string[] {
    const headerSet = new Set<string>();
    for (const row of rows) {
      if (row && typeof row === 'object') {
        for (const key of Object.keys(row)) headerSet.add(key);
      }
    }
    return Array.from(headerSet);
  }

  private csvCell(value: any): string {
    if (value == null) return '';
    const text = typeof value === 'object' ? JSON.stringify(value) : String(value);
    const needsQuotes = /[,"\n\r]/.test(text);
    if (!needsQuotes) return text;
    return '"' + text.replace(/"/g, '""') + '"';
  }

  private triggerDownload(csv: string, filename: string): void {
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }

  private timestamp(): string {
    const now = new Date();
    const pad = (n: number) => String(n).padStart(2, '0');
    return now.getFullYear() + pad(now.getMonth() + 1) + pad(now.getDate())
      + '-' + pad(now.getHours()) + pad(now.getMinutes());
  }
}
