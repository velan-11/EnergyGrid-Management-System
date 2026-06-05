import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Asset, AssetRequest } from '../models/asset.models';

@Injectable({ providedIn: 'root' })
export class AssetService {
  private http = inject(HttpClient);
  private base = environment.apiUrl + '/api/assets';

  list(): Observable<Asset[]> {
    return this.http.get<Asset[]>(this.base);
  }

  get(id: number): Observable<Asset> {
    return this.http.get<Asset>(this.base + '/' + id);
  }

  create(body: AssetRequest): Observable<Asset> {
    return this.http.post<Asset>(this.base + '/create', body);
  }

  update(id: number, body: AssetRequest): Observable<Asset> {
    return this.http.put<Asset>(this.base + '/put/' + id, body);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(this.base + '/delete/' + id);
  }
}
