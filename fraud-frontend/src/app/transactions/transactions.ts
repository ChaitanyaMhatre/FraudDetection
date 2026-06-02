import {
  ChangeDetectorRef
} from '@angular/core';

import {
  Component,
  OnInit
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  HttpClient
} from '@angular/common/http';

import * as XLSX from 'xlsx';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './transactions.html',
  styleUrls: ['./transactions.css']
})
export class TransactionsComponent
implements OnInit {

  transactions: any[] = [];

  total = 0;
  success = 0;
  suspicious = 0;
  blocked = 0;

  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {

    this.loadTransactions();

    setInterval(() => {

      this.loadTransactions();

    }, 3000);
  }

  loadTransactions() {

    console.log("🚀 METHOD STARTED");

    this.http.get<any[]>(
      'http://localhost:8080/api/transactions/all'
    ).subscribe({

      next: (data) => {

        console.log("✅ RESPONSE RECEIVED");

        console.log(data);

        try {

          this.transactions = data || [];
          this.cdr.detectChanges();

          console.log(
            "🔥 ASSIGNED:",
            this.transactions
          );

          this.total =
            this.transactions.length;

          this.success =
            this.transactions.filter(
              t => t.status === 'SUCCESS'
            ).length;

          this.suspicious =
            this.transactions.filter(
              t => t.status === 'SUSPICIOUS'
            ).length;

          this.blocked =
            this.transactions.filter(
              t => t.status === 'BLOCKED'
            ).length;

        } catch(e) {

          console.error(
            "❌ FILTER ERROR:",
            e
          );
        }
      },

      error: (err) => {

        console.error(
          "❌ HTTP ERROR:",
          err
        );
      }
    });
  }

  exportToExcel() {

    const worksheet =
      XLSX.utils.json_to_sheet(
        this.transactions
      );

    const workbook =
      XLSX.utils.book_new();

    XLSX.utils.book_append_sheet(
      workbook,
      worksheet,
      'Transactions'
    );

    XLSX.writeFile(
      workbook,
      'transactions.xlsx'
    );
  }
}
