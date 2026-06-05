import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OutageService } from '../../services/outage.service';
import { AssetService } from '../../services/asset.service';
import { TokenService } from '../../services/token.service';
import { Outage, OutageRequest, OutageSeverity, OutageStatus } from '../../models/outage.models';
import { Asset, assetId as assetIdOf, assetNameWithType } from '../../models/asset.models';
import { extractErrorMessage } from '../../utils/error-message';

type FormMode = 'closed' | 'add' | 'edit';

interface OutageRow extends Outage {
  affectedDisplay: string;
}

const RESOLVED_LOCAL_KEY = (id: number) => 'outage_resolved_' + id;
const TERMINAL_STATUSES = ['CLOSED', 'RESOLVED'];

@Component({
  selector: 'app-outages',
  imports: [CommonModule, FormsModule],
  templateUrl: './outages.component.html',
  styleUrl: './outages.component.css',
})
export class OutagesComponent implements OnInit {
  private outageService = inject(OutageService);
  private assetService = inject(AssetService);
  private tokens = inject(TokenService);

  rows = signal<OutageRow[]>([]);
  assets = signal<Asset[]>([]);
  assetById = signal<Map<number, Asset>>(new Map());
  loading = signal(true);
  errorMessage = signal<string | null>(null);

  role = computed(() => this.tokens.user()?.role || '');
  canWrite = computed(() => this.role() === 'ADMIN');

  mode = signal<FormMode>('closed');
  editingId = signal<number | null>(null);
  submitting = signal(false);
  formError = signal<string | null>(null);

  assetId = '';
  severity: OutageSeverity = 'LOW';
  status: OutageStatus = 'OPEN';
  resolvedAtValue: string | null = null;

  aId = assetIdOf;
  aLabel = assetNameWithType;

  isTerminal(status: string): boolean {
    return TERMINAL_STATUSES.indexOf((status || '').toUpperCase()) >= 0;
  }

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    let outages: Outage[] | null = null;
    let assets: Asset[] | null = null;
    let done = 0;

    const tryBuild = () => {
      if (++done < 2) return;
      const a = assets || [];
      this.assets.set(a);
      this.assetById.set(this.indexAssets(a));
      const o = outages || [];
      this.dropStaleLocalResolved(o);
      this.rows.set(this.toRows(o));
      this.loading.set(false);
    };

    this.outageService.listOutages().subscribe({
      next: (result) => { outages = result; tryBuild(); },
      error: (err) => {
        this.errorMessage.set(extractErrorMessage(err, 'Failed to load outages.'));
        tryBuild();
      },
    });

