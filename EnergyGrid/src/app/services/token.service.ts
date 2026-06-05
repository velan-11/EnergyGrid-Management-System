import { Injectable, signal } from '@angular/core';
import { jwtDecode } from 'jwt-decode';
import { AuthenticatedUser, UserRole } from '../models/auth.models';

const TOKEN_KEY = 'eg_token';
const USER_KEY = 'eg_user';
const KEEP_KEY = 'eg_keep_signed_in';

@Injectable({ providedIn: 'root' })
export class TokenService {
  user = signal<AuthenticatedUser | null>(this.readUserFromStorage());

  setToken(token: string, keepSignedIn = false): void {
    this.removeToken();
    const store = keepSignedIn ? localStorage : sessionStorage;
    store.setItem(TOKEN_KEY, token);
    if (keepSignedIn) {
      localStorage.setItem(KEEP_KEY, 'true');
    }
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY);
  }

  removeToken(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    localStorage.removeItem(KEEP_KEY);
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(USER_KEY);
    this.user.set(null);
  }

  setUser(user: AuthenticatedUser): void {
    const store = localStorage.getItem(TOKEN_KEY) ? localStorage : sessionStorage;
    store.setItem(USER_KEY, JSON.stringify(user));
    this.user.set(user);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getExpiryMs(): number | null {
    const token = this.getToken();
    if (!token) return null;
    try {
      const decoded: any = jwtDecode(token);
      return decoded.exp ? decoded.exp * 1000 : null;
    } catch {
      return null;
    }
  }

  isExpired(): boolean {
    const expiry = this.getExpiryMs();
    if (expiry === null) return false;
    return Date.now() >= expiry;
  }

  isValid(): boolean {
    return this.isLoggedIn() && !this.isExpired();
  }

  getRole(): UserRole | '' {
    const user = this.user();
    return user ? user.role : '';
  }

  isPersistent(): boolean {
    return localStorage.getItem(KEEP_KEY) === 'true';
  }

  private readUserFromStorage(): AuthenticatedUser | null {
    const raw = localStorage.getItem(USER_KEY) || sessionStorage.getItem(USER_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw);
    } catch {
      return null;
    }
  }
}
