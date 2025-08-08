import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { interval, Subscription } from 'rxjs';
import * as L from 'leaflet';

interface Delivery {
  id: string;
  driverName: string;
  driverPhone: string;
  status: 'pending' | 'in-transit' | 'delivered' | 'cancelled';
  eta: Date;
  speed: number;
  currentLocation: {
    lat: number;
    lng: number;
  };
  from: string;
  to: string;
}

interface RouteStep {
  title: string;
  icon: string;
  completed: boolean;
  active: boolean;
  time?: Date;
}

interface RecentDelivery {
  id: string;
  title: string;
  status: 'pending' | 'in-transit' | 'delivered' | 'cancelled';
  date: Date;
  from: string;
  to: string;
}

@Component({
  selector: 'app-tracking',
  templateUrl: './tracking.html',
  styleUrls: ['./tracking.scss'],
  standalone: true,
  imports: [CommonModule, RouterModule]
})
export class TrackingComponent implements OnInit, OnDestroy, AfterViewInit {
  @ViewChild('mapContainer') mapContainer!: ElementRef;

  // Tracking Data
  currentDelivery: Delivery | null = null;
  activeDeliveries: number = 0;
  completedDeliveries: number = 0;
  mapLoaded: boolean = false;
  map: any = null;

  // Route Progress
  routeSteps: RouteStep[] = [];

  // Recent Deliveries
  recentDeliveries: RecentDelivery[] = [];

  // Subscriptions
  private locationSubscription?: Subscription;
  private statsSubscription?: Subscription;

  constructor() {
    // Initialize with empty data
    this.currentDelivery = null;
    this.activeDeliveries = 0;
    this.completedDeliveries = 0;
  }

  ngOnInit(): void {
    // Initialize tracking data when component loads
    this.loadTrackingData();
  }

  ngAfterViewInit(): void {
    this.initializeMap();
  }

  ngOnDestroy(): void {
    // Clean up subscriptions if any
    this.locationSubscription?.unsubscribe();
    this.statsSubscription?.unsubscribe();
  }

  // Map Initialization
  private initializeMap(): void {
    // Initialize map when component loads
    setTimeout(() => {
      this.mapLoaded = true;
      this.loadMapData();
    }, 1000);
  }

  private loadMapData(): void {
    // Initialize Leaflet map
    console.log('Loading map data...');
    
    // Get current user location
    this.getCurrentLocation();
  }

  private getCurrentLocation(): void {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const currentLocation = {
            lat: position.coords.latitude,
            lng: position.coords.longitude
          };
          console.log('Current location:', currentLocation);
          this.initializeLeafletMap(currentLocation);
        },
        (error) => {
          console.error('Error getting current location:', error);
          // Fallback to default location (India center)
          this.initializeLeafletMap({ lat: 20.5937, lng: 78.9629 });
        }
      );
    } else {
      console.log('Geolocation not supported');
      // Fallback to default location
      this.initializeLeafletMap({ lat: 20.5937, lng: 78.9629 });
    }
  }

  private initializeLeafletMap(location: { lat: number; lng: number }): void {
    if (this.mapContainer && this.mapContainer.nativeElement) {
      // Initialize the map
      this.map = L.map(this.mapContainer.nativeElement).setView([location.lat, location.lng], 13);

      // Add OpenStreetMap tiles
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors'
      }).addTo(this.map);

      // Add current location marker
      const currentLocationMarker = L.marker([location.lat, location.lng], {
        icon: L.divIcon({
          className: 'current-location-marker',
          html: '<div style="background-color: #2563eb; color: white; border-radius: 50%; width: 20px; height: 20px; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: bold;">📍</div>',
          iconSize: [20, 20],
          iconAnchor: [10, 10]
        })
      }).addTo(this.map);

      // Add popup to marker
      currentLocationMarker.bindPopup('Your Current Location').openPopup();

      console.log('Leaflet map initialized with location:', location);
    }
  }

  private updateMapLocation(location: { lat: number; lng: number }): void {
    if (this.map) {
      // Update map view to new location
      this.map.setView([location.lat, location.lng], 13);
      console.log('Map location updated:', location);
    }
  }

  // Load tracking data
  private loadTrackingData(): void {
    // This method will load real tracking data from your backend
    console.log('Loading tracking data...');
    // TODO: Implement API calls to load real tracking data
  }

  // User Actions
  refreshLocation(): void {
    console.log('Refreshing location...');
    this.getCurrentLocation();
  }

  toggleFullscreen(): void {
    console.log('Toggling fullscreen...');
    // Implement fullscreen functionality
  }

  contactDriver(): void {
    if (this.currentDelivery) {
      console.log('Contacting driver:', this.currentDelivery.driverPhone);
      // Implement call functionality
      window.open(`tel:${this.currentDelivery.driverPhone}`, '_blank');
    } else {
      console.log('No active delivery to contact');
    }
  }

  shareLocation(): void {
    if (this.currentDelivery) {
      const locationUrl = `https://maps.google.com/?q=${this.currentDelivery.currentLocation.lat},${this.currentDelivery.currentLocation.lng}`;
      console.log('Sharing location:', locationUrl);
      
      if (navigator.share) {
        navigator.share({
          title: 'Track my delivery',
          text: 'Check out my delivery location',
          url: locationUrl
        });
      } else {
        // Fallback for browsers that don't support Web Share API
        navigator.clipboard.writeText(locationUrl);
        // Show toast notification
        this.showNotification('Location copied to clipboard');
      }
    } else {
      console.log('No active delivery to share');
    }
  }

  downloadReport(): void {
    console.log('Downloading tracking report...');
    // Implement report generation and download
    this.generateTrackingReport();
  }

  private generateTrackingReport(): void {
    const report = {
      deliveryId: this.currentDelivery?.id || 'No active delivery',
      driverName: this.currentDelivery?.driverName || 'No driver assigned',
      status: this.currentDelivery?.status || 'No status',
      route: this.currentDelivery ? `${this.currentDelivery.from} → ${this.currentDelivery.to}` : 'No route',
      timestamp: new Date().toISOString(),
      activeDeliveries: this.activeDeliveries,
      completedDeliveries: this.completedDeliveries
    };

    const blob = new Blob([JSON.stringify(report, null, 2)], { type: 'application/json' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `tracking-report-${Date.now()}.json`;
    a.click();
    window.URL.revokeObjectURL(url);
  }

  private showNotification(message: string): void {
    // Implement toast notification
    console.log('Notification:', message);
  }

  // Route Progress Management
  updateRouteProgress(stepIndex: number): void {
    this.routeSteps.forEach((step, index) => {
      step.completed = index < stepIndex;
      step.active = index === stepIndex;
    });
  }

  // Utility Methods
  getStatusColor(status: string): string {
    const statusColors: { [key: string]: string } = {
      'pending': 'warning',
      'in-transit': 'primary',
      'delivered': 'success',
      'cancelled': 'danger'
    };
    return statusColors[status] || 'secondary';
  }
} 