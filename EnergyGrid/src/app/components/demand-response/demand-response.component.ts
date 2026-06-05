import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Observable } from 'rxjs';
import { DemandResponseService } from '../../services/demand-response.service';
import { TokenService } from '../../services/token.service';
import { ToastService } from '../../services/toast.service';
import {
  DREvent, DREventRequest, DRParticipation, DRProgram, DRProgramRequest, DRProgramType,
} from '../../models/demand-response.models';
import { extractErrorMessage } from '../../utils/error-message';

type FormMode = 'closed' | 'add';

interface ParticipationVm {
  loading: boolean;
  error: string | null;
  rows: DRParticipation[];
  expanded: boolean;
  reductionInput: Record<number, string>;
}

const EMPTY_PARTICIPATION_VM: ParticipationVm = {
  loading: false, error: null, rows: [], expanded: false, reductionInput: {},
};

@Component({
  selector: 'app-demand-response',
  imports: [CommonModule, FormsModule],
  templateUrl: './demand-response.component.html',
  styleUrl: './demand-response.component.css',
})
export class DemandResponseComponent implements OnInit {
  private drService = inject(DemandResponseService);
  private tokens = inject(TokenService);
  private toast = inject(ToastService);

  events = signal<DREvent[]>([]);
  programs = signal<DRProgram[]>([]);
  programById = signal<Map<number, DRProgram>>(new Map());
  loading = signal(true);
  errorMessage = signal<string | null>(null);

  role = computed(() => (this.tokens.user()?.role || '').toUpperCase());
  canWriteEvent = computed(() => ['ADMIN', 'OPERATOR'].includes(this.role()));
  canWriteProgram = computed(() => ['ADMIN', 'OPERATOR'].includes(this.role()));
  canVerify = computed(() => ['ADMIN', 'OPERATOR'].includes(this.role()));
  canSeeParticipants = computed(() => ['ADMIN', 'OPERATOR', 'AUDITOR'].includes(this.role()));
  canJoin = computed(() => ['ADMIN', 'OPERATOR', 'PRODUCER', 'CUSTOMER'].includes(this.role()));

  eventMode = signal<FormMode>('closed');
  eventSubmitting = signal(false);
  eventFormError = signal<string | null>(null);
  ev_eventName = '';
  ev_programId = '';
  ev_startAt = '';
  ev_endAt = '';
  ev_targetReductionKW = '';

  programMode = signal<FormMode>('closed');
  programSubmitting = signal(false);
  programFormError = signal<string | null>(null);
  pg_name = '';
  pg_type: DRProgramType = 'PEAK_SHAVING';
  pg_note = '';

  participations = signal<Map<number, ParticipationVm>>(new Map());
  confirmCancelFor = signal<number | null>(null);
  rowMsg = signal<Map<number, { text: string; tone: 'ok' | 'err' }>>(new Map());

  joinedEventIds = signal<Set<number>>(new Set());
  joiningEventId = signal<number | null>(null);

  ngOnInit(): void {
    this.joinedEventIds.set(this.readJoinedFromStorage());
    this.load();
  }

  private storageKey(): string {
    const userId = this.tokens.user()?.userId || 0;
    return 'eg_dr_joined_' + userId;
  }

  private readJoinedFromStorage(): Set<number> {
    try {
      const raw = localStorage.getItem(this.storageKey());
      if (!raw) return new Set();
      const ids = JSON.parse(raw);
      return Array.isArray(ids) ? new Set(ids.map(Number).filter(isFinite)) : new Set();
    } catch {
      return new Set();
    }
  }

  private writeJoinedToStorage(ids: Set<number>): void {
    try {
      localStorage.setItem(this.storageKey(), JSON.stringify(Array.from(ids)));
    } catch {}
  }

  hasJoined(event: DREvent): boolean {
    return this.joinedEventIds().has(event.eventId);
  }

  isJoining(event: DREvent): boolean {
    return this.joiningEventId() === event.eventId;
  }

  isActiveStatus(event: DREvent): boolean {
    return (event.status || '').toUpperCase() === 'ACTIVE';
  }