    this.assetService.list().subscribe({
      next: (result) => { assets = result; tryBuild(); },
      error: () => tryBuild(),
    });
  }

  private indexAssets(assets: Asset[]): Map<number, Asset> {
    const lookup = new Map<number, Asset>();
    for (const asset of assets) lookup.set(assetIdOf(asset), asset);
    return lookup;
  }

  private dropStaleLocalResolved(outages: Outage[]): void {
    for (const outage of outages) {
      if (outage.resolvedAt) this.clearLocalResolved(outage.id);
    }
  }

  private toRows(outages: Outage[]): OutageRow[] {
    return outages.map((outage) => ({
      ...outage,
      affectedDisplay: this.formatAffectedAssets(outage.affectedAssetsJSON),
    }));
  }

  resolvedCell(outage: OutageRow): string {
    const fromApi = this.parseDate(outage.resolvedAt);
    if (fromApi) return this.formatDateTime(fromApi);
    const fromLocal = this.readLocalResolved(outage.id);
    if (fromLocal) return this.formatDateTime(fromLocal);
    return '—';
  }

  reportedCell(outage: OutageRow): string {
    const date = this.parseDate(outage.reportedAt);
    return date ? this.formatDateTime(date) : '—';
  }

  resolvedDisplay(): string {
    return this.resolvedAtValue
      ? this.formatDateTime(new Date(this.resolvedAtValue))
      : '';
  }

  onStatusChange(): void {
    if (this.isTerminal(this.status)) {
      if (!this.resolvedAtValue) {
        this.resolvedAtValue = new Date().toISOString();
      }
    } else {
      this.resolvedAtValue = null;
    }
  }

  private parseDate(raw: any): Date | null {
    if (raw == null || raw === '') return null;
    if (raw instanceof Date) return isNaN(raw.getTime()) ? null : raw;
    const date = new Date(raw);
    return isNaN(date.getTime()) ? null : date;
  }

  private formatDateTime(date: Date): string {
    const datePart = date.toLocaleDateString('en-CA');
    const timePart = date.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' });
    return datePart + '  ' + timePart;
  }

  private readLocalResolved(id: number): Date | null {
    try {
      const raw = localStorage.getItem(RESOLVED_LOCAL_KEY(id));
      return raw ? this.parseDate(raw) : null;
    } catch {
      return null;
    }
  }

  private writeLocalResolved(id: number, iso: string): void {
    try {
      localStorage.setItem(RESOLVED_LOCAL_KEY(id), iso);
    } catch {}
  }

  private clearLocalResolved(id: number): void {
    try {
      localStorage.removeItem(RESOLVED_LOCAL_KEY(id));
    } catch {}
  }

  openAdd(): void {
    this.assetId = '';
    this.severity = 'LOW';
    this.status = 'OPEN';
    this.resolvedAtValue = null;
    this.editingId.set(null);
    this.formError.set(null);
    this.mode.set('add');
  }

  openEdit(outage: OutageRow): void {
    const ids = this.parseAssetIds(outage.affectedAssetsJSON);
    this.assetId = ids.length > 0 ? String(ids[0]) : '';
    this.severity = (outage.severity as OutageSeverity) || 'LOW';
    this.status = (outage.status as OutageStatus) || 'OPEN';
    this.resolvedAtValue = this.pickResolvedForEdit(outage);
    this.editingId.set(outage.id);
    this.formError.set(null);
    this.mode.set('edit');
  }

  private pickResolvedForEdit(outage: OutageRow): string | null {
    const fromApi = this.parseDate(outage.resolvedAt);
    if (fromApi) return fromApi.toISOString();
    if (!this.isTerminal(this.status)) return null;
    const fromLocal = this.readLocalResolved(outage.id);
    return fromLocal ? fromLocal.toISOString() : null;
  }

  cancel(): void {
    this.mode.set('closed');
  }

  submit(): void {
    this.formError.set(null);
    if (this.mode() === 'edit' && this.editingId() !== null) {
      this.saveEdit();
    } else {
      this.createNew();
    }
  }

  private saveEdit(): void {
    this.submitting.set(true);
    const id = this.editingId()!;
    const resolvedIso = this.isTerminal(this.status)
      ? (this.resolvedAtValue || new Date().toISOString())
      : null;

    const body: any = {
      severity: this.severity,
      status: this.status,
      resolvedAt: resolvedIso,
      resolved_at: resolvedIso,
    };

    this.outageService.patchOutage(id, body).subscribe({
      next: () => {
        if (resolvedIso) {
          this.writeLocalResolved(id, resolvedIso);
        } else {
          this.clearLocalResolved(id);
        }
        this.finishSave();
      },
      error: (err) => this.failSave(err, 'Save failed.'),
    });
  }

  private createNew(): void {
    const assetIdNumber = Number(this.assetId);
    if (!isFinite(assetIdNumber) || assetIdNumber <= 0) {
      this.formError.set('Asset is required.');
      return;
    }

    const user = this.tokens.user();
    const body: OutageRequest = {
      affectedAssets: [String(assetIdNumber)],
      severity: this.severity,
      reportedBy: user?.userId || 0,
      status: this.status,
    };

    this.submitting.set(true);
    this.outageService.createOutage(body).subscribe({
      next: () => this.finishSave(),
      error: (err) => this.failSave(err, 'Report failed.'),
    });
  }

  private finishSave(): void {
    this.submitting.set(false);
    this.mode.set('closed');
    this.load();
  }

  private failSave(err: any, fallback: string): void {
    this.formError.set(extractErrorMessage(err, fallback));
    this.submitting.set(false);
  }

  delete(outage: OutageRow): void {
    if (!confirm('Delete outage #' + outage.id + '?')) return;
    this.outageService.deleteOutage(outage.id).subscribe({
      next: () => {
        this.clearLocalResolved(outage.id);
        this.load();
      },
      error: (err) => this.errorMessage.set(extractErrorMessage(err, 'Delete failed.')),
    });
  }

  private parseAssetIds(raw: any): number[] {
    if (!raw) return [];
    try {
      const parsed = JSON.parse(raw);
      if (Array.isArray(parsed)) return parsed.map(Number).filter(isFinite);
      return [];
    } catch {
      return [];
    }
  }

  private formatAffectedAssets(raw: any): string {
    const ids = this.parseAssetIds(raw);
    if (ids.length === 0) return '—';
    return ids.map((id) => {
      const asset = this.assetById().get(id);
      return asset ? assetNameWithType(asset) : 'Asset #' + id;
    }).join(', ');
  }
}
