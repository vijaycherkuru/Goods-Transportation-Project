import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface NotificationMessage {
  id: string;
  title: string;
  message: string;
  type: 'success' | 'error' | 'warning' | 'info';
  timestamp: Date;
  read: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private socket: any = null;
  private stompClient: any = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectInterval = 3000; // 3 seconds

  // Notification subjects
  private notificationsSubject = new BehaviorSubject<NotificationMessage[]>([]);
  public notifications$ = this.notificationsSubject.asObservable();

  // Connection status
  private connectionStatusSubject = new BehaviorSubject<'connected' | 'disconnected' | 'connecting'>('disconnected');
  public connectionStatus$ = this.connectionStatusSubject.asObservable();

  constructor() {
    // Load SockJS and STOMP libraries dynamically
    this.loadSockJSAndSTOMP();
  }

  private loadSockJSAndSTOMP(): void {
    // Load SockJS
    if (!(window as any).SockJS) {
      const sockjsScript = document.createElement('script');
      sockjsScript.src = 'https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js';
      document.head.appendChild(sockjsScript);
    }

    // Load STOMP
    if (!(window as any).Stomp) {
      const stompScript = document.createElement('script');
      stompScript.src = 'https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js';
      document.head.appendChild(stompScript);
    }
  }

  connect(userId: string): void {
    if (this.stompClient && this.stompClient.connected) {
      console.log('STOMP client already connected');
      return;
    }

    this.connectionStatusSubject.next('connecting');
    
    try {
      // Wait for libraries to load
      setTimeout(() => {
        this.initializeSTOMPConnection(userId);
      }, 1000);
    } catch (error) {
      console.error('Failed to create STOMP connection:', error);
      this.connectionStatusSubject.next('disconnected');
    }
  }

  private initializeSTOMPConnection(userId: string): void {
    const SockJS = (window as any).SockJS;
    const Stomp = (window as any).Stomp;

    if (!SockJS || !Stomp) {
      console.error('SockJS or STOMP not loaded');
      this.connectionStatusSubject.next('disconnected');
      return;
    }

    console.log('Attempting to connect to SockJS endpoint...');
    
    // Create SockJS connection
    this.socket = new SockJS('http://localhost:8087/ws');
    
    // Create STOMP client
    this.stompClient = Stomp.over(this.socket);
    
    // Disable STOMP debug logging
    this.stompClient.debug = null;

    // Connect to STOMP broker
    this.stompClient.connect({}, 
      (frame: any) => {
        console.log('STOMP connected successfully:', frame);
        this.connectionStatusSubject.next('connected');
        this.reconnectAttempts = 0;
        
        // Subscribe to user-specific notifications
        this.subscribeToUserNotifications(userId);
      },
      (error: any) => {
        console.error('STOMP connection error:', error);
        this.connectionStatusSubject.next('disconnected');
        
        // Attempt to reconnect
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
          this.attemptReconnect(userId);
        }
      }
    );
  }

  private subscribeToUserNotifications(userId: string): void {
    if (!this.stompClient || !this.stompClient.connected) {
      console.error('STOMP client not connected');
      return;
    }

    console.log('Subscribing to notifications for user:', userId);
    
    // Subscribe to user-specific queues
    const subscriptions = [
      `/user/${userId}/queue/notifications`,
      `/user/${userId}/queue/driver`,
      `/user/${userId}/queue/updates`,
      `/user/${userId}/queue/tracking`,
      `/user/${userId}/queue/payments`
    ];

    subscriptions.forEach(destination => {
      this.stompClient.subscribe(destination, (message: any) => {
        console.log('Received message from', destination, ':', message.body);
        try {
          const data = JSON.parse(message.body);
          this.handleIncomingMessage(data);
        } catch (error) {
          console.error('Error parsing message:', error);
          // Handle plain text messages
          this.handleIncomingMessage({ message: message.body });
        }
      });
    });

    // Subscribe to general driver notifications
    this.stompClient.subscribe('/topic/driver-notifications', (message: any) => {
      console.log('Received driver notification:', message.body);
      try {
        const data = JSON.parse(message.body);
        this.handleIncomingMessage(data);
      } catch (error) {
        this.handleIncomingMessage({ message: message.body });
      }
    });

    console.log('All subscriptions created successfully');
  }

  private handleIncomingMessage(data: any): void {
    console.log('Processing WebSocket message:', data);
    
    // Create notification object
    const notification: NotificationMessage = {
      id: this.generateNotificationId(),
      title: this.extractTitle(data),
      message: this.extractMessage(data),
      type: this.determineNotificationType(data),
      timestamp: new Date(),
      read: false
    };

    console.log('Created notification:', notification);

    // Add to notifications list
    const currentNotifications = this.notificationsSubject.value;
    this.notificationsSubject.next([notification, ...currentNotifications]);

    // Show browser notification if supported
    this.showBrowserNotification(notification);
  }

  private extractTitle(data: any): string {
    if (data.title) return data.title;
    if (data.type) return data.type;
    return 'New Notification';
  }

  private extractMessage(data: any): string {
    if (data.message) return data.message;
    if (data.content) return data.content;
    if (typeof data === 'string') return data;
    return 'You have a new notification';
  }

  private determineNotificationType(data: any): 'success' | 'error' | 'warning' | 'info' {
    if (data.type) {
      switch (data.type.toLowerCase()) {
        case 'success': return 'success';
        case 'error': return 'error';
        case 'warning': return 'warning';
        default: return 'info';
      }
    }
    
    // Determine type based on message content
    const message = this.extractMessage(data).toLowerCase();
    if (message.includes('accepted') || message.includes('success')) return 'success';
    if (message.includes('error') || message.includes('failed')) return 'error';
    if (message.includes('warning') || message.includes('pending')) return 'warning';
    return 'info';
  }

  private showBrowserNotification(notification: NotificationMessage): void {
    if ('Notification' in window && Notification.permission === 'granted') {
      new Notification(notification.title, {
        body: notification.message,
        icon: '/assets/icon-192x192.png',
        badge: '/assets/icon-192x192.png',
        tag: notification.id
      });
    }
  }

  private generateNotificationId(): string {
    return Date.now().toString() + Math.random().toString(36).substr(2, 9);
  }

  private attemptReconnect(userId: string): void {
    this.reconnectAttempts++;
    console.log(`Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`);
    
    setTimeout(() => {
      this.connect(userId);
    }, this.reconnectInterval);
  }

  disconnect(): void {
    if (this.stompClient) {
      this.stompClient.disconnect();
      this.stompClient = null;
    }
    if (this.socket) {
      this.socket.close();
      this.socket = null;
    }
    this.connectionStatusSubject.next('disconnected');
  }

  // Test WebSocket connection
  testConnection(): void {
    console.log('Testing STOMP WebSocket connection...');
    // Directly attempt STOMP connection with a test user (no health check)
    this.connect('test-user');
  }

  // Test notification endpoint
  testNotification(userId: string): void {
    console.log('Testing notification for user:', userId);
    
    const testPayload = {
      userId: userId,
      message: `Test notification for user ${userId} at ${new Date().toLocaleTimeString()}`,
      title: 'Test Notification',
      path: '/queue/notifications'
    };

    const token = localStorage.getItem('authToken');
    fetch('http://localhost:8087/api/notifications/user/custom', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
      },
      body: JSON.stringify(testPayload)
    })
    .then(response => {
      console.log('Test notification response:', response.status);
      if (response.ok) {
        console.log('✅ Test notification sent successfully');
      } else {
        console.error('❌ Test notification failed');
      }
    })
    .catch(error => {
      console.error('❌ Test notification error:', error);
    });
  }

  // Get current user ID from localStorage
  getCurrentUserId(): string | null {
    const userId = localStorage.getItem('userId');
    console.log('Current user ID from localStorage:', userId);
    return userId;
  }

  // Get connection status
  getConnectionStatus(): 'connected' | 'disconnected' | 'connecting' {
    return this.connectionStatusSubject.value;
  }

  // Request browser notification permission
  requestNotificationPermission(): Promise<boolean> {
    if ('Notification' in window) {
      return Notification.requestPermission().then(permission => {
        return permission === 'granted';
      });
    }
    return Promise.resolve(false);
  }

  // Mark notification as read
  markAsRead(notificationId: string): void {
    const currentNotifications = this.notificationsSubject.value;
    const updatedNotifications = currentNotifications.map(notification =>
      notification.id === notificationId ? { ...notification, read: true } : notification
    );
    this.notificationsSubject.next(updatedNotifications);
  }

  // Clear all notifications
  clearAllNotifications(): void {
    this.notificationsSubject.next([]);
  }

  // Get unread notifications count
  getUnreadCount(): Observable<number> {
    return new Observable(observer => {
      this.notifications$.subscribe(notifications => {
        const unreadCount = notifications.filter(n => !n.read).length;
        observer.next(unreadCount);
      });
    });
  }
} 