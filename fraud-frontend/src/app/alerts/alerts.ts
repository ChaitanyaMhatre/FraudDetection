import { Component, OnInit, NgZone, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WebsocketService } from '../services/websocket.service';

@Component({
  selector: 'app-alerts',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './alerts.html',
  styleUrls: ['./alerts.css']
})
export class AlertsComponent implements OnInit {

  alerts: string[] = [];

  constructor(
    private websocketService: WebsocketService,
    private zone: NgZone,
    private cd: ChangeDetectorRef   // 🔥 IMPORTANT
  ) {}

  ngOnInit(): void {
    this.websocketService.connect((msg: string) => {

      console.log("🔥 UI RECEIVED:", msg);

      // 🔥 Force Angular UI update
      this.zone.run(() => {
        this.alerts = [msg, ...this.alerts];   // create new array (important)
        this.cd.detectChanges();              // force refresh
      });
    });
  }
}
