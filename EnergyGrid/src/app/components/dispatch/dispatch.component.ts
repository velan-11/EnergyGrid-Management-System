import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SchedulingService } from '../../services/scheduling.service';
import { TokenService } from '../../services/token.service';
import { DispatchRecord, DispatchRequest, GenerationSchedule } from '../../models/scheduling.models';
import { extractErrorMessage } from '../../utils/error-message';

@Component({
  selector: 'app-dispatch',
  imports: [CommonModule, FormsModule, DatePipe],
  templateUrl: './dispatch.component.html',
})
export class DispatchComponent implements OnInit {
  private svc = inject(SchedulingService);
  private tokens = inject(TokenService);

  rows = signal<DispatchRecord[]>([]);
  schedules = signal<GenerationSchedule[]>([]);
  scheduleById = signal<Map<number, GenerationSchedule>>(new Map());
  loading = signal(true);
  error = signal<string | null>(null);

  role = computed(() => this.tokens.user()?.role || '');
  canWrite = computed(() => this.role() === 'ADMIN');

  formOpen = signal(false);
  submitting = signal(false);
  formError = signal<string | null>(null);

  scheduleId = '';
  actualKw = '';
  notes = '';

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.error.set(null);
    let done = 0;
    const finish = () => { if (++done === 2) this.loading.set(false); };

    this.svc.listDispatches().subscribe({
      next: (dispatches) => { this.rows.set(dispatches || []); finish(); },
      error: (err) => {
        this.error.set(extractErrorMessage(err, 'Failed to load dispatch records.'));
        finish();
      },
    });

    this.svc.listSchedules().subscribe({
      next: (schedules) => {
        this.schedules.set(schedules || []);
        const lookup = new Map<number, GenerationSchedule>();
        for (const schedule of schedules || []) lookup.set(schedule.scheduleId, schedule);
        this.scheduleById.set(lookup);
        finish();
      },
      error: () => finish(),
    });
  }

  scheduledKwFor(dispatch: DispatchRecord): any {
    const schedule = this.scheduleById().get(dispatch.scheduleId);
    if (schedule && isFinite(schedule.targetKw)) return schedule.targetKw;
    if (dispatch.targetKw != null) return dispatch.targetKw;
    return '—';
  }

  pickedSchedule = computed(() => {
    const id = Number(this.scheduleId);
    if (!isFinite(id) || id <= 0) return null;
    return this.scheduleById().get(id) || null;
  });

  openAdd(): void {
    this.scheduleId = '';
    this.actualKw = '';
    this.notes = '';
    this.formError.set(null);
    this.formOpen.set(true);
  }

  cancelForm(): void {
    this.formOpen.set(false);
  }

  submit(): void {
    const scheduleIdNumber = Number(this.scheduleId);
    const actualKwNumber = Number(this.actualKw);

    if (!isFinite(scheduleIdNumber) || scheduleIdNumber <= 0) {
      this.formError.set('Schedule is required.');
      return;
    }
    if (!isFinite(actualKwNumber) || actualKwNumber < 0) {
      this.formError.set('Delivered kW must be ≥ 0.');
      return;
    }

    const user = this.tokens.user();
    const body: DispatchRequest = {
      scheduleId: scheduleIdNumber,
      actualKw: actualKwNumber,
      executedBy: user?.userId || 0,
      executedByName: user?.name,
      executedByUsername: user?.username,
      notes: this.notes || undefined,
    };

    this.submitting.set(true);
    this.formError.set(null);

    this.svc.executeDispatch(body).subscribe({
      next: () => {
        this.submitting.set(false);
        this.formOpen.set(false);
        this.load();
      },
      error: (err) => {
        this.formError.set(extractErrorMessage(err, 'Dispatch failed.'));
        this.submitting.set(false);
      },
    });
  }
}
