import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  WorkOrder, WorkOrderRequest, MaintenanceEvidence, EvidenceUploadRequest,
} from '../models/workorder.models';

export interface UploadResponse {
  fileUrl: string;
  filename: string;
  originalName: string;
  mimeType: string;
  sizeBytes: number;
  sha256: string;
}

@Injectable({ providedIn: 'root' })
export class WorkOrderService {
  private http = inject(HttpClient);
  private orders = environment.apiUrl + '/api/work-orders';
  private evidence = environment.apiUrl + '/api/evidence';
  private upload = environment.apiUrl + '/api/upload';

  list(): Observable<WorkOrder[]> {
    return this.http.get<WorkOrder[]>(this.orders);
  }

  get(id: number): Observable<WorkOrder> {
    return this.http.get<WorkOrder>(this.orders + '/' + id);
  }

  create(body: WorkOrderRequest): Observable<WorkOrder> {
    return this.http.post<WorkOrder>(this.orders, body);
  }

  update(id: number, body: WorkOrderRequest): Observable<WorkOrder> {
    return this.http.put<WorkOrder>(this.orders + '/' + id, body);
  }

  delete(id: number): Observable<any> {
    return this.http.delete<any>(this.orders + '/' + id);
  }

  updateStatus(id: number, status: string): Observable<WorkOrder> {
    const params = new HttpParams().set('status', status);
    return this.http.patch<WorkOrder>(this.orders + '/' + id + '/status', null, { params });
  }

  assign(id: number, technicianId: number, technicianName?: string | null): Observable<WorkOrder> {
    let params = new HttpParams().set('technicianId', technicianId);
    if (technicianName && technicianName.trim()) {
      params = params.set('technicianName', technicianName.trim());
    }
    const headers = new HttpHeaders({ 'X-Eg-Silent-Errors': '1' });
    return this.http.put<WorkOrder>(this.orders + '/' + id + '/assign', null, { params, headers });
  }

  uploadFile(file: File): Observable<UploadResponse> {
    const fd = new FormData();
    fd.append('file', file, file.name);
    return this.http.post<UploadResponse>(this.upload, fd);
  }

  uploadEvidence(body: EvidenceUploadRequest): Observable<MaintenanceEvidence> {
    return this.http.post<MaintenanceEvidence>(this.evidence, body);
  }

  getEvidence(id: number): Observable<MaintenanceEvidence> {
    return this.http.get<MaintenanceEvidence>(this.evidence + '/' + id);
  }

  updateEvidence(id: number, body: EvidenceUploadRequest): Observable<MaintenanceEvidence> {
    return this.http.put<MaintenanceEvidence>(this.evidence + '/' + id, body);
  }

  deleteEvidence(id: number): Observable<any> {
    return this.http.delete<any>(this.evidence + '/' + id);
  }
}
