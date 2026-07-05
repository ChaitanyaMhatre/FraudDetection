import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { API_BASE_URL } from '../config';

@Injectable({
  providedIn: 'root'
})
export class TransactionService {

  private apiUrl = `${API_BASE_URL}/api/transactions`;

  private transactionsSubject = new BehaviorSubject<any[]>([]);
  public transactions$: Observable<any[]> = this.transactionsSubject.asObservable();

  constructor(private http: HttpClient) {
    this.pollTransactions();
    setInterval(() => {
      this.pollTransactions();
    }, 3000);
  }

  private pollTransactions() {
    this.http.get<any[]>(`${this.apiUrl}/all`).subscribe({
      next: (data) => {
        this.transactionsSubject.next(data || []);
      },
      error: (err) => {
        console.error("❌ Error polling transactions:", err);
      }
    });
  }

  // ✅ GET ALL TRANSACTIONS
  async getAllTransactions(): Promise<any[]> {
    return this.transactionsSubject.value;
  }
}
