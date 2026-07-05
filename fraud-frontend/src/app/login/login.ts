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
  successMessage = '';
  loading = false;

  // 📝 SIGNUP FIELDS
  isSignupMode = false;
  name = '';
  email = '';

  constructor(
    public authService: AuthService
  ) {}

  toggleMode() {
    this.isSignupMode = !this.isSignupMode;
    this.errorMessage = '';
    this.successMessage = '';
  }

  login() {
    this.errorMessage = '';
    this.successMessage = '';
    this.loading = true;

    this.authService.login(this.username, this.password).subscribe({
      next: (res) => {
        this.loading = false;
        location.reload();
      },
      error: (err) => {
        this.loading = false;
        console.error(err);
        this.errorMessage = err.error?.message || 'Invalid credentials';
      }
    });
  }

  register() {
    this.errorMessage = '';
    this.successMessage = '';
    this.loading = true;

    const userData = {
      username: this.username,
      password: this.password,
      name: this.name,
      email: this.email
    };

    this.authService.register(userData).subscribe({
      next: (res: any) => {
        this.loading = false;
        this.successMessage = 'Registration successful! A verification email has been sent. Please check your inbox and verify your email before logging in.';
        this.isSignupMode = false;
        this.name = '';
        this.email = '';
      },
      error: (err) => {
        this.loading = false;
        console.error(err);
        this.errorMessage = err.error?.message || 'Registration failed';
      }
    });
  }
}
