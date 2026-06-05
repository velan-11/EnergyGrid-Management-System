import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-toasts-container',
  imports: [CommonModule],
  template: `
    <div class="eg-toasts" aria-live="polite" aria-atomic="true">
      @for (toast of toasts.toasts(); track toast.id) {
        <div class="eg-toast"
             [class.error]="toast.tone === 'error'"
             [class.success]="toast.tone === 'success'"
             role="status">
          <span class="eg-toast-msg">{{ toast.message }}</span>
          <button class="eg-toast-close" type="button" aria-label="Dismiss"
                  (click)="toasts.remove(toast.id)">×</button>
        </div>
      }
    </div>
  `,
})
export class ToastsContainerComponent {
  toasts = inject(ToastService);
}
