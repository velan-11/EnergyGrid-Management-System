import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  DREvent, DREventRequest, DREventStatusResponse,
  DRParticipation, DRParticipationRequest, DRProgram, DRProgramRequest,
  DRReportReductionRequest,
} from '../models/demand-response.models';

@Injectable({ providedIn: 'root' })
export class DemandResponseService {
  private http = inject(HttpClient);
  private base = environment.apiUrl + '/api/demand-response';

  listEvents(): Observable<DREvent[]> {
    return this.http.get<DREvent[]>(this.base + '/events');
  }

  createEvent(body: DREventRequest): Observable<DREvent> {
    return this.http.post<DREvent>(this.base + '/events/create', body);
  }

  activateEvent(id: number): Observable<DREventStatusResponse> {
    return this.http.patch<DREventStatusResponse>(this.base + '/events/' + id + '/activate', {});
  }

  completeEvent(id: number): Observable<DREventStatusResponse> {
    return this.http.patch<DREventStatusResponse>(this.base + '/events/' + id + '/complete', {});
  }

  cancelEvent(id: number): Observable<DREventStatusResponse> {
    return this.http.patch<DREventStatusResponse>(this.base + '/events/' + id + '/cancel', {});
  }

  listPrograms(): Observable<DRProgram[]> {
    return this.http.get<DRProgram[]>(this.base + '/programs');
  }

  createProgram(body: DRProgramRequest): Observable<DRProgram> {
    return this.http.post<DRProgram>(this.base + '/programs/create', body);
  }

  listParticipationsForEvent(eventId: number): Observable<DRParticipation[]> {
    return this.http.get<DRParticipation[]>(this.base + '/participation/event/' + eventId);
  }

  join(body: DRParticipationRequest): Observable<DRParticipation> {
    return this.http.post<DRParticipation>(this.base + '/participation/join', body);
  }

  reportReduction(id: number, body: DRReportReductionRequest): Observable<DRParticipation> {
    return this.http.patch<DRParticipation>(this.base + '/participation/' + id + '/report', body);
  }

  verify(id: number): Observable<DRParticipation> {
    return this.http.patch<DRParticipation>(this.base + '/participation/' + id + '/verify', {});
  }

  optOut(id: number): Observable<DRParticipation> {
    return this.http.patch<DRParticipation>(this.base + '/participation/' + id + '/opt-out', {});
  }
}
