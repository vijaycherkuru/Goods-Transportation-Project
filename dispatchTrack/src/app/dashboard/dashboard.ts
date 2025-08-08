import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { NgParticlesModule } from 'ng-particles';
import { Container, Engine, ISourceOptions } from 'tsparticles-engine';
import { loadSnowPreset } from 'tsparticles-preset-snow';
import { gsap } from 'gsap';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef } from '@angular/core';
import { WebSocketService } from '../services/websocket.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
  standalone: true,
  imports: [CommonModule, RouterLink, NgParticlesModule, ReactiveFormsModule]
})
export class Dashboard implements OnInit, AfterViewInit {
  // User Data
  public username: string | null = null;
  
  // Dashboard Stats
  public totalDeliveries: number = 0;
  public activeDeliveries: number = 0;
  public userRating: number = 0;

  // Search Functionality
  public searchForm: FormGroup;
  public submitted = false;
  public loading = false;
  public searchResults: any[] = [];
  public searchError = '';
  public selectedRide: any = null;
  public goodsForm: FormGroup;
  public bookingLoading = false;
  public bookingSuccess = false;
  public bookingError = '';
  public lastSearchParams: any = null;

  // Options
  public luggageSpaceOptions = [
    { value: 'small', label: '0-249 ltrs (Small)' },
    { value: 'medium', label: '250-499 ltrs (Medium)' },
    { value: 'large', label: '500-750 ltrs (Large)' }
  ];

  public goodsTypeOptions = [
    'DOCUMENTS',
    'ELECTRONICS',
    'PERISHABLES',
    'FURNITURE',
    'CLOTHING',
    'MACHINERY',
    'OTHER'
  ];

  // Particles Configuration
  public particlesOptions: ISourceOptions = {
    preset: 'snow',
    background: { color: 'transparent' },
    fullScreen: { enable: false },
    particles: {
      color: { value: '#fff' },
      opacity: { value: 0.7 },
      size: { value: { min: 2, max: 5 } },
      move: { speed: 1 }
    }
  };

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
    public webSocketService: WebSocketService
  ) {
    this.username = localStorage.getItem('username');
    
    // Initialize search form - BlaBlaCar Style
    this.searchForm = this.fb.group({
      from: ['', Validators.required],
      to: ['', Validators.required],
      date: ['', Validators.required],
      luggageSpace: ['', Validators.required],
      goodsWeightInKg: ['', [Validators.required, Validators.min(0.1)]],
      goodsQuantity: ['', [Validators.required, Validators.min(1)]]
    });

    // Initialize goods form
    this.goodsForm = this.fb.group({
      goodsType: ['', Validators.required],
      description: ['', Validators.required]
    });

    // Load user stats
    this.loadUserStats();
  }

  ngOnInit() {
    // Connect to WebSocket for real-time notifications
    const userId = localStorage.getItem('userId');
    if (userId) {
      this.webSocketService.connect(userId);
    }
  }

  async particlesInit(engine: Engine): Promise<void> {
    await loadSnowPreset(engine);
  }

  ngAfterViewInit() {
    // Animate username characters
    gsap.fromTo(
      '#welcome-username span',
      { y: 40, opacity: 0 },
      { y: 0, opacity: 1, duration: 0.7, stagger: 0.07, ease: 'power2.out' }
    );

    // Animate stat cards
    gsap.fromTo(
      '.stat-card',
      { y: 30, opacity: 0 },
      { y: 0, opacity: 1, duration: 0.8, stagger: 0.1, ease: 'power2.out', delay: 0.5 }
    );
  }

  // Form getters
  get f() { return this.searchForm.controls; }

  // Load user statistics
  private loadUserStats(): void {
    const userId = localStorage.getItem('userId');
    if (userId) {
      // Mock data for now - replace with actual API calls
      this.totalDeliveries = 24;
      this.activeDeliveries = 3;
      this.userRating = 4.8;
    }
  }

  // Search functionality
  onSubmit(): void {
    this.submitted = true;
    this.searchError = '';
    
    if (this.searchForm.invalid) {
      return;
    }

    this.loading = true;
    this.searchResults = [];
    this.selectedRide = null;
    this.lastSearchParams = { ...this.searchForm.value };

    // Convert form values to query params
    const params = new URLSearchParams();
    Object.entries(this.searchForm.value).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        if (key === 'luggageSpace') {
          params.append(key, String(value).toUpperCase());
        } else {
          params.append(key, String(value));
        }
      }
    });

    const url = `http://localhost:8086/api/v1/rides/search-with-fare?${params.toString()}`;
    
    this.http.get<any[]>(url).subscribe({
      next: (results) => {
        this.searchResults = results || [];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.searchError = 'Failed to fetch rides. Please try again.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  // Ride selection
  onSelectRide(ride: any): void {
    this.selectedRide = ride;
    this.goodsForm.reset();
    this.bookingSuccess = false;
    this.bookingError = '';
    this.cdr.detectChanges();
  }

  // Booking functionality
  onBook(): void {
    if (!this.selectedRide || this.goodsForm.invalid || !this.lastSearchParams) return;
    
    this.bookingLoading = true;
    this.bookingSuccess = false;
    this.bookingError = '';
    
    const senderUserId = localStorage.getItem('userId');
    
    // Convert date to ISO string format for LocalDateTime
    const deliveryDate = new Date(this.lastSearchParams.date + 'T00:00:00').toISOString();
    
    const payload = {
      senderUserId,
      rideUserId: this.selectedRide.rideUserId,
      rideId: this.selectedRide.rideId,
      goodsDescription: this.goodsForm.value.description,
      goodsType: this.goodsForm.value.goodsType,
      weight: this.lastSearchParams.goodsWeightInKg,
      goodsQuantity: this.lastSearchParams.goodsQuantity,
      requiredSpace: (this.lastSearchParams.luggageSpace || '').toUpperCase(),
      from: this.lastSearchParams.from,
      to: this.lastSearchParams.to,
      fare: this.selectedRide.estimatedFare,
      deliveryDate: deliveryDate
    };
    
    this.http.post('http://localhost:8087/api/v1/requests', payload).subscribe({
      next: (response) => {
        this.bookingLoading = false;
        this.bookingSuccess = true;
        this.selectedRide = null;
        this.goodsForm.reset();
        this.cdr.detectChanges();
        
        // Update stats
        this.activeDeliveries++;
        this.totalDeliveries++;
      },
      error: (err) => {
        this.bookingLoading = false;
        this.bookingError = 'Booking failed. Please try again.';
        this.cdr.detectChanges();
      }
    });
  }

  // Sorting functionality
  sortByPrice(): void {
    this.searchResults.sort((a, b) => {
      const priceA = a.estimatedFare || 0;
      const priceB = b.estimatedFare || 0;
      return priceA - priceB;
    });
  }

  sortByDate(): void {
    this.searchResults.sort((a, b) => {
      const dateA = new Date(a.date);
      const dateB = new Date(b.date);
      return dateA.getTime() - dateB.getTime();
    });
  }

  // Utility methods
  clearSearch(): void {
    this.searchForm.reset();
    this.searchResults = [];
    this.selectedRide = null;
    this.submitted = false;
    this.searchError = '';
  }

  retrySearch(): void {
    this.searchError = '';
    this.onSubmit();
  }

  getCurrentUserId(): string | null {
    return localStorage.getItem('userId');
  }
}