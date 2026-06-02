import { Injectable }
from '@angular/core';

import { HttpClient }
from '@angular/common/http';

import {
  firstValueFrom
} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class TransactionService {

  private apiUrl =
    'http://localhost:8080/api/transactions';

  constructor(
    private http: HttpClient
  ) {}

  // ✅ GET ALL TRANSACTIONS
  async getAllTransactions():
    Promise<any[]> {

    return await firstValueFrom(

      this.http.get<any[]>(
        `${this.apiUrl}/all`
      )

    );
  }
}
