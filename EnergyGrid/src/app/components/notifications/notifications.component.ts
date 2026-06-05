import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { NotificationService } from '../../services/notification.service';
import { TokenService } from '../../services/token.service';
import { AppNotification } from '../../models/notification.models';
import { extractErrorMessage } from '../../utils/error-message';

@Component({
  selector: 'app-notifications',
  imports: [CommonModule, DatePipe],
  templateUrl: './notifications.component.html',
  styleUrl: './notifications.component.css',
})
export class NotificationsComponent implements OnInit {
  private notificationService = inject(NotificationService);
  private tokens = inject(TokenService);

  rows = signal<AppNotification[]>([]);
  loading = signal(true);
  errorMessage = signal<string | null>(null);
  markingAll = signal(false);

  hasUnread = computed(() => this.rows().some((n) => n.status === 'UNREAD'));

  ngOnInit(): void {
    this.loadNotifications();
  }

  private loadNotifications(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    const user = this.tokens.user();
    const request$ = user
      ? this.notificationService.listByUser(user.userId)
      : this.notificationService.listAll();

    request$.subscribe({
      next: (rows) => {
        this.rows.set(rows || []);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(extractErrorMessage(err, 'Failed to load notifications.'));
        this.loading.set(false);
      },
    });
  }

  markAllRead(): void {
    if (this.markingAll() || !this.hasUnread()) return;
    this.markingAll.set(true);

    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.markingAll.set(false);
        this.rows.update((list) => list.map((n) => ({ ...n, status: 'READ' })));
      },
      error: (err) => {
        this.markingAll.set(false);
        this.errorMessage.set(extractErrorMessage(err, 'Mark all read failed.'));
      },
    });
  }

  markRead(notification: AppNotification): void {
    if (notification.status === 'READ') return;
    this.notificationService.markAsRead(notification.id).subscribe({
      next: (updated) => {
        this.rows.update((list) => list.map((row) => row.id === notification.id ? updated : row));
      },
      error: (err) => this.errorMessage.set(extractErrorMessage(err, 'Mark read failed.')),
    });
  }
}
