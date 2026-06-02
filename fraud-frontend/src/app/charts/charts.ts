import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

import {
  Chart,
  CategoryScale,
  LinearScale,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend
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

// ✅ REGISTER CHART.JS MODULES
Chart.register(
  CategoryScale,
  LinearScale,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend
);

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
export class ChartsComponent
implements OnInit {

  transactions: any[] = [];

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
    private transactionService:
    TransactionService
  ) {}

  ngOnInit(): void {

    this.loadCharts();
  }

  async loadCharts() {

    const data =
      await this.transactionService
      .getAllTransactions();

    console.log("📊 CHART DATA:", data);

    this.transactions = data;

    // ✅ SUCCESS COUNT
    const success =
      data.filter(
        (t: any) =>
          t.status === 'SUCCESS'
      ).length;

    // ✅ BLOCKED COUNT
    const blocked =
      data.filter(
        (t: any) =>
          t.status === 'BLOCKED'
      ).length;

    // 🥧 UPDATE PIE CHART
    this.pieChartData = {
      labels: ['Success', 'Blocked'],
      datasets: [
        {
          data: [success, blocked]
        }
      ]
    };

    // 📊 LAST 7 TRANSACTIONS
    const lastTransactions =
      data.slice(-7);

    this.barChartData = {
      labels:
        lastTransactions.map(
          (t: any) =>
            'TX ' + t.id
        ),

      datasets: [
        {
          data:
            lastTransactions.map(
              (t: any) =>
                t.amount
            ),

          label: 'Transactions'
        }
      ]
    };

    console.log(
      "🥧 PIE:",
      this.pieChartData
    );

    console.log(
      "📊 BAR:",
      this.barChartData
    );
  }
}
