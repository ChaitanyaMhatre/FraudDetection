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
        let displayMsg = msg;
        try {
          const parsed = JSON.parse(msg);
          if (parsed && parsed.message) {
            displayMsg = parsed.message;
          }
        } catch (e) {
          // Fallback to raw message if it's not JSON
        }
        this.alerts = [displayMsg, ...this.alerts];   // create new array (important)
        this.cd.detectChanges();              // force refresh
      });
    });
  }
}
