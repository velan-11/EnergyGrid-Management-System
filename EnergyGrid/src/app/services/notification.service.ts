import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subscription, tap, timer, switchMap } from 'rxjs';
import { environment } from '../../environments/environment';
import { TokenService } from './token.service';
import { AppNotification, NotificationRequest } from '../models/notification.models';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private http = inject(HttpClient);
  private tokens = inject(TokenService);
  private base = environment.apiUrl + '/api/notifications';

  notifications = signal<AppNotification[]>([]);
  unreadCount = computed(() =>
    this.notifications().filter((n) => n.status === 'UNREAD').length,
  );

  private pollSub: Subscription | null = null;

  listAll(): Observable<AppNotification[]> {
    return this.http.get<AppNotification[]>(this.base).pipe(
      tap((list) => this.notifications.set(list || [])),
    );
  }

  listByUser(userId: number): Observable<AppNotification[]> {
    return this.http.get<AppNotification[]>(this.base + '/user/' + userId).pipe(
      tap((list) => this.notifications.set(list || [])),
    );
  }

  create(body: NotificationRequest): Observable<void> {
    return this.http.post<void>(this.base + '/create', body);
  }

  markAsRead(id: number): Observable<AppNotification> {
    return this.http.put<AppNotification>(this.base + '/' + id + '/read', {}).pipe(
      tap((updated) =>
        this.notifications.update((list) =>
          list.map((n) => (n.id === id ? updated : n)),
        ),
      ),
    );
  }

  markAllAsRead(): Observable<void> {
    return this.http.put<void>(this.base + '/read-all', {}).pipe(
      tap(() =>
        this.notifications.update((list) =>
          list.map((n) => ({ ...n, status: 'READ' })),
        ),
      ),
    );
  }

  startPolling(intervalMs = 5000): void {
    this.stopPolling();
    this.pollSub = timer(0, intervalMs)
      .pipe(switchMap(() => {
        const user = this.tokens.user();
        return user ? this.listByUser(user.userId) : this.listAll();
      }))
      .subscribe({ error: () => {} });
  }

  stopPolling(): void {
    if (this.pollSub) {
      this.pollSub.unsubscribe();
      this.pollSub = null;
    }
  }
}
