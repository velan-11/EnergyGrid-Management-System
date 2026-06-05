import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AbstractControl, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { ToastService } from '../../../services/toast.service';

function passwordsMustMatch(group: AbstractControl) {
  const newPassword = group.get('newPassword')?.value;
  const confirmPassword = group.get('confirmPassword')?.value;
  if (newPassword && confirmPassword && newPassword !== confirmPassword) {
    return { mismatch: true };
  }
  return null;
}

@Component({
  selector: 'app-login',
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent implements OnInit {
  private formBuilder = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private toast = inject(ToastService);

  view = signal<'login' | 'forgot' | 'reset'>('login');
  loading = signal(false);

  loginForm = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    remember: [false],
  });

  forgotForm = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
  });

  resetForm = this.formBuilder.nonNullable.group(
    {
      token: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required, Validators.minLength(8)]],
    },
    { validators: passwordsMustMatch },
  );

  ngOnInit(): void {
    if (this.auth.isAuthenticated()) {
      this.router.navigate(['/app/dashboard'], { replaceUrl: true });
      return;
    }
    this.prefillTokenFromQueryParam();
  }

  private prefillTokenFromQueryParam(): void {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (token) {
      this.resetForm.patchValue({ token });
      this.view.set('reset');
    }
  }

  showLogin(): void {
    this.view.set('login');
  }

  showForgot(): void {
    this.view.set('forgot');
  }

  showReset(): void {
    this.view.set('reset');
  }

  hasError(controlName: string): boolean {
    const control = this.loginForm.get(controlName);
    return !!(control?.invalid && control?.touched);
  }

  getError(controlName: string): string {
    const control = this.loginForm.get(controlName);
    if (!control?.errors || !control.touched) return '';
    if (control.errors['required']) return `${controlName.charAt(0).toUpperCase() + controlName.slice(1)} is required`;
    if (control.errors['email']) return 'Enter a valid email address';
    if (control.errors['minlength']) return `${controlName.charAt(0).toUpperCase() + controlName.slice(1)} must be at least ${control.errors['minlength'].requiredLength} characters`;
    return '';
  }

  submitLogin(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    const { email, password, remember } = this.loginForm.getRawValue();
    const credentials = { email: email.trim(), password };

    this.auth.login(credentials, remember).subscribe({
      next: (response) => {
        this.loading.set(false);
        this.toast.success('Welcome back, ' + response.name + '!');
        this.router.navigate(['/app/dashboard'], { replaceUrl: true });
      },
      error: (err) => {
        this.loading.set(false);
        this.toast.error(this.readError(err, 'Login failed. Check your credentials.'));
      },
    });
  }

  submitForgot(): void {
    if (this.forgotForm.invalid) {
      this.forgotForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    const email = this.forgotForm.getRawValue().email;

    this.auth.forgetPassword(email).subscribe({
      next: (message) => {
        this.loading.set(false);
        // Backend returns the OTP in the message while SMTP is not configured (dev mode).
        this.toast.success(message || 'OTP sent. Check your email.');
        this.showReset();
      },
      error: (err) => {
        this.loading.set(false);
        this.toast.error(this.readError(err, 'Could not send reset email.'));
      },
    });
  }

  submitReset(): void {
    if (this.resetForm.invalid) {
      this.resetForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.auth.resetPassword(this.resetForm.getRawValue()).subscribe({
      next: (message) => {
        this.loading.set(false);
        this.toast.success(message || 'Password reset. You can sign in now.');
        this.resetForm.reset();
        this.showLogin();
      },
      error: (err) => {
        this.loading.set(false);
        this.toast.error(this.readError(err, 'Could not reset password.'));
      },
    });
  }

  private readError(err: any, fallback: string): string {
    if (err?.error?.message) return err.error.message;
    if (err?.error?.error) return err.error.error;
    if (typeof err?.error === 'string') return err.error;
    return fallback;
  }
}
