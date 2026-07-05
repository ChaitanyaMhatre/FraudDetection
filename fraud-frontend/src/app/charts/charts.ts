import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';

import {
  Chart,
  registerables
} from 'chart.js';

import {
  NgChartsModule
} from 'ng2-charts';

import {
  ChartConfiguration,
  ChartType
} from 'chart.js';

import { TransactionService }
from '../services/transaction.service';

// ✅ REGISTER ALL CHART.JS MODULES (including Controllers)
Chart.register(...registerables);

@Component({
  selector: 'app-charts',
  standalone: true,
  imports: [
    CommonModule,
    NgChartsModule
  ],
  templateUrl: './charts.html',
  styleUrls: ['./charts.css']
})
export class ChartsComponent implements OnInit, OnDestroy {

  transactions: any[] = [];
  private sub: Subscription | null = null;
  private lastDataHash = '';

  // 🥧 PIE CHART
  pieChartType: ChartType = 'pie';

  pieChartData: ChartConfiguration<'pie'>['data'] = {
    labels: ['Success', 'Blocked'],
    datasets: [
      {
        data: [0, 0]
      }
    ]
  };

  // 📊 BAR CHART
  barChartType: ChartType = 'bar';

  barChartData: ChartConfiguration<'bar'>['data'] = {
    labels: [],
    datasets: [
      {
        data: [],
        label: 'Transactions'
      }
    ]
  };

  constructor(
    private transactionService: TransactionService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.sub = this.transactionService.transactions$.subscribe({
      next: (data) => {
        this.updateCharts(data);
      }
    });
  }

  ngOnDestroy(): void {
    if (this.sub) {
      this.sub.unsubscribe();
    }
  }

  updateCharts(data: any[]) {
    try {
      const dataStr = JSON.stringify(data || []);
      if (dataStr === this.lastDataHash) {
        return;
      }
      this.lastDataHash = dataStr;

      this.transactions = data || [];

      // ✅ SUCCESS COUNT
      const success = this.transactions.filter(
        (t: any) => t.status === 'SUCCESS'
      ).length;

      // ✅ BLOCKED COUNT
      const blocked = this.transactions.filter(
        (t: any) => t.status === 'BLOCKED'
      ).length;

      // 🥧 UPDATE PIE CHART (create a new reference to trigger change detection)
      this.pieChartData = {
        labels: ['Success', 'Blocked'],
        datasets: [
          {
            data: [success, blocked]
          }
        ]
      };

      // 📊 LAST 7 TRANSACTIONS
      const lastTransactions = this.transactions.slice(-7);

      this.barChartData = {
        labels: lastTransactions.map((t: any) => 'TX ' + t.id),
        datasets: [
          {
            data: lastTransactions.map((t: any) => t.amount),
            label: 'Transactions'
          }
        ]
      };

      this.cdr.detectChanges(); // Force Angular to update UI
    } catch (error) {
      console.error("❌ Error updating chart data:", error);
    }
  }
}
