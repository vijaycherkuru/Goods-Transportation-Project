import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
// @ts-ignore
import FOG from 'vanta/dist/vanta.fog.min';
// @ts-ignore
import * as THREE from 'three';
import { AfterViewInit, OnDestroy } from '@angular/core';

@Component({
  selector: 'app-sign-up',
  templateUrl: './sign-up.html',
  styleUrl: './sign-up.scss',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, HttpClientModule]
})
export class SignUp implements AfterViewInit, OnDestroy {
  signupForm: FormGroup;
  otpSent = false;
  otpVerified = false;
  otpError = '';
  otpSentMessage = '';
  otpVerifiedMessage = '';
  public currentYear = new Date().getFullYear();

  loadingVerify = false;
  loadingOtp = false;
  loadingRegister = false;

  // Multi-step form properties
  formStep = 1;
  showPassword = false;
  passwordStrengthClass = '';
  passwordStrengthText = '';

  private vantaEffect: any;

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
    private router: Router
  ) {
    this.signupForm = this.fb.group({
      name: ['', Validators.required],
      age: ['', [Validators.required, Validators.min(1)]],
      gender: ['MALE', Validators.required],
      phone: ['', [Validators.required, Validators.pattern(/^\d{10,15}$/)]],
      email: ['', [Validators.required, Validators.email]],
      otp: [''],
      password: ['', Validators.required],
      street: ['', Validators.required],
      city: ['', Validators.required],
      state: ['', Validators.required],
      zipCode: ['', Validators.required],
      country: ['', Validators.required],
    });

    // Watch password changes for strength indicator
    this.signupForm.get('password')?.valueChanges.subscribe(password => {
      this.updatePasswordStrength(password);
    });
  }

  ngAfterViewInit() {
    this.vantaEffect = FOG({
      el: '#vanta-bg-signup',
      THREE: THREE,
      highlightColor: 0xffffff,
      midtoneColor: 0x1976d2,
      lowlightColor: 0x000000,
      baseColor: 0x1976d2,
      blurFactor: 0.5,
      speed: 1.5,
      zoom: 1
    });
  }

  ngOnDestroy() {
    if (this.vantaEffect) {
      this.vantaEffect.destroy();
    }
  }

  // Multi-step form navigation
  nextStep() {
    if (this.canProceedToNextStep()) {
      this.formStep++;
      this.cdr.detectChanges();
    }
  }

  previousStep() {
    if (this.formStep > 1) {
      this.formStep--;
      this.cdr.detectChanges();
    }
  }

  canProceedToNextStep(): boolean {
    switch (this.formStep) {
      case 1:
        return !!(this.signupForm.get('name')?.valid && 
               this.signupForm.get('age')?.valid && 
               this.signupForm.get('phone')?.valid);
      case 2:
        return this.otpVerified;
      default:
        return false;
    }
  }

  // Password visibility toggle
  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
    const passwordInput = document.getElementById('password') as HTMLInputElement;
    if (passwordInput) {
      passwordInput.type = this.showPassword ? 'text' : 'password';
    }
  }

  // Password strength indicator
  updatePasswordStrength(password: string) {
    if (!password) {
      this.passwordStrengthClass = '';
      this.passwordStrengthText = '';
      return;
    }

    let strength = 0;
    let feedback = [];

    if (password.length >= 8) strength++;
    else feedback.push('At least 8 characters');

    if (/[a-z]/.test(password)) strength++;
    else feedback.push('Lowercase letter');

    if (/[A-Z]/.test(password)) strength++;
    else feedback.push('Uppercase letter');

    if (/[0-9]/.test(password)) strength++;
    else feedback.push('Number');

    if (/[^A-Za-z0-9]/.test(password)) strength++;
    else feedback.push('Special character');

    switch (strength) {
      case 0:
      case 1:
        this.passwordStrengthClass = 'strength-weak';
        this.passwordStrengthText = 'Very Weak';
        break;
      case 2:
        this.passwordStrengthClass = 'strength-weak';
        this.passwordStrengthText = 'Weak';
        break;
      case 3:
        this.passwordStrengthClass = 'strength-medium';
        this.passwordStrengthText = 'Medium';
        break;
      case 4:
        this.passwordStrengthClass = 'strength-strong';
        this.passwordStrengthText = 'Strong';
        break;
      case 5:
        this.passwordStrengthClass = 'strength-very-strong';
        this.passwordStrengthText = 'Very Strong';
        break;
    }
  }

  verifyEmail() {
    const email = this.signupForm.get('email')?.value;
    if (!email) return;
    this.loadingVerify = true;
    this.otpSentMessage = '';
    this.http.post<any>('http://localhost:8081/api/users/request-otp', { email }).subscribe({
      next: (res) => {
        console.log('OTP request success:', res);
        this.otpSent = true;
        this.loadingVerify = false;
        this.otpSentMessage = 'OTP sent to the given email id.';
        this.cdr.detectChanges();
        // No alert here
      },
      error: (err) => {
        console.log('OTP request error:', err);
        this.loadingVerify = false;
        this.otpSentMessage = 'Failed to send OTP: ' + (err?.error?.message || 'Unknown error');
        this.cdr.detectChanges();
      }
    });
  }

  validateOtp() {
    const email = this.signupForm.get('email')?.value;
    const otp = this.signupForm.get('otp')?.value;
    if (!otp || !email) return;
    this.loadingOtp = true;
    this.http.post('http://localhost:8081/api/users/verify-otp', { email, otp }).subscribe({
      next: () => {
        this.otpVerified = true;
        this.otpError = '';
        this.loadingOtp = false;
        this.otpVerifiedMessage = 'Email verified successfully.';
        this.cdr.detectChanges();
        setTimeout(() => {
          this.otpSent = false;
          this.otpVerifiedMessage = '';
          this.cdr.detectChanges();
        }, 2000);
      },
      error: (err) => {
        this.loadingOtp = false;
        this.otpError = err?.error?.message || 'Invalid OTP. Please try again.';
        this.cdr.detectChanges();
      }
    });
  }

  onSubmit() {
    if (this.signupForm.invalid || !this.otpVerified) {
      this.signupForm.markAllAsTouched();
      return;
    }
    const formValue = { ...this.signupForm.value };
    delete formValue.otp;
    // Add timestamp in ISO format
    formValue.timestamp = new Date().toISOString();
    this.loadingRegister = true;
    this.http.post('http://localhost:8081/api/users/register', formValue).subscribe({
      next: () => {
        this.loadingRegister = false;
        // Clear OTP sent message after registration
        this.otpSentMessage = '';
        alert('Registration successful!');
        this.signupForm.reset();
        this.otpSent = false;
        this.otpVerified = false;
      },
      error: (err) => {
        this.loadingRegister = false;
        alert('Registration failed: ' + (err?.error?.message || 'Unknown error'));
      }
    });
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }
}
