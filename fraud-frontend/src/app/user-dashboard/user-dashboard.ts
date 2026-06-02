import { Component }
from '@angular/core';

import { CommonModule }
from '@angular/common';

import { FormsModule }
from '@angular/forms';

import { HttpClient }
from '@angular/common/http';

@Component({
  selector: 'app-user-dashboard',

  standalone: true,

  imports: [
    CommonModule,
    FormsModule
  ],

  templateUrl:
    './user-dashboard.html',

  styleUrls:
    ['./user-dashboard.css']
})

export class UserDashboardComponent {

  senderId = 1;

  receiverId = 2;

  amount = 0;

  message = '';

  constructor(
    private http: HttpClient
  ) {}

  sendMoney() {

    const request = {

      senderId:
        this.senderId,

      receiverId:
        this.receiverId,

      amount:
        this.amount,

      transactionFrequency: 1,

      avgTransaction: 3000,

      deviceChange: 0,

      locationRisk: 0
    };

    this.http.post(

      'http://localhost:8080/api/transactions/send',

      request

    ).subscribe({

      next: (response: any) => {

        console.log(response);

        this.message =

          "✅ Transaction Status: "
          + response.status;
      },

      error: (err) => {

        console.error(err);

        this.message =
          "❌ Transaction Failed";
      }
    });
  }
}
