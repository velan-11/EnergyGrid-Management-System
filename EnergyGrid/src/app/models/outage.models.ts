export type OutageSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type OutageStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
export type IncidentStatus = 'ASSIGNED' | 'IN_PROGRESS' | 'COMPLETED' | 'DONE';

export interface Outage {
  id: number;
  affectedAssetsJSON: string;
  reportedAt: string;
  resolvedAt?: string | null;
  severity: string;
  reportedBy: number;
  status: string;
}

export interface OutageRequest {
  affectedAssets: string[];
  severity: OutageSeverity;
  reportedBy: number;
  status: OutageStatus;
}

export interface IncidentTask {
  id: number;
  outageId: number;
  assignedTo: number;
  assignedAt: string;
  completedAt: string | null;
  evidenceURI: string;
  status: string;
}

export interface IncidentTaskRequest {
  outageId: number;
  assignedTo: number;
  evidenceURI: string;
  status: IncidentStatus;
}
