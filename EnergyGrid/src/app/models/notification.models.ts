export type NotificationType = 'INFO' | 'WARNING' | 'ALERT' | 'SUCCESS';
export type NotificationSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type NotificationStatus = 'READ' | 'UNREAD';

export interface AppNotification {
  id: number;
  userId: number;
  entityId: number;
  title?: string;
  type?: string;
  relatedEntityType?: string;
  expiresAt?: string | null;
  message: string;
  category: string;
  severity: string;
  status: string;
  createdAt: string;
}

export interface NotificationRequest {
  userId: number;
  entityId: number;
  message: string;
  category: string;
  severity: string;
  title?: string;
  type?: NotificationType;
  relatedEntityType?: string;
}
