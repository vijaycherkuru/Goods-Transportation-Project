import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-your-rides',
  templateUrl: './your-rides.html',
  styleUrls: ['./your-rides.scss'],
  standalone: true,
  imports: [CommonModule]
})
export class YourRides implements OnInit {
  rides: any[] = [];
  loading = false;
  error = '';
  cancelingRideId: string | null = null;

  // Popup properties
  showPopup = false;
  popupMessage = '';
  popupType: 'success' | 'error' | 'warning' = 'success';
  popupTitle = '';

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.fetchRides();
  }

  fetchRides() {
    this.loading = true;
    this.error = '';
    const userId = localStorage.getItem('userId');
    if (!userId) {
      this.error = 'User not found.';
      this.loading = false;
      this.cdr.detectChanges();
      return;
    }
    this.http.get<any[]>(`http://localhost:8086/api/v1/rides/my`).subscribe({
      next: (data) => {
        this.rides = data || [];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.error = 'Failed to load rides.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  editRide(ride: any) {
    this.showPopupMessage('Edit Ride', 'Edit functionality will be implemented soon.', 'warning');
  }

  cancelRide(ride: any) {
    this.showPopupMessage(
      'Cancel Ride', 
      `Are you sure you want to cancel the ride from ${ride.from} to ${ride.to}?`,
      'warning'
    );
    
    this.rideToCancel = ride;
  }

  private rideToCancel: any = null;

  confirmCancel() {
    if (this.rideToCancel) {
      this.cancelingRideId = this.rideToCancel.id;
      this.hidePopup();
      
      this.http.delete(`http://localhost:8086/api/v1/rides/${this.rideToCancel.id}`).subscribe({
        next: () => {
          this.rides = this.rides.filter(r => r.id !== this.rideToCancel.id);
          this.cancelingRideId = null;
          this.rideToCancel = null;
          this.fetchRides();
          this.showPopupMessage('Success', 'Ride cancelled successfully!', 'success');
        },
        error: (err) => {
          this.cancelingRideId = null;
          this.rideToCancel = null;
          this.cdr.detectChanges();
          this.showPopupMessage('Error', 'Failed to cancel ride. Please try again.', 'error');
        }
      });
    }
  }

  showPopupMessage(title: string, message: string, type: 'success' | 'error' | 'warning') {
    this.popupTitle = title;
    this.popupMessage = message;
    this.popupType = type;
    this.showPopup = true;
    this.cdr.detectChanges();
  }

  hidePopup() {
    this.showPopup = false;
    this.cdr.detectChanges();
  }
} 