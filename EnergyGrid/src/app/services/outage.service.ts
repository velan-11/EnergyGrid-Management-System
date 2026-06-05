import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Outage, OutageRequest, IncidentTask, IncidentTaskRequest } from '../models/outage.models';

@Injectable({ providedIn: 'root' })
export class OutageService {
  private http = inject(HttpClient);
  private outages = environment.apiUrl + '/api/outages';
  private incidents = environment.apiUrl + '/api/incident-tasks';

  listOutages(): Observable<Outage[]> {
    return this.http.get<Outage[]>(this.outages);
  }

  createOutage(body: OutageRequest): Observable<Outage> {
    return this.http.post<Outage>(this.outages, body);
  }

  getOutage(id: number): Observable<Outage> {
    return this.http.get<Outage>(this.outages + '/' + id);
  }

  patchOutage(id: number, body: any): Observable<Outage> {
    return this.http.patch<Outage>(this.outages + '/' + id, body);
  }

  deleteOutage(id: number): Observable<any> {
    return this.http.delete<any>(this.outages + '/' + id);
  }

  listIncidents(): Observable<IncidentTask[]> {
    return this.http.get<IncidentTask[]>(this.incidents);
  }

  getIncident(id: number): Observable<IncidentTask> {
    return this.http.get<IncidentTask>(this.incidents + '/' + id);
  }

  createIncident(body: IncidentTaskRequest): Observable<IncidentTask> {
    return this.http.post<IncidentTask>(this.incidents, body);
  }

  updateIncidentStatus(id: number, status: string): Observable<IncidentTask> {
    const params = new HttpParams().set('status', status);
    return this.http.put<IncidentTask>(this.incidents + '/' + id + '/status', null, { params });
  }

  updateIncidentEvidence(id: number, evidenceURI: string): Observable<IncidentTask> {
    const params = new HttpParams().set('evidenceURI', evidenceURI);
    return this.http.put<IncidentTask>(this.incidents + '/' + id + '/evidence', null, { params });
  }
}
