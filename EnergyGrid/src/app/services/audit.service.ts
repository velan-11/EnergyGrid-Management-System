import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { AuditLogEntry } from '../models/auth.models';

const LOGIN_ACTIONS = ['LOGIN', 'LOGIN_FAILED'];

@Injectable({ providedIn: 'root' })
export class AuditService {
  private http = inject(HttpClient);
  private base = environment.apiUrl + '/api/identity/audit';

  listAll(): Observable<AuditLogEntry[]> {
    return this.http.get<any>(this.base).pipe(map((raw) => this.processResponse(raw)));
  }

  getByUser(userId: number): Observable<AuditLogEntry[]> {
    return this.http.get<any>(this.base + '/' + userId).pipe(map((raw) => this.processResponse(raw)));
  }

  private processResponse(raw: any): AuditLogEntry[] {
    const entries = this.toEntries(raw);
    const logins = entries.filter((e) => LOGIN_ACTIONS.indexOf((e.action || '').toUpperCase()) >= 0);
    return logins.sort((a, b) => this.timestampMs(b) - this.timestampMs(a));
  }

  private toEntries(raw: any): AuditLogEntry[] {
    let list: any[] = [];
    if (Array.isArray(raw)) {
      list = raw;
    } else if (raw && typeof raw === 'object') {
      list = raw.content || raw.data || raw.items || [];
    }
    return list.map((o) => this.toEntry(o));
  }

  private toEntry(o: any): AuditLogEntry {
    return {
      auditId: Number(o.auditId || o.id || 0),
      userId: Number(o.userId || o.performedBy || 0),
      name: String(o.name || o.userName || o.actor || ''),
      action: String(o.action || ''),
      resourceType: String(o.resourceType || o.entityType || ''),
      resourceId: Number(o.resourceId || o.entityId || 0),
      details: String(o.details || o.description || o.message || ''),
      timestamp: String(o.timestamp || o.createdAt || o.performedAt || ''),
      serviceName: 'identity',
    };
  }

  private timestampMs(entry: AuditLogEntry): number {
    if (!entry.timestamp) return 0;
    const ms = Date.parse(entry.timestamp);
    return isFinite(ms) ? ms : 0;
  }
}
