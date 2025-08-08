import { Component, AfterViewInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule, NgIf } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthService, LoginRequest, ForgotPasswordRequest } from '../services/auth.service';
import { gsap } from 'gsap';

@Component({
  selector: 'app-login',
  templateUrl: './login.html',
  styleUrl: './login.scss',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, HttpClientModule, NgIf]
})
export class Login implements AfterViewInit, OnDestroy {
  loginForm: FormGroup;
  forgotPasswordForm: FormGroup;
  showForgotPassword = false;
  loadingLogin = false;
  loadingForgotPassword = false;
  loginError = '';
  forgotPasswordMessage = '';
  public currentYear = new Date().getFullYear();
  showPassword = false;
  @ViewChild('loginCard') loginCard!: ElementRef;
  @ViewChild('loginSlogan') loginSlogan!: ElementRef;
  // Only single line slogan now

  ngAfterViewInit() {
    // Animate login card with GSAP
    if (this.loginCard) {
      gsap.from(this.loginCard.nativeElement, { opacity: 0, y: 40, duration: 1, ease: 'power3.out' });
    }

    // Single line slogan animation
    const slogan = 'your pick of rides at low prices';
    const el = this.loginSlogan?.nativeElement;
    if (el) {
      el.innerHTML = '';
      function typeWriterSlogan() {
        // Typewriter animation sequence for two sentences
        const container = el;
        container.innerHTML = '';

        // First sentence
        const before1 = 'Dispatch Your Goods at ';
        const red1 = 'Low Prices';
        const after1 = '';

        // Second sentence
        const before2 = 'Deliver More, Pay ';
        const red2 = 'Less';
        const after2 = '';

        function typeSentence(before: string, red: string, after: string, callback: () => void) {
          container.innerHTML = '';
          const beforeSpan = document.createElement('span');
          const redSpan = document.createElement('span');
          redSpan.className = 'red-word';
          const afterSpan = document.createElement('span');
          container.appendChild(beforeSpan);
          container.appendChild(redSpan);
          container.appendChild(afterSpan);

          let i = 0, j = 0, k = 0;
          function typeLetter() {
            if (i < before.length) {
              beforeSpan.innerHTML += before[i] === ' ' ? '&nbsp;' : before[i];
              i++;
              setTimeout(typeLetter, 40);
            } else if (j < red.length) {
              redSpan.innerHTML += red[j] === ' ' ? '&nbsp;' : red[j];
              j++;
              setTimeout(typeLetter, 40);
            } else if (k < after.length) {
              afterSpan.innerHTML += after[k] === ' ' ? '&nbsp;' : after[k];
              k++;
              setTimeout(typeLetter, 40);
            } else {
              setTimeout(callback, 1200); // Pause before next sentence
            }
          }
          typeLetter();
        }

        // Sequence: first sentence, then fade out, then second sentence
        typeSentence(before1, red1, after1, () => {
          container.style.transition = 'opacity 0.5s';
          container.style.opacity = '0';
          setTimeout(() => {
            container.style.opacity = '1';
            typeSentence(before2, red2, after2, () => {});
          }, 1000);
        });
      }
      typeWriterSlogan();
    }
  }

  ngOnDestroy() {
    // No Vanta cleanup needed
  }

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });

    this.forgotPasswordForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onSubmit() {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.loadingLogin = true;
    this.loginError = '';

    const credentials: LoginRequest = {
      email: this.loginForm.get('email')?.value,
      password: this.loginForm.get('password')?.value
    };

    this.authService.login(credentials).subscribe({
      next: (response: any) => {
        console.log(response);
        this.loadingLogin = false;
        // Extract from response.data
        const data = response.data || {};
        this.authService.setToken(data.token);
        localStorage.setItem('username', data.username);
        localStorage.setItem('userId', data.userId);
        localStorage.setItem('email',data.email);
        // Navigate to dashboard or home page
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loadingLogin = false;
        this.loginError = err?.error?.message || 'Login failed. Please check your credentials.';
      }
    });
  }

  onForgotPassword() {
    if (this.forgotPasswordForm.invalid) {
      this.forgotPasswordForm.markAllAsTouched();
      return;
    }

    this.loadingForgotPassword = true;
    this.forgotPasswordMessage = '';

    const request: ForgotPasswordRequest = {
      email: this.forgotPasswordForm.get('email')?.value
    };

    this.authService.forgotPassword(request).subscribe({
      next: (response) => {
        this.loadingForgotPassword = false;
        this.forgotPasswordMessage = response.message || 'Password reset instructions sent to your email.';
        // Hide forgot password form after 3 seconds
        setTimeout(() => {
          this.showForgotPassword = false;
          this.forgotPasswordMessage = '';
        }, 3000);
      },
      error: (err) => {
        this.loadingForgotPassword = false;
        this.forgotPasswordMessage = err?.error?.message || 'Failed to send password reset email.';
      }
    });
  }

  toggleForgotPassword() {
    this.showForgotPassword = !this.showForgotPassword;
    this.forgotPasswordMessage = '';
    if (this.showForgotPassword) {
      // Pre-fill email from login form
      const loginEmail = this.loginForm.get('email')?.value;
      if (loginEmail) {
        this.forgotPasswordForm.patchValue({ email: loginEmail });
      }
    }
  }

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  goToSignUp() {
    this.router.navigate(['/sign-up']);
  }
} 