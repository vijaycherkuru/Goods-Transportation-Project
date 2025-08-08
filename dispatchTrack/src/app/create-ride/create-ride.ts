import { Component, OnInit, AfterViewInit, ElementRef, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import * as L from 'leaflet';

@Component({
  selector: 'app-create-ride',
  templateUrl: './create-ride.html',
  styleUrl: './create-ride.scss',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule]
})
export class CreateRide implements OnInit, AfterViewInit {
  @ViewChild('mapContainer', { static: false }) mapContainer!: ElementRef;
  
  rideForm: FormGroup;
  submitted = false;
  showSuccessModal = false;
  showLoading = false;
  
  // Map related properties
  private map!: L.Map;
  private fromMarker!: L.Marker;
  private toMarker!: L.Marker;
  fromSuggestions: any[] = [];
  toSuggestions: any[] = [];
  showFromSuggestions = false;
  showToSuggestions = false;

  hours = Array.from({ length: 23 }, (_, i) => i + 1); 

  vehicleTypes = [
    'Car',
    'Van',
    'Bike',
    'Auto'
  ];

  constructor(private fb: FormBuilder, private http: HttpClient, private router: Router) {
    this.rideForm = this.fb.group({
      from: ['', Validators.required],
      to: ['', Validators.required],
      date: ['', Validators.required],
      time: ['', Validators.required],
      vehicleType: ['', Validators.required],
      luggageSpace: ['', Validators.required],
      licenseNumber: ['', Validators.required]
    });
  }

  ngOnInit() {
    // Set minimum date to today
    const today = new Date().toISOString().split('T')[0];
    this.rideForm.patchValue({ date: today });
  }

  ngAfterViewInit() {
    this.initMap();
  }

  private initMap(): void {
    // Initialize map centered on India
    this.map = L.map(this.mapContainer.nativeElement).setView([20.5937, 78.9629], 5);
    
    // Add OpenStreetMap tiles
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);

    // Get current location
    this.getCurrentLocation();

    // Custom marker icons with better styling
    const fromIcon = L.divIcon({
      className: 'custom-marker from-marker',
      html: '<div class="marker-content"><i class="bi bi-geo-alt-fill"></i><span>From</span></div>',
      iconSize: [40, 40],
      iconAnchor: [20, 40]
    });

    const toIcon = L.divIcon({
      className: 'custom-marker to-marker',
      html: '<div class="marker-content"><i class="bi bi-geo-alt"></i><span>To</span></div>',
      iconSize: [40, 40],
      iconAnchor: [20, 40]
    });

    // Initialize markers
    this.fromMarker = L.marker([0, 0], { icon: fromIcon });
    this.toMarker = L.marker([0, 0], { icon: toIcon });
    
    // Add click handlers to markers
    this.fromMarker.on('click', () => {
      this.showMarkerInfo('From Location', this.rideForm.get('from')?.value);
    });
    