  private load(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    let done = 0;
    const finish = () => { if (++done === 2) this.loading.set(false); };

    this.drService.listEvents().subscribe({
      next: (events) => { this.events.set(this.sortByCreatedDesc(events || [])); finish(); },
      error: (err) => {
        this.errorMessage.set(extractErrorMessage(err, 'Failed to load Demand Response data.'));
        finish();
      },
    });

    this.drService.listPrograms().subscribe({
      next: (programs) => {
        this.programs.set(programs || []);
        this.programById.set(this.indexPrograms(programs || []));
        finish();
      },
      error: () => finish(),
    });
  }

  private indexPrograms(programs: DRProgram[]): Map<number, DRProgram> {
    const lookup = new Map<number, DRProgram>();
    for (const program of programs) lookup.set(program.programId, program);
    return lookup;
  }

  private sortByCreatedDesc(events: DREvent[]): DREvent[] {
    return [...events].sort((a, b) => {
      const aTime = Date.parse(a.createdAt || a.startAt) || 0;
      const bTime = Date.parse(b.createdAt || b.startAt) || 0;
      return bTime - aTime;
    });
  }

  formatTime(raw: any): string {
    if (!raw) return '—';
    const date = new Date(raw);
    if (isNaN(date.getTime())) return '—';
    const datePart = date.toLocaleDateString('en-CA');
    const timePart = date.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' });
    return datePart + '  ' + timePart;
  }

  programNameFor(programId: number): string {
    const program = this.programById().get(programId);
    if (!program) return 'Program #' + programId;
    return program.name + ' — ' + program.type;
  }

  statusPill(status: string): string {
    const value = (status || '').toUpperCase();
    if (value === 'ACTIVE') return 'pill active';
    if (value === 'SCHEDULED') return 'pill scheduled';
    if (value === 'COMPLETED') return 'pill inactive';
    if (value === 'CANCELLED') return 'pill rejected';
    return 'pill muted-pill';
  }

  participationPill(status: string): string {
    const value = (status || '').toUpperCase();
    if (value === 'VERIFIED') return 'pill active';
    if (value === 'REPORTED') return 'pill pending';
    if (value === 'OPTED_OUT') return 'pill rejected';
    if (value === 'REGISTERED') return 'pill scheduled';
    return 'pill muted-pill';
  }

  openAddEvent(): void {
    this.ev_eventName = '';
    this.ev_programId = this.programs()[0]?.programId
      ? String(this.programs()[0].programId)
      : '';
    this.ev_startAt = '';
    this.ev_endAt = '';
    this.ev_targetReductionKW = '';
    this.eventFormError.set(null);
    this.eventMode.set('add');
  }

  cancelEventForm(): void {
    this.eventMode.set('closed');
  }

  submitEvent(): void {
    this.eventFormError.set(null);
    const body = this.buildValidEventBody();
    if (!body) return;

    this.eventSubmitting.set(true);
    this.drService.createEvent(body).subscribe({
      next: () => {
        this.eventSubmitting.set(false);
        this.eventMode.set('closed');
        this.load();
      },
      error: (err) => {
        this.eventFormError.set(extractErrorMessage(err, 'Save failed.'));
        this.eventSubmitting.set(false);
      },
    });
  }

  private buildValidEventBody(): DREventRequest | null {
    const eventName = this.ev_eventName.trim();
    const programId = Number(this.ev_programId);
    const target = Number(this.ev_targetReductionKW);

    if (eventName.length < 3) {
      this.eventFormError.set('Event name must be at least 3 characters.');
      return null;
    }
    if (!isFinite(programId) || programId <= 0) {
      this.eventFormError.set('Program is required.');
      return null;
    }
    if (!this.ev_startAt || !this.ev_endAt) {
      this.eventFormError.set('Start and end times are required.');
      return null;
    }
    if (this.ev_startAt >= this.ev_endAt) {
      this.eventFormError.set('End time must be after start time.');
      return null;
    }
    if (Date.parse(this.ev_startAt) < Date.now() - 60000) {
      this.eventFormError.set('Start time must not be in the past.');
      return null;
    }
    if (!isFinite(target) || target <= 0) {
      this.eventFormError.set('Target reduction must be > 0 kW.');
      return null;
    }

    return {
      eventName,
      programId,
      startAt: this.toIso(this.ev_startAt),
      endAt: this.toIso(this.ev_endAt),
      targetReductionKW: target,
    };
  }

