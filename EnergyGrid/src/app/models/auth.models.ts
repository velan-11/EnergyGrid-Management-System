export type UserRole = 'ADMIN' | 'OPERATOR' | 'TECHNICIAN' | 'PRODUCER' | 'CUSTOMER' | 'AUDITOR';
export type UserStatus = 'PENDING' | 'ACTIVE' | 'INACTIVE' | 'DELETED';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  userId: number;
  username: string;
  name: string;
  role: UserRole;
  status: UserStatus;
}

export interface RegisterRequest {
  name: string;
  username: string;
  email: string;
  phone: string;
  password: string;
  role: UserRole;
}

export interface RegisterResponse {
  message: string;
  userID: number;
  username: string;
  name: string;
  email: string;
  role: UserRole;
  status: UserStatus;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
  confirmPassword: string;
}

export interface AuthenticatedUser {
  userId: number;
  username: string;
  name: string;
  role: UserRole;
  status: UserStatus;
}

export interface AdminUser {
  userId: number;
  name: string;
  username: string;
  email: string;
  phone?: string;
  role: UserRole;
  status: UserStatus;
  createdAt?: string;
  deleted?: boolean;
}

export interface AuditLogEntry {
  auditId: number;
  userId: number;
  name: string;
  action: string;
  resourceType: string;
  resourceId: number;
  details: string;
  timestamp: string;
  serviceName?: string;
}