    this.toMarker.on('click', () => {
      this.showMarkerInfo('To Location', this.rideForm.get('to')?.value);
    });
  }

  // Get current location
  private getCurrentLocation() {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const lat = position.coords.latitude;
          const lng = position.coords.longitude;
          
          // Center map on current location
          this.map.setView([lat, lng], 10);
          
          // Add current location marker
          const currentLocationIcon = L.divIcon({
            className: 'custom-marker current-location-marker',
            html: '<div class="marker-content"><i class="bi bi-geo-alt-fill"></i><span>You</span></div>',
            iconSize: [40, 40],
            iconAnchor: [20, 40]
          });
          
          const currentMarker = L.marker([lat, lng], { icon: currentLocationIcon }).addTo(this.map);
          
          // Add click handler for current location
          currentMarker.on('click', () => {
            this.showMarkerInfo('Current Location', `Lat: ${lat.toFixed(4)}, Lng: ${lng.toFixed(4)}`);
          });
          
          // Reverse geocode to get address
          this.reverseGeocode(lat, lng);
        },
        (error) => {
          console.log('Error getting current location:', error);
          // Keep default India view if location access is denied
        }
      );
    }
  }

  // Reverse geocode to get address from coordinates
  private async reverseGeocode(lat: number, lng: number) {
    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&zoom=18&addressdetails=1`
      ).then(res => res.json());
      
      if (response.display_name) {
        console.log('Current location:', response.display_name);
        // Optionally auto-fill the "from" field with current location
        // this.rideForm.patchValue({ from: response.display_name });
      }
    } catch (error) {
      console.error('Error reverse geocoding:', error);
    }
  }

  // Show marker information
  private showMarkerInfo(title: string, location: string) {
    if (location) {
      alert(`${title}: ${location}`);
    }
  }

  // Location search with OpenStreetMap Nominatim API
  async searchLocation(query: string): Promise<any[]> {
    if (query.length < 3) return [];
    
    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&countrycodes=in&limit=5`
      ).then(res => res.json());
      
      return response.map((item: any) => ({
        display_name: item.display_name,
        lat: parseFloat(item.lat),
        lon: parseFloat(item.lon)
      }));
    } catch (error) {
      console.error('Error searching location:', error);
      return [];
    }
  }

  // Handle from location input
  async onFromInput(event: any) {
    const query = event.target.value;
    this.showFromSuggestions = query.length >= 3;
    
    if (this.showFromSuggestions) {
      this.fromSuggestions = await this.searchLocation(query);
    }
  }

  // Handle to location input
  async onToInput(event: any) {
    const query = event.target.value;
    this.showToSuggestions = query.length >= 3;
    
    if (this.showToSuggestions) {
      this.toSuggestions = await this.searchLocation(query);
    }
  }

  // Select from location
  selectFromLocation(location: any) {
    this.rideForm.patchValue({ from: location.display_name });
    this.showFromSuggestions = false;
    this.addFromMarker(location.lat, location.lon);
    this.updateMapView();
  }

  // Select to location
  selectToLocation(location: any) {
    this.rideForm.patchValue({ to: location.display_name });
    this.showToSuggestions = false;
    this.addToMarker(location.lat, location.lon);
    this.updateMapView();
  }

  // Add from marker to map
  private addFromMarker(lat: number, lon: number) {
    this.fromMarker.setLatLng([lat, lon]).addTo(this.map);
  }

  // Add to marker to map
  private addToMarker(lat: number, lon: number) {
    this.toMarker.setLatLng([lat, lon]).addTo(this.map);
  }

  // Update map view to show both markers
  private updateMapView() {
    const fromLatLng = this.fromMarker.getLatLng();
    const toLatLng = this.toMarker.getLatLng();
    
    if (fromLatLng.lat !== 0 && toLatLng.lat !== 0) {
      const bounds = L.latLngBounds([fromLatLng, toLatLng]);
      this.map.fitBounds(bounds, { padding: [50, 50] });
      
      // Draw route line
      this.drawRoute(fromLatLng, toLatLng);
      
      // Add distance calculation
      const distance = fromLatLng.distanceTo(toLatLng);
      this.showRouteInfo(distance);
    }
  }

  // Show route information
  private showRouteInfo(distance: number) {
    // For now, we'll just log the distance
    // In a real implementation, you could add a custom overlay
    console.log(`Route distance: ${(distance / 1000).toFixed(1)} km`);
    console.log(`From: ${this.rideForm.get('from')?.value}`);
    console.log(`To: ${this.rideForm.get('to')?.value}`);
  }

  // Draw route line between markers
  private drawRoute(from: L.LatLng, to: L.LatLng) {
    // Remove existing route line
    this.map.eachLayer((layer) => {
      if (layer instanceof L.Polyline) {
        this.map.removeLayer(layer);
      }
    });

    // Add new route line with better styling
    L.polyline([from, to], {
      color: '#2563eb',
      weight: 6,
      opacity: 0.8,
      dashArray: '15, 10',
      lineCap: 'round',
      lineJoin: 'round'
    }).addTo(this.map);

    // Add direction arrow in the middle
    const midPoint = L.latLng(
      (from.lat + to.lat) / 2,
      (from.lng + to.lng) / 2
    );
    
    const arrowIcon = L.divIcon({
      className: 'route-arrow',
      html: '<i class="bi bi-arrow-right"></i>',
      iconSize: [20, 20],
      iconAnchor: [10, 10]
    });
    
    L.marker(midPoint, { icon: arrowIcon }).addTo(this.map);
  }

  // Hide suggestions when clicking outside
  hideSuggestions() {
    setTimeout(() => {
      this.showFromSuggestions = false;
      this.showToSuggestions = false;
    }, 200);
  }

  get f() { return this.rideForm.controls; }

  onSubmit() {
    this.submitted = true;
    if (this.rideForm.invalid) {
      return;
    }
    this.showLoading = true;
    const formValue = { ...this.rideForm.value };
    // Parse hour from time (string)
    const hour = parseInt(formValue.time, 10);
    // Prepare payload as per backend expectation
    const payload = {
      from: formValue.from,
      to: formValue.to,
      date: formValue.date,
      time: hour.toString().padStart(2, '0') + ':00:00',
      vehicleType: formValue.vehicleType ? formValue.vehicleType.toUpperCase() : undefined,
      luggageSpace: formValue.luggageSpace ? formValue.luggageSpace.toUpperCase() : undefined,
      drivingLicenseNumber: formValue.licenseNumber,
      rideUserId: localStorage.getItem('userId') || ''
    };
    this.http.post('http://localhost:8086/api/v1/rides', payload).subscribe({
      next: (response) => {
        this.rideForm.reset();
        this.submitted = false;
        this.showLoading = false;
        this.showSuccessModal = true;
        // Clear map markers
        this.fromMarker.remove();
        this.toMarker.remove();
      },
      error: (error) => {
        this.showLoading = false;
        alert('Failed to create ride. Please try again.');
      }
    });
  }

  closeSuccessModal() {
    this.showSuccessModal = false;
    this.router.navigate(['/dashboard']);
  }
} 