import { Component, AfterViewInit, OnInit, OnDestroy, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { WebsocketService } from '../services/websocket.service';
import { API_BASE_URL } from '../config';

declare const L: any;

@Component({
  selector: 'app-map',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './map.html',
  styleUrls: ['./map.css']
})
export class MapComponent implements OnInit, AfterViewInit, OnDestroy {

  private map: any;
  private markers = new Map<number, any>();

  constructor(
    private http: HttpClient,
    private websocketService: WebsocketService,
    private zone: NgZone
  ) {}

  ngOnInit(): void {}

  ngAfterViewInit(): void {
    // Wrap Leaflet map initialization in a timeout to ensure container is rendered and sized
    setTimeout(() => {
      this.initMap();
    }, 100);
  }

  ngOnDestroy(): void {
    if (this.map) {
      this.map.remove();
    }
  }

  private initMap(): void {
    if (typeof L === 'undefined') {
      console.error('❌ Leaflet library is not loaded!');
      return;
    }

    // Initialize map centering over India region with reasonable default view
    this.map = L.map('map', {
      zoomControl: true,
      minZoom: 2,
      maxZoom: 18
    }).setView([20.5937, 78.9629], 5);

    // Apply beautiful dark themed tiles from CartoDB
    L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>'
    }).addTo(this.map);

    // Fetch previous transactions to populate map
    this.loadPreviousTransactions();

    // Subscribe to live WebSocket alerts
    this.setupLiveWebSocket();
  }

  private loadPreviousTransactions(): void {
    this.http.get<any[]>(`${API_BASE_URL}/api/transactions/all`).subscribe({
      next: (txs) => {
        txs.forEach(tx => this.plotTransactionMarker(tx, false));
      },
      error: (err) => console.error('Error fetching transactions for map:', err)
    });
  }

  private setupLiveWebSocket(): void {
    this.websocketService.connect((msg: string) => {
      this.zone.run(() => {
        try {
          const tx = JSON.parse(msg);
          if (tx && tx.id) {
            // Plot/update marker for live transaction
            this.plotTransactionMarker(tx, true);

            // Dynamic camera movement to focus the incoming threat/event
            if (tx.latitude && tx.longitude) {
              this.map.panTo([tx.latitude, tx.longitude]);
              const marker = this.markers.get(tx.id);
              if (marker) {
                setTimeout(() => {
                  marker.openPopup();
                }, 400);
              }
            }
          }
        } catch (e) {
          // Message was not JSON (e.g. legacy plain text fallback), ignore or log
        }
      });
    });
  }

  private plotTransactionMarker(tx: any, isLive: boolean): void {
    let lat = tx.latitude;
    let lng = tx.longitude;

    // Deterministic fallback coordinates if none are persisted (prevents clustering on 0,0)
    if (!lat || !lng || (lat === 0.0 && lng === 0.0)) {
      const seed = tx.id * 12345;
      lat = 8.4 + (seed % 290) / 10.0;
      lng = 68.7 + (seed % 280) / 10.0;
    }

    let colorClass = 'marker-safe';
    let statusText = 'SUCCESS';
    if (tx.status === 'SUSPICIOUS') {
      colorClass = 'marker-suspicious';
      statusText = 'SUSPICIOUS';
    } else if (tx.status === 'BLOCKED') {
      colorClass = 'marker-blocked';
      statusText = 'BLOCKED';
    }

    // Replace existing marker if it already exists (e.g., transition from SUSPICIOUS to SUCCESS on OTP verify)
    if (this.markers.has(tx.id)) {
      const existing = this.markers.get(tx.id);
      existing.remove();
      this.markers.delete(tx.id);
    }

    // Build custom Leaflet HTML markup representing the premium pulsing ripple dot
    const customIcon = L.divIcon({
      className: `custom-marker ${colorClass}`,
      html: `
        <div class="marker-pulse"></div>
        <div class="marker-dot"></div>
      `,
      iconSize: [16, 16],
      iconAnchor: [8, 8]
    });

    const reasonsList = tx.reasons && Array.isArray(tx.reasons) 
      ? tx.reasons.join(', ') 
      : (tx.reasons || 'N/A');

    const popupHtml = `
      <div class="popup-details">
        <span class="popup-status status-${tx.status?.toLowerCase()}">${statusText}</span><br>
        <strong>Transaction ID:</strong> #${tx.id}<br>
        <strong>Amount:</strong> ₹${tx.amount.toLocaleString()}<br>
        <strong>Sender ID:</strong> ${tx.senderId}<br>
        <strong>Receiver ID:</strong> ${tx.receiverId}<br>
        <strong>AI Confidence:</strong> ${tx.confidence ? tx.confidence + '%' : '0%'}<br>
        <strong>Reasons:</strong> ${reasonsList}
      </div>
    `;

    const marker = L.marker([lat, lng], { icon: customIcon })
      .bindPopup(popupHtml)
      .addTo(this.map);

    this.markers.set(tx.id, marker);
  }
}
