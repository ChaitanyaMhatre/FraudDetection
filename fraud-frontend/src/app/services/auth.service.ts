import { Injectable }
from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  // ✅ LOGIN page
  login(
    username: string,
    password: string
  ): boolean {

    // 👤 USER
    if (
      username === 'user'
      &&
      password === 'user123'
    ) {

      localStorage.setItem(
        'loggedIn',
        'true'
      );

      localStorage.setItem(
        'role',
        'USER'
      );

      return true;
    }

    // 🛡 ADMIN
    if (
      username === 'admin'
      &&
      password === 'admin123'
    ) {

      localStorage.setItem(
        'loggedIn',
        'true'
      );

      localStorage.setItem(
        'role',
        'ADMIN'
      );

      return true;
    }

    return false;
  }

  // ✅ LOGOUT
  logout() {

    localStorage.removeItem(
      'loggedIn'
    );

    localStorage.removeItem(
      'role'
    );
  }

  // ✅ LOGIN CHECK
  isLoggedIn(): boolean {

    return localStorage.getItem(
      'loggedIn'
    ) === 'true';
  }

  // ✅ ROLE CHECK
  getRole(): string {

    return localStorage.getItem(
      'role'
    ) || '';
  }
}


