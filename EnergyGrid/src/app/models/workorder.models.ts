export type WorkOrderStatus = 'CREATED' | 'ASSIGNED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface MaintenanceEvidence {
  id: number;
  evidenceUrl: string;
  notes: string;
  uploadedAt: string;
  uploadedBy: string;
  sha256: string;
  status: string;
}

export interface Technician {
  id: number;
  name: string;
  email?: string;
  phone?: string;
  specialization?: string;
}

export interface WorkOrder {
  id: number;
  assetId: number;
  issueDescription: string;
  status: string;
  createdAt: string;
  dueDate: string;
  technician: Technician | null;
  evidences: MaintenanceEvidence[];
}

export interface WorkOrderRequest {
  assetId: number;
  issueDescription: string;
  dueDate: string;
}

export interface EvidenceUploadRequest {
  workOrderId: number;
  evidenceUrl: string;
  notes?: string;
  uploadedBy?: string;
  status?: string;
}
