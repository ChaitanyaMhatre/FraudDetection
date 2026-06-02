import { Injectable } from '@angular/core';
import * as Stomp from 'stompjs';

@Injectable({
  providedIn: 'root'
})
export class WebsocketService {

  private stompClient: any;

  connect(onMessageReceived: (msg: string) => void) {

    const socket = new WebSocket('ws://localhost:8080/ws');

    this.stompClient = Stomp.over(socket);

    // Disable logs
    this.stompClient.debug = null;

    this.stompClient.connect({}, (frame: any) => {
      console.log('✅ CONNECTED:', frame);

      this.stompClient.subscribe('/topic/fraud-alerts', (message: any) => {
        console.log("📩 MESSAGE:", message.body);

        onMessageReceived(message.body);  // 🔥 trigger UI update
      });

    }, (error: any) => {
      console.error('❌ WebSocket Error:', error);
    });
  }
}
