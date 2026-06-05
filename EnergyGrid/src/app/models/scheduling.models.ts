export type ScheduleStatus = 'SCHEDULED' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED';
export type DispatchStatus = 'SUCCESS' | 'PARTIAL' | 'FAILED' | 'MANUAL_OVERRIDE' | 'PENDING';

export interface GenerationSchedule {
  scheduleId: number;
  assetId: number;
  startAt: string;
  endAt: string;
  targetKw: number;
  createdBy: string;
  createdAt: string;
  status: string;
}

export interface ScheduleRequest {
  assetId: number;
  startAt: string;
  endAt: string;
  targetKw: number;
  createdBy: string;
}

export interface DispatchRecord {
  dispatchId: number;
  scheduleId: number;
  executedAt: string;
  executedBy: number;
  executedByName?: string;
  executedByUsername?: string;
  actualKw: number;
  targetKw?: number;
  status: string;
  notes: string;
}

export interface DispatchRequest {
  scheduleId: number;
  actualKw: number;
  executedBy: number;
  executedByName?: string;
  executedByUsername?: string;
  notes?: string;
}
