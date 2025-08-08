import { Component, OnInit, AfterViewInit } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { CommonModule } from '@angular/common';
import * as L from 'leaflet';
@Component({
  selector: 'app-user-dashboard',
  templateUrl: './user-dashboard.html',
  styleUrls: ['./user-dashboard.scss'],
  standalone: true,
  imports: [CommonModule]
})
export class UserDashboard implements OnInit, AfterViewInit {
  private map: any;
  private marker: any;

  ngAfterViewInit() {
    setTimeout(() => {
      const mapContainer = document.getElementById('map');
      if (mapContainer && !this.map) {
        this.initMap();
      }
    }, 0);
  }

  private initMap() {
    this.map = L.map('map').setView([20.5937, 78.9629], 5); // Default to India
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap'
    }).addTo(this.map);

    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition((position) => {
        const lat = position.coords.latitude;
        const lng = position.coords.longitude;
        this.map.setView([lat, lng], 15);
        this.marker = L.marker([lat, lng]).addTo(this.map)
          .bindPopup('You are here').openPopup();
      }, (err) => {
        // fallback to default
      });
    }
  }
  selectedMode: 'Sender' | 'Rider' = 'Sender';
  username: string | null = null;
  showProfileMenu = false;

  quickActionSelected: 'send' | 'track' = 'send';

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.username = localStorage.getItem('username');
  }

  onSignout(): void {
    this.authService.logout();
    // Optionally, redirect to login page here
  }

  onProfile(): void {
    // TODO: Navigate to profile page
  }

  onSettings(): void {
    // TODO: Navigate to settings page
  }

  onPayments(): void {
    // TODO: Navigate to payments page
  }
}
