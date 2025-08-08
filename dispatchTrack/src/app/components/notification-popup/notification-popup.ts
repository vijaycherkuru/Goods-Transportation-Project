import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WebSocketService, NotificationMessage } from '../../services/websocket.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-notification-popup',
  templateUrl: './notification-popup.html',
  styleUrls: ['./notification-popup.scss'],
  standalone: true,
  imports: [CommonModule]
})
export class NotificationPopupComponent implements OnInit, OnDestroy {
  notifications: NotificationMessage[] = [];
  unreadCount = 0;
  showNotifications = false;
  connectionStatus: 'connected' | 'disconnected' | 'connecting' = 'disconnected';
  connectionStatus$;

  private notificationsSubscription: Subscription | null = null;
  private connectionSubscription: Subscription | null = null;
  private unreadCountSubscription: Subscription | null = null;

  constructor(public webSocketService: WebSocketService) {
    this.connectionStatus$ = this.webSocketService.connectionStatus$;
  }

  ngOnInit() {
    // Request notification permission
    this.webSocketService.requestNotificationPermission();

    // Subscribe to notifications
    this.notificationsSubscription = this.webSocketService.notifications$.subscribe(
      notifications => {
        this.notifications = notifications;
      }
    );

    // Subscribe to connection status
    this.connectionSubscription = this.webSocketService.connectionStatus$.subscribe(
      status => {
        this.connectionStatus = status;
      }
    );

    // Subscribe to unread count
    this.unreadCountSubscription = this.webSocketService.getUnreadCount().subscribe(
      count => {
        this.unreadCount = count;
      }
    );

    // Connect to WebSocket if user is logged in
    const userId = localStorage.getItem('userId');
    if (userId) {
      this.webSocketService.connect(userId);
    }
  }

  ngOnDestroy() {
    this.notificationsSubscription?.unsubscribe();
    this.connectionSubscription?.unsubscribe();
    this.unreadCountSubscription?.unsubscribe();
    this.webSocketService.disconnect();
  }

  toggleNotifications() {
    this.showNotifications = !this.showNotifications;
  }

  markAsRead(notification: NotificationMessage) {
    this.webSocketService.markAsRead(notification.id);
  }

  markAllAsRead() {
    this.notifications.forEach(notification => {
      this.webSocketService.markAsRead(notification.id);
    });
  }

  clearAllNotifications() {
    this.webSocketService.clearAllNotifications();
    this.showNotifications = false;
  }

  getNotificationIcon(type: string): string {
    switch (type) {
      case 'success': return 'bi-check-circle-fill';
      case 'error': return 'bi-x-circle-fill';
      case 'warning': return 'bi-exclamation-triangle-fill';
      case 'info': return 'bi-info-circle-fill';
      default: return 'bi-bell-fill';
    }
  }

  getNotificationColor(type: string): string {
    switch (type) {
      case 'success': return 'text-success';
      case 'error': return 'text-danger';
      case 'warning': return 'text-warning';
      case 'info': return 'text-info';
      default: return 'text-primary';
    }
  }

  getConnectionStatusIcon(): string {
    switch (this.connectionStatus) {
      case 'connected': return 'bi-wifi';
      case 'connecting': return 'bi-wifi-1';
      case 'disconnected': return 'bi-wifi-off';
      default: return 'bi-wifi-off';
    }
  }

  getConnectionStatusColor(): string {
    switch (this.connectionStatus) {
      case 'connected': return 'text-success';
      case 'connecting': return 'text-warning';
      case 'disconnected': return 'text-danger';
      default: return 'text-danger';
    }
  }

  formatTime(timestamp: Date): string {
    const now = new Date();
    const diff = now.getTime() - timestamp.getTime();
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) return 'Just now';
    if (minutes < 60) return `${minutes}m ago`;
    if (hours < 24) return `${hours}h ago`;
    return `${days}d ago`;
  }

  testConnection() {
    this.webSocketService.testConnection();
  }

  testNotification() {
    const userId = this.webSocketService.getCurrentUserId();
    if (userId) {
      this.webSocketService.testNotification(userId);
    } else {
      console.error('No user ID found in localStorage');
    }
  }

  getCurrentUserId(): string | null {
    return this.webSocketService.getCurrentUserId();
  }

  requestNotificationPermission() {
    this.webSocketService.requestNotificationPermission().then(granted => {
      if (granted) {
        console.log('Browser notifications enabled!');
      } else {
        console.warn('Browser notifications not granted.');
      }
    });
  }
} 