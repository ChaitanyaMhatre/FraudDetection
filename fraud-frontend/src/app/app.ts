import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AlertsComponent } from './alerts/alerts';
import { TransactionsComponent } from './transactions/transactions';
import { ChartsComponent } from './charts/charts';
import { LoginComponent } from './login/login';
import { MapComponent } from './map/map';

import { AuthService } from './services/auth.service';
import { UserDashboardComponent }
from './user-dashboard/user-dashboard';
@Component({
  selector: 'app-root',
  standalone: true,

  imports: [
    CommonModule,

    AlertsComponent,
    TransactionsComponent,
    ChartsComponent,
    LoginComponent,
    UserDashboardComponent,
    MapComponent
  ],

  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class AppComponent {
  activeTab: string = 'dashboard';

  constructor(public authService: AuthService) {}

  selectTab(tab: string) {
    this.activeTab = tab;
  }

  // ✅ Logout method
  logout() {

    this.authService.logout();

    location.reload();
  }
}
