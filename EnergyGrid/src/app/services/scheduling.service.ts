import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  GenerationSchedule, ScheduleRequest, DispatchRecord, DispatchRequest,
} from '../models/scheduling.models';

@Injectable({ providedIn: 'root' })
export class SchedulingService {
  private http = inject(HttpClient);
  private schedules = environment.apiUrl + '/api/schedules';
  private dispatches = environment.apiUrl + '/api/dispatch';

  listSchedules(): Observable<GenerationSchedule[]> {
    return this.http.get<GenerationSchedule[]>(this.schedules);
  }

  getSchedule(id: number): Observable<GenerationSchedule> {
    return this.http.get<GenerationSchedule>(this.schedules + '/' + id);
  }

  schedulesByAsset(assetId: number): Observable<GenerationSchedule[]> {
    return this.http.get<GenerationSchedule[]>(this.schedules + '/asset/' + assetId);
  }

  createSchedule(body: ScheduleRequest): Observable<GenerationSchedule> {
    return this.http.post<GenerationSchedule>(this.schedules, body);
  }

  cancelSchedule(id: number): Observable<GenerationSchedule> {
    return this.http.put<GenerationSchedule>(this.schedules + '/' + id + '/cancel', {});
  }

  listDispatches(): Observable<DispatchRecord[]> {
    return this.http.get<DispatchRecord[]>(this.dispatches);
  }

  getDispatch(id: number): Observable<DispatchRecord> {
    return this.http.get<DispatchRecord>(this.dispatches + '/' + id);
  }

  dispatchesBySchedule(scheduleId: number): Observable<DispatchRecord[]> {
    return this.http.get<DispatchRecord[]>(this.dispatches + '/schedule/' + scheduleId);
  }

  executeDispatch(body: DispatchRequest): Observable<DispatchRecord> {
    return this.http.post<DispatchRecord>(this.dispatches, body);
  }
}
