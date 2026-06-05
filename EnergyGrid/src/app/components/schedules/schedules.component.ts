import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SchedulingService } from '../../services/scheduling.service';
import { AssetService } from '../../services/asset.service';
import { TokenService } from '../../services/token.service';
import { GenerationSchedule, ScheduleRequest } from '../../models/scheduling.models';
import { Asset, assetId, assetName, assetNameWithType, assetType } from '../../models/asset.models';
import { extractErrorMessage } from '../../utils/error-message';

@Component({
  selector: 'app-schedules',
  imports: [CommonModule, FormsModule, DatePipe],
  templateUrl: './schedules.component.html',
  styleUrl: './schedules.component.css',
})
export class SchedulesComponent implements OnInit {
  private svc = inject(SchedulingService);
  private assetSvc = inject(AssetService);
  private tokens = inject(TokenService);

  rows = signal<GenerationSchedule[]>([]);
  assets = signal<Asset[]>([]);
  assetById = signal<Map<number, Asset>>(new Map());
  loading = signal(true);
  error = signal<string | null>(null);

  role = computed(() => this.tokens.user()?.role || '');
  canWrite = computed(() => this.role() === 'ADMIN');

  formOpen = signal(false);
  submitting = signal(false);
  formError = signal<string | null>(null);

  assetId = '';
  targetKw = '';
  startAt = '';
  endAt = '';

  aId = assetId;
  aName = assetName;
  aType = assetType;
  aLabel = assetNameWithType;

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.error.set(null);
    let done = 0;
    const finish = () => { if (++done === 2) this.loading.set(false); };

    this.svc.listSchedules().subscribe({
      next: (schedules) => { this.rows.set(schedules || []); finish(); },
      error: (err) => {
        this.error.set(extractErrorMessage(err, 'Failed to load schedules.'));
        finish();
      },
    });

    this.assetSvc.list().subscribe({
      next: (assets) => {
        this.assets.set(assets || []);
        const lookup = new Map<number, Asset>();
        for (const asset of assets || []) lookup.set(assetId(asset), asset);
        this.assetById.set(lookup);
        finish();
      },
      error: () => finish(),
    });
  }

  assetLabel(scheduleAssetId: number): string {
    const asset = this.assetById().get(scheduleAssetId);
    if (!asset) return 'Asset #' + scheduleAssetId;
    return assetNameWithType(asset);
  }

  openAdd(): void {
    this.assetId = '';
    this.targetKw = '';
    this.startAt = '';
    this.endAt = '';
    this.formError.set(null);
    this.formOpen.set(true);
  }

  cancelForm(): void {
    this.formOpen.set(false);
  }

  submit(): void {
    if (!this.isFormValid()) return;

    const body: ScheduleRequest = {
      assetId: Number(this.assetId),
      startAt: this.toIso(this.startAt),
      endAt: this.toIso(this.endAt),
      targetKw: Number(this.targetKw),
      createdBy: this.currentUserLabel(),
    };

    this.submitting.set(true);
    this.formError.set(null);

    this.svc.createSchedule(body).subscribe({
      next: () => {
        this.submitting.set(false);
        this.formOpen.set(false);
        this.load();
      },
      error: (err) => {
        this.formError.set(extractErrorMessage(err, 'Save failed.'));
        this.submitting.set(false);
      },
    });
  }

  private isFormValid(): boolean {
    const assetIdNumber = Number(this.assetId);
    const kw = Number(this.targetKw);

    if (!isFinite(assetIdNumber) || assetIdNumber <= 0) {
      this.formError.set('Asset is required.');
      return false;
    }
    if (!isFinite(kw) || kw <= 0) {
      this.formError.set('Scheduled output must be > 0.');
      return false;
    }
    if (!this.startAt || !this.endAt) {
      this.formError.set('Start and end are required.');
      return false;
    }
    if (this.startAt >= this.endAt) {
      this.formError.set('Start must be before end.');
      return false;
    }
    return true;
  }

  private currentUserLabel(): string {
    const user = this.tokens.user();
    return user?.username || user?.name || 'unknown';
  }

  cancelSchedule(schedule: GenerationSchedule): void {
    if (!confirm('Cancel schedule #' + schedule.scheduleId + '?')) return;
    this.svc.cancelSchedule(schedule.scheduleId).subscribe({
      next: () => this.load(),
      error: (err) => this.error.set(extractErrorMessage(err, 'Cancel failed.')),
    });
  }

  private toIso(localTime: string): string {
    return localTime.length === 16 ? localTime + ':00' : localTime;
  }
}
