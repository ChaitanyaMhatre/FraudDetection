import {
  ChangeDetectorRef,
  Component,
  OnInit,
  OnDestroy
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { TransactionService } from '../services/transaction.service';
import * as XLSX from 'xlsx';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './transactions.html',
  styleUrls: ['./transactions.css']
})
export class TransactionsComponent implements OnInit, OnDestroy {

  transactions: any[] = [];
  private sub: Subscription | null = null;
  private lastDataHash = '';

  total = 0;
  success = 0;
  suspicious = 0;
  blocked = 0;

  constructor(
    private transactionService: TransactionService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.sub = this.transactionService.transactions$.subscribe({
      next: (data) => {
        try {
          const dataStr = JSON.stringify(data || []);
          if (dataStr === this.lastDataHash) {
            return;
          }
          this.lastDataHash = dataStr;

          this.transactions = data || [];
          this.total = this.transactions.length;

          this.success = this.transactions.filter(
            t => t.status === 'SUCCESS'
          ).length;

          this.suspicious = this.transactions.filter(
            t => t.status === 'SUSPICIOUS'
          ).length;

          this.blocked = this.transactions.filter(
            t => t.status === 'BLOCKED'
          ).length;

          this.cdr.detectChanges();
        } catch (e) {
          console.error("❌ FILTER ERROR:", e);
        }
      }
    });
  }

  ngOnDestroy(): void {
    if (this.sub) {
      this.sub.unsubscribe();
    }
  }

  exportToExcel() {
    const worksheet = XLSX.utils.json_to_sheet(this.transactions);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Transactions');
    XLSX.writeFile(workbook, 'transactions.xlsx');
  }
}