  private toIso(localTime: string): string {
    return localTime.length === 16 ? localTime + ':00' : localTime;
  }

  openAddProgram(): void {
    this.pg_name = '';
    this.pg_type = 'PEAK_SHAVING';
    this.pg_note = '';
    this.programFormError.set(null);
    this.programMode.set('add');
  }

  cancelProgramForm(): void {
    this.programMode.set('closed');
  }

  submitProgram(): void {
    this.programFormError.set(null);
    const name = this.pg_name.trim();
    if (name.length < 3) {
      this.programFormError.set('Program name must be at least 3 characters.');
      return;
    }

    const body: DRProgramRequest = {
      name,
      type: this.pg_type,
      enrollmentCriteriaJson: this.pg_note.trim() ? { note: this.pg_note.trim() } : {},
    };

    this.programSubmitting.set(true);
    this.drService.createProgram(body).subscribe({
      next: () => {
        this.programSubmitting.set(false);
        this.programMode.set('closed');
        this.load();
      },
      error: (err) => {
        this.programFormError.set(extractErrorMessage(err, 'Save failed.'));
        this.programSubmitting.set(false);
      },
    });
  }

  activate(event: DREvent): void {
    this.runEventAction(event.eventId, this.drService.activateEvent(event.eventId), 'Event activated.');
  }

  complete(event: DREvent): void {
    this.runEventAction(event.eventId, this.drService.completeEvent(event.eventId), 'Event completed.');
  }

  askCancel(event: DREvent): void {
    this.confirmCancelFor.set(event.eventId);
  }

  abortCancel(): void {
    this.confirmCancelFor.set(null);
  }

  confirmCancel(event: DREvent): void {
    this.confirmCancelFor.set(null);
    this.runEventAction(event.eventId, this.drService.cancelEvent(event.eventId), 'Event cancelled.');
  }

  private runEventAction(eventId: number, request$: Observable<any>, successMessage: string): void {
    request$.subscribe({
      next: (response) => {
        this.patchEventStatus(eventId, response.status);
        this.flashMessage(eventId, successMessage, 'ok');
      },
      error: (err) => this.flashMessage(eventId, extractErrorMessage(err, 'Action failed.'), 'err'),
    });
  }

  private patchEventStatus(eventId: number, status: string): void {
    this.events.update((list) =>
      list.map((event) => event.eventId === eventId ? { ...event, status } : event),
    );
  }

  private flashMessage(eventId: number, text: string, tone: 'ok' | 'err'): void {
    this.rowMsg.update((map) => {
      const next = new Map(map);
      next.set(eventId, { text, tone });
      return next;
    });
    setTimeout(() => this.clearRowMessage(eventId), 3000);
  }

  private clearRowMessage(eventId: number): void {
    this.rowMsg.update((map) => {
      if (!map.has(eventId)) return map;
      const next = new Map(map);
      next.delete(eventId);
      return next;
    });
  }

  msgFor(eventId: number): { text: string; tone: 'ok' | 'err' } | null {
    return this.rowMsg().get(eventId) || null;
  }

  isActiveBtn(event: DREvent): boolean {
    return (event.status || '').toUpperCase() === 'SCHEDULED';
  }

  isCompleteBtn(event: DREvent): boolean {
    return (event.status || '').toUpperCase() === 'ACTIVE';
  }

  isCancelBtn(event: DREvent): boolean {
    const value = (event.status || '').toUpperCase();
    return value === 'SCHEDULED' || value === 'ACTIVE';
  }

  canJoinEvent(event: DREvent): boolean {
    if (!this.canJoin()) return false;
    if (this.hasJoined(event)) return false;
    if (this.isJoining(event)) return false;
    return this.isActiveStatus(event);
  }

  toggleParticipations(event: DREvent): void {
    if (!this.canSeeParticipants()) return;
    const current = this.participations().get(event.eventId);

    if (current?.expanded) {
      this.collapseParticipations(event.eventId, current);
      return;
    }
    if (current) {
      this.expandParticipations(event.eventId, current);
      return;
    }
    this.fetchParticipations(event.eventId);
  }

