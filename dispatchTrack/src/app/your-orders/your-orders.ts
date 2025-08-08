import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { ChangeDetectorRef } from '@angular/core';
import { AuthService } from '../services/auth.service';

const STATUS_OPTIONS = [
  { label: 'All', value: 'ALL' },
  { label: 'Pending', value: 'PENDING' },
  { label: 'Accepted', value: 'ACCEPTED' },
  { label: 'Rejected', value: 'REJECTED' },
  { label: 'In Transit', value: 'INTRANSIT' },
  { label: 'Delivered', value: 'DELIVERED' }
];

@Component({
  selector: 'app-your-orders',
  templateUrl: './your-orders.html',
  styleUrls: ['./your-orders.scss'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class YourOrders implements OnInit {
  allOrders: any[] = [];
  filteredOrders: any[] = [];
  loading = false;
  error = '';
  statusOptions = STATUS_OPTIONS;
  selectedStatus = 'ALL';
  paymentLoading: string | null = null; // order id being paid

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef, private authService: AuthService) {}

  ngOnInit() {
    this.fetchOrders();
  }

  fetchOrders() {
    this.loading = true;
    this.error = '';
    this.http.post<any>('http://localhost:8087/api/v1/requests/my-requests', {}).subscribe({
      next: (response) => {
        this.allOrders = response?.data?.content || [];
        this.applyFilter();
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.error = 'Failed to load orders.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  applyFilter() {
    if (this.selectedStatus === 'ALL') {
      this.filteredOrders = this.allOrders;
    } else {
      this.filteredOrders = this.allOrders.filter(order => order.status === this.selectedStatus);
    }
  }

  onFilterChange(status: string) {
    this.selectedStatus = status;
    this.applyFilter();
  }

  pay(order: any) {
    this.paymentLoading = order.id;
    this.cdr.detectChanges();

    const userId = localStorage.getItem('userId');
    const userEmail = localStorage.getItem('email');
    const token = this.authService.getToken();

    if (!userId || !userEmail) {
      alert('User information not found. Please login again.');
      this.paymentLoading = null;
      this.cdr.detectChanges();
      return;
    }

    if (!token) {
      alert('Authentication token not found. Please login again.');
      this.paymentLoading = null;
      this.cdr.detectChanges();
      return;
    }

    // First API call to create payment
    const paymentRequest = {
      rideId: order.rideId,
      id: order.id,
      senderUserId: order.senderUserId,
      rideUserId: order.rideUserId,
      email: userEmail,
      paymentMethod: "WALLET",
      fare: order.fare || 0
    };

    this.http.post('http://localhost:9091/api/payments', paymentRequest).subscribe({
      next: (response: any) => {
        if (response.success === 0 && response.data) {
          // Second API call to create payment intent
          const paymentIntentRequest = {
            amount: order.fare || 0,
            email: userEmail,
          };

          this.http.post(`http://localhost:9091/api/payments/${response.data.paymentId}/create-payment-intent`, paymentIntentRequest).subscribe({
            next: (intentResponse: any) => {
              this.paymentLoading = null;
              this.cdr.detectChanges();
              
              if (intentResponse.success === 0 && intentResponse.data.status === 'succeeded') {
                alert('Payment processed successfully!');
                // Refresh orders to update status
                this.fetchOrders();
              } else {
                alert(intentResponse.message || 'Payment failed. Please try again.');
              }
            },
            error: (intentError) => {
              this.paymentLoading = null;
              this.cdr.detectChanges();
              alert('Failed to process payment. Please try again.');
            }
          });
        } else {
          this.paymentLoading = null;
          this.cdr.detectChanges();
          alert(response.message || 'Failed to create payment. Please try again.');
        }
      },
      error: (err) => {
        this.paymentLoading = null;
        this.cdr.detectChanges();
        alert('Failed to initiate payment. Please try again.');
      }
    });
  }
} 