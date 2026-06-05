import { Injectable, signal } from '@angular/core';

export type ToastTone = 'success' | 'error' | 'info';

export interface ToastItem {
  id: number;
  message: string;
  tone: ToastTone;
  duration: number;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private nextId = 1;
  toasts = signal<ToastItem[]>([]);

  success(message: string, duration = 3500): void {
    this.show(message, 'success', duration);
  }

  error(message: string, duration = 5000): void {
    this.show(message, 'error', duration);
  }

  info(message: string, duration = 3500): void {
    this.show(message, 'info', duration);
  }

  remove(id: number): void {
    this.toasts.update((list) => list.filter((t) => t.id !== id));
  }

  clearAll(): void {
    this.toasts.set([]);
  }

  private show(message: string, tone: ToastTone, duration: number): void {
    const id = this.nextId++;
    this.toasts.update((list) => [...list, { id, message, tone, duration }]);
    if (duration > 0) {
      setTimeout(() => this.remove(id), duration);
    }
  }
}
