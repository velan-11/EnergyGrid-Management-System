import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { UserRole } from '../../../models/auth.models';

function nameValidator(control: AbstractControl): ValidationErrors | null {
  const value = control.value?.trim();
  if (!value) return null;
  const isValid = /^[A-Za-z][A-Za-z\s'.-]*$/.test(value);
  return isValid ? null : { invalidName: true };
}

function phoneValidator(control: AbstractControl): ValidationErrors | null {
  const value = control.value?.trim();
  if (!value) return null;
  const isValid = /^\d{10,}$/.test(value);
  return isValid ? null : { invalidPhone: true };
}

function passwordStrengthValidator(control: AbstractControl): ValidationErrors | null {
  const value = control.value;
  if (!value) return null;
  if (!/[A-Z]/.test(value)) return { uppercase: true };
  if (!/\d/.test(value)) return { digit: true };
  return null;
}

function passwordMatchValidator(group: AbstractControl): ValidationErrors | null {
  const password = group.get('password')?.value;
  const confirmPassword = group.get('confirmPassword')?.value;
  if (password && confirmPassword && password !== confirmPassword) {
    return { mismatch: true };
  }
  return null;
}

@Component({
  selector: 'app-register',
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css',
})
export class RegisterComponent {
  private formBuilder = inject(FormBuilder);
  private auth = inject(AuthService);

  loading = signal(false);
  successMessage = signal<string | null>(null);
  serverError = signal<string | null>(null);

  roles = [
    { value: 'OPERATOR', label: 'Operator' },
    { value: 'TECHNICIAN', label: 'Technician' },
    { value: 'PRODUCER', label: 'Producer' },
    { value: 'CUSTOMER', label: 'Customer' },
    { value: 'AUDITOR', label: 'Auditor' },
  ];

  form = this.formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2), nameValidator]],
    username: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    phone: ['', [Validators.required, Validators.minLength(10), phoneValidator]],
    password: ['', [Validators.required, Validators.minLength(8), passwordStrengthValidator]],
    confirmPassword: ['', [Validators.required]],
    role: ['' as UserRole | '', [Validators.required]],
  }, { validators: passwordMatchValidator });

  passwordStrength = computed(() => this.scorePassword(this.form.get('password')!.value || ''));

  hasError(field: string): boolean {
    const control = this.form.get(field);
    return !!(control?.invalid && control?.touched);
  }

  errorOf(field: string): string {
    const control = this.form.get(field);
    if (!control?.errors || !control.touched) return '';

    const fieldLabels: Record<string, string> = {
      name: 'Full name', username: 'Username', email: 'Email',
      phone: 'Phone', password: 'Password', confirmPassword: 'Confirm password', role: 'Role'
    };
    const label = fieldLabels[field] || field;

    if (control.errors['required']) return `${label} is required`;
    if (control.errors['minlength']) return `${label} must be at least ${control.errors['minlength'].requiredLength} characters`;
    if (control.errors['email']) return 'Enter a valid email address';
    if (control.errors['invalidName']) return 'Name can only contain letters and spaces';
    if (control.errors['invalidPhone']) return 'Enter a valid phone number (digits only)';
    if (control.errors['uppercase']) return 'Password must contain at least one uppercase letter';
    if (control.errors['digit']) return 'Password must contain at least one number';
    return '';
  }

  confirmPasswordError(): string {
    if (this.form.errors?.['mismatch'] && this.form.get('confirmPassword')?.touched) {
      return 'Passwords do not match';
    }
    return this.errorOf('confirmPassword');
  }

  hasConfirmPasswordError(): boolean {
    const control = this.form.get('confirmPassword');
    return !!(control?.touched && (control?.invalid || this.form.errors?.['mismatch']));
  }

  isNonCustomer(): boolean {
    const role = this.form.get('role')!.value;
    return !!role && role !== 'CUSTOMER';
  }

  private scorePassword(password: string) {
    if (!password) return { score: 0, label: '', color: '#e5e7eb' };
    let score = 0;
    if (password.length >= 8) score++;
    if (/[A-Z]/.test(password)) score++;
    if (/\d/.test(password)) score++;
    if (score <= 1) return { score: 1, label: 'Weak', color: '#ef4444' };
    if (score === 2) return { score: 2, label: 'Medium', color: '#f59e0b' };
    return { score: 3, label: 'Strong', color: '#10b981' };
  }

  submit(): void {
    this.serverError.set(null);
    this.successMessage.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    const values = this.form.getRawValue();

    this.auth.register({
      name: values.name.trim(),
      username: values.username.trim(),
      email: values.email.trim(),
      phone: values.phone.trim(),
      password: values.password,
      role: values.role as UserRole,
    }).subscribe({
      next: () => {
        this.loading.set(false);
        this.successMessage.set(this.buildSuccessMessage(values.role));
      },
      error: (err) => {
        this.loading.set(false);
        this.serverError.set(this.readError(err));
      },
    });
  }

  private buildSuccessMessage(role: string): string {
    if (role === 'CUSTOMER') {
      return 'Account created! You can now log in.';
    }
    return 'Registration submitted. Awaiting admin approval before you can log in.';
  }

  private readError(err: any): string {
    return (err?.error?.message) || (err?.error?.error) || 'Registration failed.';
  }
}
