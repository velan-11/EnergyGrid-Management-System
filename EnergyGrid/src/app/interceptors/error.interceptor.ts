import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';
import { TokenService } from '../services/token.service';

// Intercepts every HTTP response and handles errors globally
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const toast = inject(ToastService);
  const tokens = inject(TokenService);

  // These custom headers let specific requests silence error toasts
  const silentAll = req.headers.has('X-Eg-Silent-Errors');
  const silent403 = req.headers.has('X-Eg-Silent-403');

  return next(req).pipe(
    catchError((err: any) => {
      // Don't show errors if the user isn't logged in
      if (!tokens.isLoggedIn()) {
        return throwError(() => err);
      }

      const isAuthEndpoint = req.url.includes('/api/auth/');
      const status: number = err.status;

      // 401 on a non-auth endpoint means the session expired
      if (status === 401 && !isAuthEndpoint) {
        auth.expireSession();
        return throwError(() => err);
      }

      // Show a toast unless this request asked to be silent
      if (!silentAll) {
        const message = getBackendMessage(err);

        if (status === 403 && !silent403) {
          toast.error(message || 'You do not have permission to perform this action.');
        } else if (status === 0) {
          toast.error('Could not reach the server. Check your connection.');
        } else if (status >= 500) {
          toast.error(message || 'Server error. Please try again.');
        } else if (status === 400 || status === 404 || status === 409 || status === 422) {
          toast.error(message || 'Request failed.');
        }
      }

      return throwError(() => err);
    }),
  );
};

// Extracts a human-readable message from the error response body
function getBackendMessage(err: any): string | null {
  const body = err?.error;
  if (!body) return null;
  if (typeof body === 'string') return body;
  return body.message || body.error || null;
}
