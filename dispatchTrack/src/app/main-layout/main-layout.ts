import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-main-layout',
  templateUrl: './main-layout.html',
  styleUrls: ['./main-layout.scss'],
  standalone: true,
  imports: [CommonModule, RouterModule]
})
    
export class MainLayout {
  profileDropdownOpen = false;

  constructor(private authService: AuthService, private router: Router) {}

  toggleProfileDropdown() {
    this.profileDropdownOpen = !this.profileDropdownOpen;
  }

  closeProfileDropdown() {
    this.profileDropdownOpen = false;
  }

  onSignOut() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
