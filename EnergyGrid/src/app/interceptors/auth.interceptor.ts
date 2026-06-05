import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { TokenService } from '../services/token.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const tokens = inject(TokenService);
  const token = tokens.getToken();

  if (!token) return next(req);

  const authorizedRequest = req.clone({
    headers: req.headers.set('Authorization', 'Bearer ' + token),
  });

  return next(authorizedRequest);
};
