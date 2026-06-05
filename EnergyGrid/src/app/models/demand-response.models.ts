export type DREventStatus = 'SCHEDULED' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED';
export type DRProgramType = 'PEAK_SHAVING' | 'LOAD_SHIFTING' | 'EMERGENCY_DR' | 'PRICE_BASED';
export type DRProgramStatus = 'DRAFT' | 'ACTIVE' | 'INACTIVE' | 'ARCHIVED';
export type DRParticipationStatus = 'REGISTERED' | 'REPORTED' | 'VERIFIED' | 'OPTED_OUT';

export interface DRProgram {
  programId: number;
  name: string;
  type: string;
  status: string;
  enrollmentCriteriaJson?: any;
  createdBy: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface DRProgramRequest {
  name: string;
  type: DRProgramType;
  enrollmentCriteriaJson: any;
}

export interface DREvent {
  eventId: number;
  eventName: string;
  programId: number;
  startAt: string;
  endAt: string;
  targetReductionKW: number;
  status: string;
  createdBy: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface DREventRequest {
  eventName: string;
  programId: number;
  startAt: string;
  endAt: string;
  targetReductionKW: number;
}

export interface DRParticipation {
  participationId: number;
  eventId: number;
  participantEmail: string;
  reportedReductionKW?: number | null;
  verifiedAt?: string | null;
  status: string;
  createdBy?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface DRParticipationRequest {
  eventId: number;
}

export interface DRReportReductionRequest {
  reportedReductionKW: number;
}

export interface DREventStatusResponse {
  id: number;
  status: string;
}
