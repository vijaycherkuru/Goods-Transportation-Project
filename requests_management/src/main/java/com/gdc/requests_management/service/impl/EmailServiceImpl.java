package com.gdc.requests_management.service.impl;

import com.gdc.requests_management.service.EmailService;
import com.gdc.requests_management.repository.RequestRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final RequestRepository requestRepo;

    @Value("${spring.mail.username:noreply@goodstransportsystem.com}")
    private String fromEmail;

    @Value("${app.support.email:support@goodstransportsystem.com}")
    private String supportEmail;

    @Value("${app.company.phone:+1-800-TRANSPORT}")
    private String supportPhone;

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(to);
            helper.setFrom(fromEmail, "Goods Transport System");
            helper.setSubject(subject);
            helper.setText(body, true);
            javaMailSender.send(mimeMessage);
            log.info("Email sent successfully to: {}", to);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }

    @Override
    public void sendDriverRequestNotification(String to, String requestId, String from, String toLocation, String goodsDescription, String fare, String acceptUrl, String rejectUrl) {
        String safeTo = to != null ? to : "";
        String safeRequestId = requestId != null ? requestId : "N/A";
        String safeFrom = from != null ? from : "Unknown";
        String safeToLocation = toLocation != null ? toLocation : "Unknown";
        String safeGoodsDescription = goodsDescription != null ? goodsDescription : "N/A";
        String safeFare = fare != null ? fare : "0.00";
        String safeAcceptUrl = acceptUrl != null ? acceptUrl : "";
        String safeRejectUrl = rejectUrl != null ? rejectUrl : "";

        log.info("Sending driver notification with: to={}, requestId={}, from={}, toLocation={}, goodsDescription={}, fare={}, acceptUrl={}, rejectUrl={}",
                safeTo, safeRequestId, safeFrom, safeToLocation, safeGoodsDescription, safeFare, safeAcceptUrl, safeRejectUrl);

        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("""
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>New Ride Request</title>
            <style>
                * {
                    margin: 0;
                    padding: 0;
                    box-sizing: border-box;
                }
                body {
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                    padding: 20px;
                    line-height: 1.6;
                }
                .email-container {
                    max-width: 600px;
                    margin: 0 auto;
                    background: rgba(255, 255, 255, 0.95);
                    backdrop-filter: blur(10px);
                    border-radius: 20px;
                    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
                    overflow: hidden;
                    border: 1px solid rgba(255, 255, 255, 0.2);
                }
                .header {
                    background: linear-gradient(135deg, #ff6b6b, #ee5a52);
                    padding: 40px 30px;
                    text-align: center;
                    position: relative;
                    overflow: hidden;
                }
                .truck-icon {
                    font-size: 64px;
                    margin-bottom: 15px;
                    animation: bounce 2s infinite;
                }
                @keyframes bounce {
                    0%, 20%, 50%, 80%, 100% { transform: translateY(0); }
                    40% { transform: translateY(-10px); }
                    60% { transform: translateY(-5px); }
                }
                .header h1 {
                    color: white;
                    font-size: 32px;
                    margin-bottom: 10px;
                    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
                }
                .content {
                    padding: 40px 30px;
                }
                .greeting {
                    font-size: 24px;
                    color: #2c3e50;
                    margin-bottom: 25px;
                    font-weight: 600;
                }
                .wave {
                    display: inline-block;
                    animation: wave 2s infinite;
                }
                @keyframes wave {
                    0%, 100% { transform: rotate(0deg); }
                    25% { transform: rotate(20deg); }
                    75% { transform: rotate(-20deg); }
                }
                .notification-text {
                    background: linear-gradient(135deg, #d4edda, #c3e6cb);
                    border: 1px solid #c3e6cb;
                    border-radius: 15px;
                    padding: 20px;
                    margin-bottom: 30px;
                    color: #155724;
                    font-size: 16px;
                    font-weight: 500;
                }
                .request-details {
                    background: linear-gradient(135deg, #ffffff, #f8f9fa);
                    border-radius: 15px;
                    padding: 30px;
                    margin: 25px 0;
                    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
                    border: 1px solid #e9ecef;
                }
                .detail-item {
                    display: flex;
                    align-items: center;
                    margin-bottom: 20px;
                    padding: 15px;
                    background: rgba(102, 126, 234, 0.1);
                    border-radius: 12px;
                    transition: all 0.3s ease;
                    border-left: 4px solid #667eea;
                }
                .detail-item:hover {
                    background: rgba(102, 126, 234, 0.15);
                    transform: translateX(5px);
                }
                .detail-icon {
                    width: 45px;
                    height: 45px;
                    background: linear-gradient(135deg, #667eea, #764ba2);
                    border-radius: 50%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    margin-right: 15px;
                    font-size: 20px;
                    color: white;
                    box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
                }
                .detail-content {
                    flex: 1;
                }
                .detail-label {
                    font-weight: 600;
                    color: #2c3e50;
                    font-size: 14px;
                    text-transform: uppercase;
                    letter-spacing: 0.5px;
                    margin-bottom: 5px;
                }
                .detail-value {
                    color: #34495e;
                    font-size: 16px;
                    font-weight: 500;
                }
                .action-section {
                    background: linear-gradient(135deg, #fff3cd, #ffeaa7);
                    border-radius: 15px;
                    padding: 30px;
                    margin: 25px 0;
                    text-align: center;
                    border: 1px solid #fdcb6e;
                }
                .action-text {
                    color: #856404;
                    font-size: 16px;
                    font-weight: 500;
                    margin-bottom: 25px;
                }
                .button-container {
                    display: flex;
                    gap: 15px;
                    justify-content: center;
                    flex-wrap: wrap;
                }
                .btn {
                    display: inline-block;
                    padding: 15px 30px;
                    border-radius: 50px;
                    text-decoration: none;
                    font-weight: 600;
                    font-size: 16px;
                    transition: all 0.3s ease;
                    text-align: center;
                    min-width: 180px;
                    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
                }
                .btn-accept {
                    background: linear-gradient(135deg, #00b894, #00a085);
                    color: white;
                }
                .btn-accept:hover {
                    background: linear-gradient(135deg, #00a085, #00b894);
                    transform: translateY(-2px);
                    box-shadow: 0 6px 20px rgba(0, 184, 148, 0.4);
                }
                .btn-reject {
                    background: linear-gradient(135deg, #d63031, #e17055);
                    color: white;
                }
                .btn-reject:hover {
                    background: linear-gradient(135deg, #e17055, #d63031);
                    transform: translateY(-2px);
                    box-shadow: 0 6px 20px rgba(214, 48, 49, 0.4);
                }
                .footer {
                    background: linear-gradient(135deg, #2d3436, #636e72);
                    color: white;
                    padding: 30px;
                    display: flex;
                    justify-content: space-between;
                    align-items: flex-start;
                    flex-wrap: wrap;
                    gap: 20px;
                }
                .footer div {
                    flex: 1;
                    min-width: 200px;
                }
                .support-info {
                    text-align: right;
                }
                .support-info a {
                    color: #74b9ff;
                    text-decoration: none;
                }
                .support-info a:hover {
                    color: #0984e3;
                    text-decoration: underline;
                }
                @media (max-width: 600px) {
                    .email-container {
                        margin: 10px;
                        border-radius: 15px;
                    }
                    .content {
                        padding: 25px 20px;
                    }
                    .detail-item {
                        flex-direction: column;
                        text-align: center;
                    }
                    .detail-icon {
                        margin-right: 0;
                        margin-bottom: 10px;
                    }
                    .button-container {
                        flex-direction: column;
                        align-items: center;
                    }
                    .btn {
                        min-width: 250px;
                    }
                    .footer {
                        flex-direction: column;
                        text-align: center;
                    }
                    .support-info {
                        text-align: center;
                    }
                }
            </style>
        </head>
        <body>
            <div class="email-container">
                <div class="header">
                    <div class="truck-icon">🚛</div>
                    <h1>New Ride Request</h1>
                </div>
                <div class="content">
                    <div class="greeting">
                        Hello """);
        bodyBuilder.append(extractUsername(safeTo)).append("""
                        ! <span class="wave">👋</span>
                    </div>
                    <div class="notification-text">
                        🎉 Great news! You have been assigned a new ride request that matches your route and schedule.
                    </div>
                    <div class="request-details">
                        <div class="detail-item">
                            <div class="detail-icon">🆔</div>
                            <div class="detail-content">
                                <div class="detail-label">Request ID</div>
                                <div class="detail-value">""").append(safeRequestId).append("""
                                </div>
                            </div>
                        </div>
                        <div class="detail-item">
                            <div class="detail-icon">📍</div>
                            <div class="detail-content">
                                <div class="detail-label">Pickup Location</div>
                                <div class="detail-value">""").append(safeFrom).append("""
                                </div>
                            </div>
                        </div>
                        <div class="detail-item">
                            <div class="detail-icon">🎯</div>
                            <div class="detail-content">
                                <div class="detail-label">Destination</div>
                                <div class="detail-value">""").append(safeToLocation).append("""
                                </div>
                            </div>
                        </div>
                        <div class="detail-item">
                            <div class="detail-icon">📦</div>
                            <div class="detail-content">
                                <div class="detail-label">Goods Description</div>
                                <div class="detail-value">""").append(safeGoodsDescription).append("""
                                </div>
                            </div>
                        </div>
                        <div class="detail-item">
                            <div class="detail-icon">💰</div>
                            <div class="detail-content">
                                <div class="detail-label">Estimated Fare</div>
                                <div class="detail-value">₹""").append(safeFare).append("""
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="action-section">
                        <div class="action-text">
                            ⏰ Please respond to this request promptly to confirm your availability.
                        </div>
                        <div class="button-container">
                            <a href=\"""").append(safeAcceptUrl).append("""
                            " class="btn btn-accept">
                                ✅ Accept Request
                            </a>
                            <a href=\"""").append(safeRejectUrl).append("""
                            " class="btn btn-reject">
                                ❌ Decline Request
                            </a>
                        </div>
                    </div>
                </div>
                <div class="footer">
                    <div>
                        <strong>Goods Transport System</strong><br>
                        Your trusted logistics partner
                    </div>
                    <div class="support-info">
                        <strong>Need Help?</strong><br>
                        📧 Email: <a href="mailto:""").append(supportEmail).append("\">").append(supportEmail).append("""
                        </a><br>
                        📞 Phone: <a href="tel:""").append(supportPhone).append("\">").append(supportPhone).append("""
                        </a>
                    </div>
                </div>
            </div>
        </body>
        </html>
        """);

        String body = bodyBuilder.toString();
        sendEmail(safeTo, "🚚 New Ride Assigned: Action Required", body);
    }

    @Override
    public void sendUserConfirmation(String to, String requestId, String from, String toLocation, String goodsDescription) {
        String safeTo = to != null ? to : "";
        String safeRequestId = requestId != null ? requestId : "N/A";
        String safeFrom = from != null ? from : "Unknown";
        String safeToLocation = toLocation != null ? toLocation : "Unknown";
        String safeGoodsDescription = goodsDescription != null ? goodsDescription : "N/A";
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"));

        log.info("sendUserConfirmation args: to={}, requestId={}, from={}, toLocation={}, goodsDescription={}",
                safeTo, safeRequestId, safeFrom, safeToLocation, safeGoodsDescription);

        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("""
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Request Confirmation</title>
            <style>
                * {
                    margin: 0;
                    padding: 0;
                    box-sizing: border-box;
                }
                body {
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    background: linear-gradient(135deg, #74b9ff 0%, #0984e3 100%);
                    padding: 20px;
                    line-height: 1.6;
                }
                .email-container {
                    max-width: 600px;
                    margin: 0 auto;
                    background: rgba(255, 255, 255, 0.95);
                    backdrop-filter: blur(10px);
                    border-radius: 20px;
                    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
                    overflow: hidden;
                    border: 1px solid rgba(255, 255, 255, 0.2);
                }
                .header {
                    background: linear-gradient(135deg, #00b894, #00a085);
                    padding: 40px 30px;
                    text-align: center;
                    position: relative;
                    overflow: hidden;
                }
                .header::before {
                    content: '';
                    position: absolute;
                    top: 0;
                    left: 0;
                    right: 0;
                    bottom: 0;
                    background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnitsOnUse="userSpaceOnUse"><circle cx="25" cy="25" r="2" fill="rgba(255,255,255,0.1)"/><circle cx="75" cy="75" r="1.5" fill="rgba(255,255,255,0.1)"/><circle cx="50" cy="10" r="1" fill="rgba(255,255,255,0.1)"/><circle cx="10" cy="60" r="1.5" fill="rgba(255,255,255,0.1)"/><circle cx="90" cy="30" r="1" fill="rgba(255,255,255,0.1)"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
                    opacity: 0.3;
                }
                .success-icon {
                    font-size: 64px;
                    margin-bottom: 15px;
                    position: relative;
                    z-index: 1;
                    animation: pulse 2s infinite;
                }
                @keyframes pulse {
                    0% { transform: scale(1); }
                    50% { transform: scale(1.1); }
                    100% { transform: scale(1); }
                }
                .header h1 {
                    color: white;
                    font-size: 32px;
                    margin-bottom: 10px;
                    position: relative;
                    z-index: 1;
                    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
                }
                .header p {
                    color: rgba(255, 255, 255, 0.9);
                    font-size: 16px;
                    position: relative;
                    z-index: 1;
                }
                .content {
                    padding: 40px 30px;
                }
                .greeting {
                    font-size: 24px;
                    color: #2c3e50;
                    margin-bottom: 25px;
                    font-weight: 600;
                }
                .confirmation-message {
                    background: linear-gradient(135deg, #d4edda, #c3e6cb);
                    border: 1px solid #00b894;
                    border-radius: 15px;
                    padding: 25px;
                    margin-bottom: 30px;
                    position: relative;
                }
                .confirmation-message::before {
                    content: '✅';
                    position: absolute;
                    top: -10px;
                    left: 20px;
                    background: white;
                    padding: 5px 10px;
                    border-radius: 50px;
                    font-size: 20px;
                    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
                }
                .confirmation-text {
                    color: #155724;
                    font-size: 18px;
                    font-weight: 500;
                    margin-top: 10px;
                }
                .timestamp {
                    color: #6c757d;
                    font-size: 14px;
                    margin-top: 10px;
                    font-style: italic;
                }
                .request-summary {
                    background: linear-gradient(135deg, #ffffff, #f8f9fa);
                    border-radius: 15px;
                    padding: 30px;
                    margin: 25px 0;
                    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
                    border: 1px solid #e9ecef;
                }
                .summary-title {
                    font-size: 20px;
                    color: #2c3e50;
                    margin-bottom: 20px;
                    font-weight: 600;
                    text-align: center;
                    border-bottom: 2px solid #00b894;
                    padding-bottom: 10px;
                }
                .detail-row {
                    display: flex;
                    align-items: center;
                    margin-bottom: 18px;
                    padding: 15px;
                    background: rgba(0, 184, 148, 0.05);
                    border-radius: 12px;
                    transition: all 0.3s ease;
                    border-left: 4px solid #00b894;
                }
                .detail-row:hover {
                    background: rgba(0, 184, 148, 0.1);
                    transform: translateX(5px);
                }
                .detail-icon {
                    width: 45px;
                    height: 45px;
                    background: linear-gradient(135deg, #00b894, #00a085);
                    border-radius: 50%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    margin-right: 15px;
                    font-size: 20px;
                    color: white;
                    box-shadow: 0 4px 15px rgba(0, 184, 148, 0.3);
                }
                .detail-text {
                    flex: 1;
                }
                .detail-label {
                    font-weight: 600;
                    color: #2c3e50;
                    font-size: 14px;
                    text-transform: uppercase;
                    letter-spacing: 0.5px;
                    margin-bottom: 5px;
                }
                .detail-value {
                    color: #34495e;
                    font-size: 16px;
                    font-weight: 500;
                }
                .status-section {
                    background: linear-gradient(135deg, #fff3cd, #ffeaa7);
                    border-radius: 15px;
                    padding: 25px;
                    margin: 25px 0;
                    text-align: center;
                    border: 1px solid #fdcb6e;
                }
                .status-icon {
                    font-size: 48px;
                    margin-bottom: 15px;
                    animation: rotate 3s linear infinite;
                }
                @keyframes rotate {
                    from { transform: rotate(0deg); }
                    to { transform: rotate(360deg); }
                }
                .status-text {
                    color: #856404;
                    font-size: 16px;
                    font-weight: 500;
                    margin-bottom: 10px;
                }
                .status-subtext {
                    color: #6c757d;
                    font-size: 14px;
                }
                .footer {
                    background: linear-gradient(135deg, #2d3436, #636e72);
                    color: white;
                    padding: 30px;
                    text-align: center;
                }
                .footer-title {
                    font-size: 18px;
                    font-weight: 600;
                    margin-bottom: 15px;
                }
                .footer-subtitle {
                    color: #b2bec3;
                    font-size: 14px;
                    margin-bottom: 20px;
                }
                .support-box {
                    background: rgba(255, 255, 255, 0.1);
                    border-radius: 12px;
                    padding: 20px;
                    margin-top: 20px;
                }
                .support-title {
                    font-weight: 600;
                    margin-bottom: 10px;
                    color: #ddd;
                }
                .support-item {
                    margin: 8px 0;
                    color: #b2bec3;
                }
                .support-item a {
                    color: #74b9ff;
                    text-decoration: none;
                    font-weight: 500;
                }
                .support-item a:hover {
                    color: #0984e3;
                    text-decoration: underline;
                }
                @media (max-width: 600px) {
                    .email-container {
                        margin: 10px;
                        border-radius: 15px;
                    }
                    .content {
                        padding: 25px 20px;
                    }
                    .detail-row {
                        flex-direction: column;
                        text-align: center;
                    }
                    .detail-icon {
                        margin-right: 0;
                        margin-bottom: 10px;
                    }
                    .header {
                        padding: 30px 20px;
                    }
                    .header h1 {
                        font-size: 24px;
                    }
                }
            </style>
        </head>
        <body>
            <div class="email-container">
                <div class="header">
                    <div class="success-icon">✅</div>
                    <h1>Request Created Successfully!</h1>
                    <p>Your transport request has been submitted and is being processed</p>
                </div>
                <div class="content">
                    <div class="greeting">
                        Dear """).append(extractUsername(safeTo)).append("""
                    </div>
                    <div class="confirmation-message">
                        <div class="confirmation-text">
                            🎉 Congratulations! Your ride request has been successfully created and submitted to our system.
                        </div>
                        <div class="timestamp">
                            Created on: """).append(time).append("""
                        </div>
                    </div>
                    <div class="request-summary">
                        <div class="summary-title">📋 Request Summary</div>
                        <div class="detail-row">
                            <div class="detail-icon">🆔</div>
                            <div class="detail-text">
                                <div class="detail-label">Request ID</div>
                                <div class="detail-value">""").append(safeRequestId).append("""
                                </div>
                            </div>
                        </div>
                        <div class="detail-row">
                            <div class="detail-icon">📍</div>
                            <div class="detail-text">
                                <div class="detail-label">Pickup Location</div>
                                <div class="detail-value">""").append(safeFrom).append("""
                                </div>
                            </div>
                        </div>
                        <div class="detail-row">
                            <div class="detail-icon">🎯</div>
                            <div class="detail-text">
                                <div class="detail-label">Destination</div>
                                <div class="detail-value">""").append(safeToLocation).append("""
                                </div>
                            </div>
                        </div>
                        <div class="detail-row">
                            <div class="detail-icon">📦</div>
                            <div class="detail-text">
                                <div class="detail-label">Goods Description</div>
                                <div class="detail-value">""").append(safeGoodsDescription).append("""
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="status-section">
                        <div class="status-icon">⏳</div>
                        <div class="status-text">
                            Your request is currently being processed
                        </div>
                        <div class="status-subtext">
                            We'll notify you once a driver has been assigned to your request.
                            For any immediate concerns, please contact our support team.
                        </div>
                    </div>
                </div>
                <div class="footer">
                    <div class="footer-title">Goods Transport System</div>
                    <div class="footer-subtitle">Your trusted logistics partner</div>
                    <div class="support-box">
                        <div class="support-title">Need Assistance?</div>
                        <div class="support-item">
                            📧 Email: <a href="mailto:""").append(supportEmail).append("\">").append(supportEmail).append("""
                            </a>
                        </div>
                        <div class="support-item">
                            📞 Phone: <a href="tel:""").append(supportPhone).append("\">").append(supportPhone).append("""
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </body>
        </html>
        """);

        String body = bodyBuilder.toString();
        sendEmail(safeTo, "🚚 Your Transport Request Confirmation", body);
    }

    @Scheduled(fixedRate = 30000)
    public void autoRejectPendingRequests() {
        // Pseudo logic: loop unresponded requests, mark as rejected
        // and email user
        // Example:
        // List<Request> pending = requestRepo.findUnrespondedOlderThan(1 minute);
        // for (Request req : pending) {
        //     requestService.rejectRequest(req.getId(), req.getDriverId(), "Auto-rejected");
        //     sendEmail(userEmail, "Driver did not respond", "Request %s was auto-rejected due to no driver response.");
        // }
    }

    private String extractUsername(String email) {
        if (email == null || !email.contains("@")) {
            return "Valued Customer";
        }
        String username = email.substring(0, email.indexOf('@'));
        return username.substring(0, 1).toUpperCase() + username.substring(1);
    }
}