import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { TokenService } from '../services/token.service';
import { UserRole } from '../models/auth.models';

export function roleGuard(allowedRoles: UserRole[]): CanActivateFn {
  return () => {
    const tokens = inject(TokenService);
    const router = inject(Router);

    if (!tokens.isLoggedIn()) {
      router.navigate(['/login']);
      return false;
    }

    const role = tokens.getRole() as UserRole;

    if (allowedRoles.includes(role)) {
      return true;
    }

    router.navigate(['/app/dashboard']);
    return false;
  };
}
