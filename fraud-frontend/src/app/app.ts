import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';

import { AlertsComponent } from './alerts/alerts';
import { TransactionsComponent } from './transactions/transactions';
import { ChartsComponent } from './charts/charts';
import { LoginComponent } from './login/login';
import { MapComponent } from './map/map';

import { AuthService } from './services/auth.service';
import { TransactionService } from './services/transaction.service';
import { UserDashboardComponent } from './user-dashboard/user-dashboard';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    AlertsComponent,
    TransactionsComponent,
    ChartsComponent,
    LoginComponent,
    UserDashboardComponent,
    MapComponent
  ],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class AppComponent implements OnInit, OnDestroy {
  activeTab: string = 'dashboard';
  private transactionsSub: Subscription | null = null;

  // KPI variables
  transactions: any[] = [];
  totalUsers = 0;
  totalTransactions = 0;
  successfulTransactions = 0;
  suspiciousTransactions = 0;
  blockedTransactions = 0;
  fraudRate = 0;
  transactionVolume = 0;

  constructor(
    public authService: AuthService,
    private transactionService: TransactionService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.transactionsSub = this.transactionService.transactions$.subscribe({
      next: (data) => {
        if (!data) return;
        this.transactions = data || [];
        this.totalTransactions = data.length;
        
        this.successfulTransactions = data.filter((t: any) => t.status === 'SUCCESS').length;
        this.suspiciousTransactions = data.filter((t: any) => t.status === 'SUSPICIOUS').length;
        this.blockedTransactions = data.filter((t: any) => t.status === 'BLOCKED').length;
        
        // Fraud Rate
        const totalFlagged = this.suspiciousTransactions + this.blockedTransactions;
        this.fraudRate = this.totalTransactions > 0 
          ? parseFloat(((totalFlagged / this.totalTransactions) * 100).toFixed(1))
          : 0;

        // Transaction Volume (Sum of successful transaction amounts)
        this.transactionVolume = data
          .filter((t: any) => t.status === 'SUCCESS')
          .reduce((sum: number, t: any) => sum + (t.amount || 0), 0);

        // Unique Users calculation: count unique names in transactions
        const uniqueUsernames = new Set();
        data.forEach((t: any) => {
          if (t.senderUsername) uniqueUsernames.add(t.senderUsername);
          if (t.receiverUsername) uniqueUsernames.add(t.receiverUsername);
          if (t.sender && t.sender.username) uniqueUsernames.add(t.sender.username);
          if (t.receiver && t.receiver.username) uniqueUsernames.add(t.receiver.username);
        });
        // Include default seeded accounts
        uniqueUsernames.add('admin');
        uniqueUsernames.add('user');
        this.totalUsers = uniqueUsernames.size;

        this.cdr.detectChanges();
      }
    });
  }

  ngOnDestroy() {
    if (this.transactionsSub) {
      this.transactionsSub.unsubscribe();
    }
  }

  selectTab(tab: string) {
    this.activeTab = tab;
  }

  // ✅ Logout method
  logout() {
    this.authService.logout();
    location.reload();
  }
}
