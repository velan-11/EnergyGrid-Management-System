import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { TokenService } from './token.service';
import { ToastService } from './toast.service';
import { NotificationService } from './notification.service';
import {
  LoginRequest, LoginResponse, RegisterRequest, RegisterResponse,
  ResetPasswordRequest, UserRole,
} from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private tokens = inject(TokenService);
  private router = inject(Router);
  private toast = inject(ToastService);
  private notifications = inject(NotificationService);

  private baseUrl = environment.apiUrl + '/api/auth';
  private autoLogoutTimer: any = null;
  authenticated = signal<boolean>(this.tokens.isValid());

  constructor() {
    if (this.tokens.isLoggedIn() && this.tokens.isExpired()) {
      this.expireSession();
    } else {
      this.scheduleAutoLogout();
    }
  }

  login(credentials: LoginRequest, keepSignedIn = false): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(this.baseUrl + '/login', credentials).pipe(
      tap((response) => this.handleLoginSuccess(response, keepSignedIn)),
    );
  }

  private handleLoginSuccess(response: LoginResponse, keepSignedIn: boolean): void {
    this.tokens.setToken(response.accessToken, keepSignedIn);
    this.tokens.setUser({
      userId: response.userId,
      username: response.username,
      name: response.name,
      role: response.role,
      status: response.status,
    });
    this.authenticated.set(true);
    this.scheduleAutoLogout();
  }

  register(body: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(this.baseUrl + '/register', body);
  }

  forgetPassword(email: string): Observable<string> {
    return this.http.post(this.baseUrl + '/forget-password', { email }, { responseType: 'text' });
  }

  resetPassword(body: ResetPasswordRequest): Observable<string> {
    return this.http.post(this.baseUrl + '/reset-password', body, { responseType: 'text' });
  }

  forgetUsername(email: string): Observable<any> {
    return this.http.post<any>(this.baseUrl + '/forget-username', { email });
  }

  isAuthenticated(): boolean {
    if (!this.tokens.isLoggedIn()) {
      this.authenticated.set(false);
      return false;
    }
    if (this.tokens.isExpired()) {
      this.expireSession();
      return false;
    }
    this.authenticated.set(true);
    return true;
  }

  logout(reason?: string): void {
    this.clearTimer();
    this.notifications.stopPolling();
    this.tokens.removeToken();
    this.authenticated.set(false);
    this.toast.clearAll();
    if (reason) this.toast.info(reason);
    this.router.navigate(['/login'], { replaceUrl: true });
  }

  isLoggedIn(): boolean {
    return this.isAuthenticated();
  }

  getRole(): UserRole | '' {
    return this.tokens.getRole();
  }

  expireSession(): void {
    this.clearTimer();
    this.notifications.stopPolling();
    this.tokens.removeToken();
    this.authenticated.set(false);
    this.toast.clearAll();
    this.toast.error('Your session has expired. Please sign in again.');
    this.router.navigate(['/login'], { replaceUrl: true });
  }

  private scheduleAutoLogout(): void {
    this.clearTimer();
    const expiryMs = this.tokens.getExpiryMs();
    if (expiryMs === null) return;
    const delay = Math.max(0, expiryMs - Date.now());
    const safeDelay = Math.min(delay, 24 * 60 * 60 * 1000);
    this.autoLogoutTimer = setTimeout(() => this.expireSession(), safeDelay);
  }

  private clearTimer(): void {
    if (this.autoLogoutTimer !== null) {
      clearTimeout(this.autoLogoutTimer);
      this.autoLogoutTimer = null;
    }
  }
}
