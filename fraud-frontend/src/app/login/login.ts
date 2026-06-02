import { Component } from '@angular/core';

import { CommonModule }
from '@angular/common';

import { FormsModule }
from '@angular/forms';

import { AuthService }
from '../services/auth.service';

@Component({
  selector: 'app-login',

  standalone: true,

  imports: [
    CommonModule,
    FormsModule
  ],

  templateUrl: './login.html',

  styleUrls: ['./login.css']
})

export class LoginComponent {

  username = '';

  password = '';

  errorMessage = '';

  constructor(
    public authService: AuthService
  ) {}

  login() {

    const success =
      this.authService.login(
        this.username,
        this.password
      );

    if (success) {

      console.log(
        "✅ LOGIN SUCCESS"
      );

      this.errorMessage = '';

      location.reload();

    } else {

      console.log(
        "❌ LOGIN FAILED"
      );

      this.errorMessage =
        'Invalid credentials';
    }
  }
}
