import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AdminUser } from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private http = inject(HttpClient);
  private base = environment.apiUrl + '/api/admin/users';

  list(): Observable<AdminUser[]> {
    return this.http.get<AdminUser[]>(this.base);
  }

  me(): Observable<AdminUser> {
    return this.http.get<AdminUser>(this.base + '/me');
  }

  get(id: number): Observable<AdminUser> {
    return this.http.get<AdminUser>(this.base + '/' + id);
  }

  pending(): Observable<AdminUser[]> {
    return this.http.get<AdminUser[]>(this.base + '/pending');
  }

  approve(id: number): Observable<any> {
    return this.http.put(this.base + '/' + id + '/approve', {}, { responseType: 'text' });
  }

  softDelete(id: number): Observable<any> {
    return this.http.delete(this.base + '/' + id, { responseType: 'text' });
  }

  restore(id: number): Observable<any> {
    return this.http.put(this.base + '/' + id + '/restore', {}, { responseType: 'text' });
  }
}
