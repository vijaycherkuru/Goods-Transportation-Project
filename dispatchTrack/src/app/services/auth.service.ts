import { Injectable, Provider } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HTTP_INTERCEPTORS } from '@angular/common/http';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: any;
  message: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ForgotPasswordResponse {
  message: string;
  success: boolean;
}

export interface UserProfile {
  name: string;
  email: string;
  phone: string;
  age: number;
  gender: string;
  street: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseUrl = 'http://localhost:8081/api';

  constructor(private http: HttpClient) { }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    // Map frontend email field to backend emailOrPhone field
    const backendRequest = {
      emailOrPhone: credentials.email,
      password: credentials.password
    };
    return this.http.post<LoginResponse>(`${this.baseUrl}/auth/login`, backendRequest);
  }

  forgotPassword(request: ForgotPasswordRequest): Observable<ForgotPasswordResponse> {
    return this.http.post<ForgotPasswordResponse>(`${this.baseUrl}/users/forgot-password`, request);
  }

  getProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.baseUrl}/auth/profile`);
  }

  updateProfile(profile: Partial<UserProfile>): Observable<UserProfile> {
    return this.http.put<UserProfile>(`${this.baseUrl}/auth/profile`, profile);
  }

  // Store token in localStorage
  setToken(token: string): void {
    localStorage.setItem('authToken', token);
    console.log('Token set in localStorage:', token);
  }

  // Get token from localStorage
  getToken(): string | null {
    return localStorage.getItem('authToken');
  }

  // Remove token from localStorage
  removeToken(): void {
    localStorage.removeItem('authToken');
  }

  // Check if user is logged in
  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  // Logout method for auto logout
  logout(): void {
    console.log('Clearing authentication data...');
    this.removeToken();
    localStorage.clear();
    sessionStorage.clear();
    console.log('Authentication data cleared successfully');
  }
} 