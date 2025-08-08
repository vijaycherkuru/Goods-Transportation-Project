import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef } from '@angular/core';
import { WebSocketService } from '../services/websocket.service';

@Component({
  selector: 'app-search-ride',
  templateUrl: './search-ride.html',
  styleUrls: ['./search-ride.scss'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule]
})
export class SearchRide implements OnInit {
  searchForm: FormGroup;
  submitted = false;
  loading = false;
  searchResults: any[] = [];
  searchError = '';
  selectedRide: any = null;
  goodsForm: FormGroup;
  bookingLoading = false;
  bookingSuccess = false;
  bookingError = '';
  lastSearchParams: any = null;

  luggageSpaceOptions = [
    { value: 'small', label: '0-249 ltrs (Small)' },
    { value: 'medium', label: '250-499 ltrs (Medium)' },
    { value: 'large', label: '500-750 ltrs (Large)' }
  ];

  goodsTypeOptions = [
    'DOCUMENTS',
    'ELECTRONICS',
    'PERISHABLES',
    'FURNITURE',
    'CLOTHING',
    'MACHINERY',
    'OTHER'
  ];

  constructor(
    private fb: FormBuilder, 
    private http: HttpClient, 
    private cdr: ChangeDetectorRef,
    public webSocketService: WebSocketService
  ) {
    this.searchForm = this.fb.group({
      from: ['', Validators.required],
      to: ['', Validators.required],
      date: ['', Validators.required],
      luggageSpace: ['', Validators.required],
      goodsWeightInKg: ['', [Validators.required, Validators.min(0.1)]],
      goodsQuantity: ['', [Validators.required, Validators.min(1)]]
    });
    this.goodsForm = this.fb.group({
      goodsType: ['', Validators.required],
      goodsDescription: ['', Validators.required],
      specialInstructions: ['']
    });
  }

  ngOnInit() {
    // Connect to WebSocket for real-time notifications
    const userId = localStorage.getItem('userId');
    if (userId) {
      this.webSocketService.connect(userId);
    }
  }

  getCurrentUserId(): string | null {
    return localStorage.getItem('userId');
  }

  get f() { return this.searchForm.controls; }

  onSubmit() {
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

  onSelectRide(ride: any) {
    this.selectedRide = ride;
    this.goodsForm.reset();
    this.bookingSuccess = false;
    this.bookingError = '';
    this.cdr.detectChanges();
  }

  onBook() {
    if (!this.selectedRide || this.goodsForm.invalid || !this.lastSearchParams) return;
    this.bookingLoading = true;
    this.bookingSuccess = false;
    this.bookingError = '';
    const senderUserId = localStorage.getItem('userId');
    
    console.log('🔍 Booking Debug Info:');
    console.log('Sender User ID:', senderUserId);
    console.log('Ride User ID:', this.selectedRide.rideUserId || this.selectedRide.userId);
    console.log('Ride ID:', this.selectedRide.rideId || this.selectedRide.id);
    console.log('WebSocket Status:', this.webSocketService.getConnectionStatus());
    
    // Convert date to ISO string format for LocalDateTime
    const deliveryDate = new Date(this.lastSearchParams.date + 'T00:00:00').toISOString();
    
    const payload = {
      senderUserId,
      rideUserId: this.selectedRide.rideUserId || this.selectedRide.userId,
      rideId: this.selectedRide.rideId || this.selectedRide.id,
      goodsDescription: this.goodsForm.value.goodsDescription,
      goodsType: this.goodsForm.value.goodsType,
      weight: this.lastSearchParams.goodsWeightInKg,
      goodsQuantity: this.lastSearchParams.goodsQuantity,
      requiredSpace: (this.lastSearchParams.luggageSpace || '').toUpperCase(),
      from: this.lastSearchParams.from,
      to: this.lastSearchParams.to,
      fare: this.selectedRide.estimatedFare || this.selectedRide.fare,
      specialInstructions: this.goodsForm.value.specialInstructions,
      deliveryDate: deliveryDate
    };
    
    console.log('📤 Sending booking payload:', payload);
    
    this.http.post('http://localhost:8087/api/v1/requests', payload).subscribe({
      next: (response) => {
        console.log('✅ Booking successful:', response);
        this.bookingLoading = false;
        this.bookingSuccess = true;
        this.selectedRide = null;
        this.goodsForm.reset();
        this.cdr.detectChanges();
        
        // The backend will automatically send WebSocket notifications to the rider
        console.log('🎯 WebSocket notifications should be sent automatically to rider ID:', payload.rideUserId);
        
        // Test notification after booking
        setTimeout(() => {
          console.log('🧪 Testing notification after booking...');
          this.webSocketService.testNotification(payload.rideUserId);
        }, 2000);
      },
      error: (err) => {
        console.error('❌ Booking failed:', err);
        this.bookingLoading = false;
        this.bookingError = 'Booking failed. Please try again.';
        this.cdr.detectChanges();
      }
    });
  }
} 