  private collapseParticipations(eventId: number, current: ParticipationVm): void {
    this.participations.update((map) => {
      const next = new Map(map);
      next.set(eventId, { ...current, expanded: false });
      return next;
    });
  }

  private expandParticipations(eventId: number, current: ParticipationVm): void {
    this.participations.update((map) => {
      const next = new Map(map);
      next.set(eventId, { ...current, expanded: true });
      return next;
    });
  }

  private fetchParticipations(eventId: number): void {
    this.setParticipationVm(eventId, { ...EMPTY_PARTICIPATION_VM, loading: true, expanded: true });

    this.drService.listParticipationsForEvent(eventId).subscribe({
      next: (rows) => this.patchParticipationVm(eventId, { loading: false, rows: rows || [] }),
      error: (err) => this.patchParticipationVm(eventId, {
        loading: false,
        error: extractErrorMessage(err, 'Failed to load participants.'),
      }),
    });
  }

  private setParticipationVm(eventId: number, vm: ParticipationVm): void {
    this.participations.update((map) => {
      const next = new Map(map);
      next.set(eventId, vm);
      return next;
    });
  }

  private patchParticipationVm(eventId: number, patch: Partial<ParticipationVm>): void {
    this.participations.update((map) => {
      const next = new Map(map);
      const existing = next.get(eventId) || { ...EMPTY_PARTICIPATION_VM, expanded: true };
      next.set(eventId, { ...existing, ...patch });
      return next;
    });
  }

  partsFor(eventId: number): ParticipationVm | null {
    return this.participations().get(eventId) || null;
  }

  joinEvent(event: DREvent): void {
    if (!this.canJoinEvent(event)) return;

    this.joiningEventId.set(event.eventId);

    this.drService.join({ eventId: event.eventId }).subscribe({
      next: (participation) => {
        this.markEventJoined(event.eventId);
        this.upsertParticipation(event.eventId, participation);
        this.joiningEventId.set(null);
        this.toast.success('Successfully joined event!');
      },
      error: () => {
        this.joiningEventId.set(null);
        this.toast.error('Failed to join event. Please try again.');
      },
    });
  }

  private markEventJoined(eventId: number): void {
    const next = new Set(this.joinedEventIds());
    next.add(eventId);
    this.joinedEventIds.set(next);
    this.writeJoinedToStorage(next);
  }

  reportReduction(event: DREvent, participation: DRParticipation): void {
    const vm = this.participations().get(event.eventId);
    const raw = vm?.reductionInput[participation.participationId] || '';
    const reduction = Number(raw);

    if (!isFinite(reduction) || reduction <= 0) {
      this.flashMessage(event.eventId, 'Reported reduction must be > 0.', 'err');
      return;
    }

    this.drService.reportReduction(participation.participationId, { reportedReductionKW: reduction }).subscribe({
      next: (updated) => this.upsertParticipation(event.eventId, updated),
      error: (err) => this.flashMessage(event.eventId, extractErrorMessage(err, 'Report failed.'), 'err'),
    });
  }

  verifyParticipation(event: DREvent, participation: DRParticipation): void {
    this.drService.verify(participation.participationId).subscribe({
      next: (updated) => this.upsertParticipation(event.eventId, updated),
      error: (err) => this.flashMessage(event.eventId, extractErrorMessage(err, 'Verify failed.'), 'err'),
    });
  }

  optOut(event: DREvent, participation: DRParticipation): void {
    this.drService.optOut(participation.participationId).subscribe({
      next: (updated) => this.upsertParticipation(event.eventId, updated),
      error: (err) => this.flashMessage(event.eventId, extractErrorMessage(err, 'Opt-out failed.'), 'err'),
    });
  }

  private upsertParticipation(eventId: number, participation: DRParticipation): void {
    this.participations.update((map) => {
      const next = new Map(map);
      const existing = next.get(eventId) || { ...EMPTY_PARTICIPATION_VM, expanded: true };
      const index = existing.rows.findIndex((row) => row.participationId === participation.participationId);
      const rows = index >= 0
        ? existing.rows.map((row, i) => i === index ? participation : row)
        : [...existing.rows, participation];
      next.set(eventId, { ...existing, rows });
      return next;
    });
  }
}
