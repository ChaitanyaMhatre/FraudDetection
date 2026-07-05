import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_BASE_URL } from '../config';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private authUrl = `${API_BASE_URL}/api/auth`;

  constructor(private http: HttpClient) {}

  // ✅ LOGIN
  login(username: string, password: string): Observable<any> {
    return this.http.post(`${this.authUrl}/login`, { username, password }).pipe(
      tap((res: any) => {
        localStorage.setItem('token', res.token);
        localStorage.setItem('loggedIn', 'true');
        localStorage.setItem('role', res.role);
        localStorage.setItem('name', res.name);
        localStorage.setItem('username', res.username);
        localStorage.setItem('id', res.id.toString());
      })
    );
  }

  // ✅ REGISTER / SIGNUP
  register(userData: { username: string; password: string; name: string; email: string }): Observable<any> {
    return this.http.post(`${this.authUrl}/register`, userData);
  }

  // ✅ LOGOUT
  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('loggedIn');
    localStorage.removeItem('role');
    localStorage.removeItem('name');
    localStorage.removeItem('username');
    localStorage.removeItem('id');
  }

  // ✅ LOGIN CHECK
  isLoggedIn(): boolean {
    return localStorage.getItem('loggedIn') === 'true';
  }

  // ✅ ROLE CHECK
  getRole(): string {
    return localStorage.getItem('role') || '';
  }

  // ✅ NAME GETTER
  getName(): string {
    return localStorage.getItem('name') || '';
  }
}


