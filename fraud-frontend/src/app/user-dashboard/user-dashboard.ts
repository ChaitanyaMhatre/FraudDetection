import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { API_BASE_URL } from '../config';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './user-dashboard.html',
  styleUrls: ['./user-dashboard.css']
})
export class UserDashboardComponent implements OnInit {

  // Pay form fields
  senderId = 1;
  receiverId = 2;
  amount: number | null = null;
  message = '';

  // Geolocation
  latitude = 0.0;
  longitude = 0.0;

  // Verification PIN Modal
  showOtpModal = false;
  otpCode = '';
  currentTransactionId: number | null = null;

  // Profile data
  currentUserId = 1;
  profileName = localStorage.getItem('name') || '';
  profileUsername = localStorage.getItem('username') || '';
  profileBalance = 0.0;
  profileEmail = '';
  
  // UI states
  showBalance = true;
  showToast = false;
  toastMessage = '';
  toastTimeout: any = null;
  loadingPay = false;

  // Activity feed
  userTransactions: any[] = [];

  constructor(
    private http: HttpClient,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    // 1. Get Logged in User ID from local storage
    const storedId = localStorage.getItem('id');
    if (storedId) {
      this.currentUserId = parseInt(storedId, 10);
      this.senderId = this.currentUserId;
    }

    // 2. Fetch profile data
    this.loadUserProfile();

    // 3. Fetch transaction history
    this.loadUserTransactions();

    // 4. Load Geolocation
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          this.latitude = position.coords.latitude;
          this.longitude = position.coords.longitude;
          console.log(`🌐 Geolocation obtained: ${this.latitude}, ${this.longitude}`);
        },
        (error) => {
          console.log('⚠️ Geolocation blocked or not available:', error.message);
        }
      );
    }
  }

  loadUserProfile() {
    this.http.get(`${API_BASE_URL}/api/auth/profile/${this.currentUserId}`).subscribe({
      next: (user: any) => {
        this.profileName = user.name || 'User';
        this.profileUsername = user.username || 'user';
        this.profileBalance = user.balance || 0.0;
        this.profileEmail = user.email || '';
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading profile:', err);
        this.cdr.detectChanges();
      }
    });
  }

  loadUserTransactions() {
    this.http.get<any[]>(`${API_BASE_URL}/api/transactions/user/${this.currentUserId}`).subscribe({
      next: (txs) => {
        this.userTransactions = txs || [];
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading user transactions:', err);
        this.cdr.detectChanges();
      }
    });
  }

  toggleBalanceVisibility() {
    this.showBalance = !this.showBalance;
  }

  selectPayee(id: number) {
    this.receiverId = id;
  }

  // Keypad Typing Handlers
  typePinNumber(num: string) {
    if (this.otpCode.length < 6) {
      this.otpCode += num;
    }
  }

  deletePinNumber() {
    if (this.otpCode.length > 0) {
      this.otpCode = this.otpCode.slice(0, -1);
    }
  }

  closeOtpModal() {
    this.showOtpModal = false;
    this.otpCode = '';
    this.currentTransactionId = null;
  }

  openExistingOtp(txId: number) {
    this.currentTransactionId = txId;
    
    // Find transaction to populate summary in PIN pad
    const found = this.userTransactions.find(t => t.id === txId);
    if (found) {
      this.receiverId = found.receiverId;
      this.amount = found.amount;
    }
    
    this.showOtpModal = true;
  }

  // Display 'Work in progress' custom GPay Toast
  showProgressToast(serviceName: string) {
    if (this.toastTimeout) {
      clearTimeout(this.toastTimeout);
    }
    this.toastMessage = `${serviceName}: Work on progress`;
    this.showToast = true;
    this.toastTimeout = setTimeout(() => {
      this.showToast = false;
    }, 3000);
  }

  sendMoney() {
    if (!this.amount || this.amount <= 0) {
      this.message = "❌ Please enter a valid transfer amount";
      return;
    }

    this.message = '';
    this.loadingPay = true;

    const request = {
      senderId: this.senderId,
      receiverId: this.receiverId,
      amount: this.amount,
      transactionFrequency: 1,
      avgTransaction: 3000,
      deviceChange: 0,
      locationRisk: 0,
      latitude: this.latitude,
      longitude: this.longitude
    };

    this.http.post(
      `${API_BASE_URL}/api/transactions/send`,
      request
    ).subscribe({
      next: (response: any) => {
        console.log("💰 Transaction response received:", response);
        this.loadingPay = false;
        // Reload history and balance immediately
        this.loadUserProfile();
        this.loadUserTransactions();

        if (response.status === 'SUSPICIOUS') {
          this.message = "⚠️ Suspicious Transaction: OTP Sent to Email";
          this.currentTransactionId = response.id;
          this.otpCode = ''; // Reset PIN inputs
          this.showOtpModal = true;
          console.log("🔓 showOtpModal set to true, currentTransactionId:", this.currentTransactionId);
        } else {
          this.message = "✅ Transaction Status: " + response.status;
          this.amount = null; // Clear amount on success
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error("❌ Transaction API Error:", err);
        this.loadingPay = false;
        this.message = "❌ " + (err.error?.message || "Transaction Failed");
        this.cdr.detectChanges();
      }
    });
  }

  verifyOtp() {
    if (!this.currentTransactionId || !this.otpCode || this.otpCode.length < 6) {
      this.message = "❌ Please enter full 6-digit OTP code";
      return;
    }

    this.http.post(
      `${API_BASE_URL}/api/otp/verify`,
      {
        transactionId: this.currentTransactionId,
        otp: this.otpCode
      }
    ).subscribe({
      next: (res: any) => {
        this.message = "✅ Transaction Approved successfully!";
        this.showOtpModal = false;
        this.otpCode = '';
        this.currentTransactionId = null;
        this.amount = null; // Reset
        
        // Reload details
        this.loadUserProfile();
        this.loadUserTransactions();
      },
      error: (err: any) => {
        console.error(err);
        this.message = "❌ OTP verification failed: " + (err.error?.message || "Invalid OTP");
        this.otpCode = ''; // Clear failed PIN code
      }
    });
  }

  // Visual helper methods
  getAvatarColor(name: string): string {
    if (!name) return '#1a73e8';
    
    // Hash function to choose deterministic background colors
    let hash = 0;
    for (let i = 0; i < name.length; i++) {
      hash = name.charCodeAt(i) + ((hash << 5) - hash);
    }
    
    const colors = [
      '#1a73e8', // Google Blue
      '#0f9d58', // Google Green
      '#ea4335', // Google Red
      '#f4b400', // Google Yellow
      '#673ab7', // Deep Purple
      '#00acc1', // Teal
      '#e91e63'  // Pink
    ];
    
    const index = Math.abs(hash) % colors.length;
    return colors[index];
  }

  getInitials(name: string): string {
    if (!name) return 'U';
    const parts = name.trim().split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name.slice(0, 2).toUpperCase();
  }

  logout() {
    this.authService.logout();
    location.reload();
  }
}
