
import { Component, ChangeDetectorRef } from '@angular/core';
import { trigger, transition, style, animate } from '@angular/animations';
import { AuthService, UserProfile } from '../services/auth.service';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
  imports: [CommonModule, ReactiveFormsModule],
  animations: [
    trigger('fadeInOut', [
      transition(':enter', [
        style({ opacity: 0 }),
        animate('300ms ease-in', style({ opacity: 1 }))
      ]),
      transition(':leave', [
        animate('200ms ease-out', style({ opacity: 0 }))
      ])
    ])
  ]
})
export class Profile {
  profile: UserProfile | null = null;
  loading = true;
  error: string | null = null;
  editMode = false;
  profileForm: FormGroup;
  showSuccessModal = false;

  constructor(private authService: AuthService, private fb: FormBuilder, private cdr: ChangeDetectorRef) {
    this.profileForm = this.fb.group({
      name: [''],
      email: [''],
      phone: [''],
      age: [''],
      gender: [''],
      street: [''],
      city: [''],
      state: [''],
      zipCode: [''],
      country: ['']
    });
  }

  ngOnInit() {
    this.fetchProfile();
  }

  fetchProfile() {
    this.loading = true;
    this.error = null;
    this.authService.getProfile().subscribe({
      next: (response: any) => {
        // Expecting { data: { ...profileFields } }
        const data = response.data || response;
        this.profile = data;
        this.loading = false;
        this.profileForm.patchValue(data);
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to load profile.';
        this.loading = false;
      }
    });
  }

  onEditProfile() {
    this.editMode = true;
    if (this.profile) {
      this.profileForm.patchValue(this.profile);
    }
  }

  onCancelEdit() {
    this.editMode = false;
    if (this.profile) {
      this.profileForm.patchValue(this.profile);
    }
  }

  onSaveProfile() {
    if (this.profileForm.invalid) return;
    this.loading = true;
    this.authService.updateProfile(this.profileForm.value).subscribe({
      next: (response: any) => {
        // Accept both { data: ... } and direct object
        const data = response.data || response;
        this.profile = data;
        this.editMode = false;
        this.loading = false;
        this.showSuccessModal = true;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to update profile.';
        this.loading = false;
      }
    });
  }

  closeSuccessModal() {
    this.showSuccessModal = false;
    this.fetchProfile();
  }
}
