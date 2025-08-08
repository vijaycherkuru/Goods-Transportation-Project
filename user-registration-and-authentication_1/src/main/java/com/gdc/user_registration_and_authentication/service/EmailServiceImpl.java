package com.gdc.user_registration_and_authentication.service;

import com.gdc.user_registration_and_authentication.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username:noreply@goodstransportsystem.com}")
    private String fromEmail;

    @Value("${app.support.email:support@goodstransportsystem.com}")
    private String supportEmail;

    @Value("${app.company.phone:+1-800-TRANSPORT}")
    private String supportPhone;

    @Override
    public void sendOtpEmail(String to, String otp) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Enhanced email headers to avoid spam
            helper.setTo(to);
            helper.setFrom(fromEmail, "Goods Transport System Security Team");
            helper.setReplyTo(supportEmail);
            helper.setSubject("🔐 Secure Password Reset Request - Action Required");

            // Add custom headers to improve deliverability
            mimeMessage.setHeader("X-Priority", "1");
            mimeMessage.setHeader("X-MSMail-Priority", "High");
            mimeMessage.setHeader("X-Mailer", "Goods Transport System v2.0");
            mimeMessage.setHeader("List-Unsubscribe", "<mailto:" + supportEmail + ">");

            helper.setText(createEnhancedEmailContent(to, otp), true);

            javaMailSender.send(mimeMessage);
            log.info("✅ Password reset OTP email sent successfully to: {}", to);

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("❌ Failed to send email to: {}", to, e);
            throw new RuntimeException("Email delivery failed. Please try again or contact support.", e);
        }
    }

    public void sendEmailVerificationOtp(String to, String otp) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Enhanced email headers to avoid spam
            helper.setTo(to);
            helper.setFrom(fromEmail, "Goods Transport System - Welcome Team");
            helper.setReplyTo(supportEmail);
            helper.setSubject("🎉 Welcome to Goods Transport System - Verify Your Email");

            // Add custom headers to improve deliverability
            mimeMessage.setHeader("X-Priority", "1");
            mimeMessage.setHeader("X-MSMail-Priority", "High");
            mimeMessage.setHeader("X-Mailer", "Goods Transport System v2.0");
            mimeMessage.setHeader("List-Unsubscribe", "<mailto:" + supportEmail + ">");

            helper.setText(createEmailVerificationContent(to, otp), true);

            javaMailSender.send(mimeMessage);
            log.info("✅ Email verification OTP sent successfully to: {}", to);

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("❌ Failed to send verification email to: {}", to, e);
            throw new RuntimeException("Email verification failed. Please try again or contact support.", e);
        }
    }

    private String createEmailVerificationContent(String email, String otp) {
        String username = extractUsername(email);
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a"));
        String verificationId = "VERIFY-" + System.currentTimeMillis();

        return String.format("""
                        <!DOCTYPE html>
                        <html lang="en" xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:o="urn:schemas-microsoft-com:office:office">
                        <head>
                            <meta charset="utf-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <meta http-equiv="X-UA-Compatible" content="IE=edge">
                            <meta name="x-apple-disable-message-reformatting">
                            <meta name="format-detection" content="telephone=no,address=no,email=no,date=no,url=no">
                            <title>Welcome to Goods Transport System - Email Verification</title>
                        
                            <!--[if mso]>
                            <noscript>
                                <xml>
                                    <o:OfficeDocumentSettings>
                                        <o:AllowPNG/>
                                        <o:PixelsPerInch>96</o:PixelsPerInch>
                                    </o:OfficeDocumentSettings>
                                </xml>
                            </noscript>
                            <![endif]-->
                        
                            <style>
                                * { box-sizing: border-box; }
                                body { margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif; }
                                .email-container { max-width: 680px; margin: 0 auto; background: #ffffff; }
                                .content-block { padding: 0; }
                                .responsive-table { width: 100%%; border-collapse: collapse; }
                        
                                @media screen and (max-width: 600px) {
                                    .mobile-padding { padding: 20px 15px !important; }
                                    .mobile-text { font-size: 16px !important; line-height: 1.5 !important; }
                                    .mobile-otp { font-size: 28px !important; padding: 12px 15px !important; }
                                    .mobile-hide { display: none !important; }
                                    .mobile-center { text-align: center !important; }
                                }
                        
                                @media (prefers-color-scheme: dark) {
                                    .dark-mode { background-color: #1a1a1a !important; color: #ffffff !important; }
                                    .dark-text { color: #e0e0e0 !important; }
                                }
                            </style>
                        </head>
                        
                        <body style="margin: 0; padding: 0; background-color: #f8fffe; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;">
                            <!-- Preheader Text -->
                            <div style="display: none; font-size: 1px; color: #f8fffe; line-height: 1px; max-height: 0px; max-width: 0px; opacity: 0; overflow: hidden;">
                                Welcome to Goods Transport System! Your email verification code: %s - Let's get you started with seamless logistics!
                            </div>
                        
                            <!-- Email Container -->
                            <table role="presentation" cellspacing="0" cellpadding="0" style="width: 100%%; background-color: #f8fffe; padding: 20px 0;">
                                <tr>
                                    <td align="center">
                                        <div class="email-container" style="max-width: 680px; margin: 0 auto; background: #ffffff; border-radius: 16px; box-shadow: 0 8px 32px rgba(0,0,0,0.08); overflow: hidden;">
                        
                                            <!-- Header Section with Integrated Logo -->
                                            <table role="presentation" class="responsive-table" style="background: linear-gradient(135deg, #10b981 0%%, #059669 100%%);">
                                                <tr>
                                                    <td class="mobile-padding" style="padding: 50px 40px; text-align: center;">
                        
                                                        <!-- Welcome Badge -->
                                                        <div style="background: rgba(255,255,255,0.1); backdrop-filter: blur(20px); display: inline-block; padding: 8px 24px; border-radius: 30px; margin-bottom: 20px; border: 2px solid rgba(255,255,255,0.2);">
                                                            <span style="color: #ffffff; font-size: 14px; font-weight: 600;">✨ WELCOME TO THE FAMILY</span>
                                                        </div>
                        
                                                        <!-- Professional Logo Section -->
                                                        <div style="background: rgba(255,255,255,0.1); backdrop-filter: blur(20px); display: inline-block; padding: 20px; border-radius: 20px; margin-bottom: 24px; border: 2px solid rgba(255,255,255,0.2); box-shadow: 0 8px 32px rgba(0,0,0,0.15);">
                                                            <!-- SVG Logo Implementation -->
                                                            <svg width="80" height="80" viewBox="0 0 120 120" xmlns="http://www.w3.org/2000/svg" style="display: block; margin: 0 auto;">
                                                                <!-- Background Elements -->
                                                                <defs>
                                                                    <linearGradient id="logoGradient" x1="0%%" y1="0%%" x2="100%%" y2="100%%">
                                                                        <stop offset="0%%" style="stop-color:#ffffff"/>
                                                                        <stop offset="100%%" style="stop-color:#f1f5f9"/>
                                                                    </linearGradient>
                                                                    <linearGradient id="truckGradient" x1="0%%" y1="0%%" x2="100%%" y2="100%%">
                                                                        <stop offset="0%%" style="stop-color:#10b981"/>
                                                                        <stop offset="100%%" style="stop-color:#059669"/>
                                                                    </linearGradient>
                                                                </defs>
                        
                                                                <!-- Outer Ring -->
                                                                <circle cx="60" cy="60" r="58" fill="url(#logoGradient)" opacity="0.8"/>
                                                                <circle cx="60" cy="60" r="50" fill="none" stroke="url(#truckGradient)" stroke-width="2" opacity="0.9"/>
                        
                                                                <!-- Modern Truck Icon -->
                                                                <g transform="translate(25, 35)">
                                                                    <!-- Truck Body -->
                                                                    <rect x="15" y="15" width="45" height="25" rx="3" fill="url(#truckGradient)"/>
                                                                    <!-- Truck Cab -->
                                                                    <rect x="5" y="20" width="15" height="20" rx="2" fill="url(#truckGradient)"/>
                                                                    <!-- Wheels -->
                                                                    <circle cx="25" cy="45" r="6" fill="none" stroke="url(#truckGradient)" stroke-width="2"/>
                                                                    <circle cx="50" cy="45" r="6" fill="none" stroke="url(#truckGradient)" stroke-width="2"/>
                                                                    <!-- Motion Lines -->
                                                                    <line x1="0" y1="10" x2="8" y2="10" stroke="url(#truckGradient)" stroke-width="2" opacity="0.7"/>
                                                                    <line x1="2" y1="15" x2="10" y2="15" stroke="url(#truckGradient)" stroke-width="2" opacity="0.5"/>
                                                                </g>
                        
                                                                <!-- Connection Points/Network -->
                                                                <circle cx="30" cy="30" r="3" fill="url(#truckGradient)" opacity="0.8"/>
                                                                <circle cx="90" cy="30" r="3" fill="url(#truckGradient)" opacity="0.8"/>
                                                                <circle cx="90" cy="90" r="3" fill="url(#truckGradient)" opacity="0.8"/>
                                                                <line x1="30" y1="30" x2="90" y2="30" stroke="url(#truckGradient)" stroke-width="1" opacity="0.4"/>
                                                                <line x1="90" y1="30" x2="90" y2="90" stroke="url(#truckGradient)" stroke-width="1" opacity="0.4"/>
                                                            </svg>
                                                        </div>
                        
                                                        <h1 style="color: #ffffff; margin: 0 0 8px 0; font-size: 32px; font-weight: 700; letter-spacing: -0.5px;">
                                                            Welcome to Goods Transport System!
                                                        </h1>
                                                        <p style="color: rgba(255,255,255,0.9); margin: 0 0 8px 0; font-size: 18px; font-weight: 500;">
                                                            Your journey to seamless logistics starts here
                                                        </p>
                                                        <p style="color: rgba(255,255,255,0.8); margin: 0; font-size: 16px; font-weight: 400;">
                                                            🎉 Let's verify your email and get you started!
                                                        </p>
                        
                                                        <!-- Welcome Badge -->
                                                        <div style="background: rgba(255, 255, 255, 0.2); border: 1px solid rgba(255, 255, 255, 0.3); color: #ffffff; display: inline-block; padding: 8px 16px; border-radius: 20px; font-size: 12px; font-weight: 600; margin-top: 16px; backdrop-filter: blur(10px);">
                                                            ✅ STEP 1: EMAIL VERIFICATION
                                                        </div>
                                                    </td>
                                                </tr>
                                            </table>
                        
                                            <!-- Main Content -->
                                            <table role="presentation" class="responsive-table">
                                                <tr>
                                                    <td class="mobile-padding" style="padding: 50px 40px;">
                        
                                                        <!-- Welcome Greeting -->
                                                        <div style="margin-bottom: 32px; text-align: center;">
                                                            <div style="display: inline-block; background: linear-gradient(135deg, #fef3c7, #fde68a); padding: 20px; border-radius: 50px; margin-bottom: 20px;">
                                                                <span style="font-size: 48px;">🎉</span>
                                                            </div>
                                                            <h2 style="color: #1f2937; margin: 0 0 16px 0; font-size: 28px; font-weight: 700;">
                                                                Welcome aboard, %s! 
                                                            </h2>
                                                            <p style="color: #6b7280; font-size: 16px; line-height: 1.6; margin: 0;">
                                                                Thank you for choosing Goods Transport System! You're just one step away from experiencing seamless logistics management. 
                                                                Let's verify your email address to complete your registration.
                                                            </p>
                                                        </div>
                        
                                                        <!-- Welcome Message -->
                                                        <div style="background: linear-gradient(135deg, #ecfdf5, #d1fae5); border-left: 4px solid #10b981; padding: 20px; margin: 32px 0; border-radius: 8px;">
                                                            <div style="display: flex; align-items: flex-start;">
                                                                <span style="font-size: 24px; margin-right: 12px; line-height: 1;">🚀</span>
                                                                <div>
                                                                    <h3 style="color: #065f46; margin: 0 0 8px 0; font-size: 16px; font-weight: 600;">You're Almost There!</h3>
                                                                    <p style="color: #065f46; margin: 0; font-size: 14px; line-height: 1.5;">
                                                                        We've created your account on <strong>%s</strong>. Just verify your email address using the code below, 
                                                                        and you'll have full access to all our features including shipment tracking, route optimization, and real-time updates.
                                                                    </p>
                                                                </div>
                                                            </div>
                                                        </div>
                        
                                                        <!-- OTP Section -->
                                                        <div style="background: linear-gradient(135deg, #ecfdf5, #d1fae5); border: 2px solid #10b981; border-radius: 16px; padding: 40px 30px; text-align: center; margin: 40px 0; position: relative; overflow: hidden;">
                                                            <!-- Decorative Elements -->
                                                            <div style="position: absolute; top: -10px; left: -10px; width: 40px; height: 40px; background: linear-gradient(45deg, #10b981, #059669); border-radius: 50%%; opacity: 0.1;"></div>
                                                            <div style="position: absolute; bottom: -15px; right: -15px; width: 60px; height: 60px; background: linear-gradient(45deg, #059669, #047857); border-radius: 50%%; opacity: 0.1;"></div>
                        
                                                            <div style="position: relative; z-index: 1;">
                                                                <h3 style="color: #065f46; margin: 0 0 12px 0; font-size: 20px; font-weight: 700;">
                                                                    ✉️ Your Email Verification Code
                                                                </h3>
                                                                <p style="color: #065f46; margin: 0 0 24px 0; font-size: 14px; opacity: 0.8;">
                                                                    Enter this code to complete your registration
                                                                </p>
                        
                                                                <!-- OTP Display -->
                                                                <div style="background: linear-gradient(135deg, #10b981, #059669); color: #ffffff; font-size: 42px; font-weight: 900; padding: 20px 30px; border-radius: 12px; display: inline-block; letter-spacing: 8px; font-family: 'Courier New', monospace; box-shadow: 0 8px 24px rgba(16, 185, 129, 0.3); border: 3px solid rgba(255,255,255,0.2);">
                                                                    %s
                                                                </div>
                        
                                                                <!-- Timer -->
                                                                <div style="margin-top: 24px; padding: 12px 20px; background: rgba(239, 68, 68, 0.1); border: 1px solid rgba(239, 68, 68, 0.2); border-radius: 8px; display: inline-block;">
                                                                    <p style="color: #dc2626; margin: 0; font-size: 14px; font-weight: 600;">
                                                                        ⏰ Expires in 10 minutes | Verification ID: %s
                                                                    </p>
                                                                </div>
                                                            </div>
                                                        </div>
                        
                                                        <!-- Instructions -->
                                                        <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 24px; margin: 32px 0;">
                                                            <h3 style="color: #374151; margin: 0 0 16px 0; font-size: 18px; font-weight: 600;">
                                                                📋 How to Complete Your Registration:
                                                            </h3>
                                                            <ol style="color: #6b7280; margin: 0; padding-left: 20px; line-height: 1.6;">
                                                                <li style="margin-bottom: 8px;">Return to the registration page in your browser</li>
                                                                <li style="margin-bottom: 8px;">Enter the 6-digit verification code shown above</li>
                                                                <li style="margin-bottom: 8px;">Click "Verify Email" to complete your registration</li>
                                                                <li>Start exploring our logistics solutions!</li>
                                                            </ol>
                                                        </div>
                        
                                                        <!-- What's Next Section -->
                                                        <div style="background: linear-gradient(135deg, #eff6ff, #dbeafe); border-left: 4px solid #3b82f6; padding: 20px; margin: 32px 0; border-radius: 8px;">
                                                            <h3 style="color: #1e40af; margin: 0 0 12px 0; font-size: 16px; font-weight: 600;">
                                                                🎯 What's Next After Verification?
                                                            </h3>
                                                            <ul style="color: #1e40af; margin: 0; padding-left: 20px; font-size: 14px; line-height: 1.5;">
                                                                <li>Access your personalized dashboard</li>
                                                                <li>Create and track shipments in real-time</li>
                                                                <li>Optimize routes for maximum efficiency</li>
                                                                <li>Connect with our network of trusted drivers</li>
                                                                <li>Get instant updates on delivery status</li>
                                                            </ul>
                                                        </div>
                        
                                                        <!-- Features Preview -->
                                                        <div style="background: #f9fafb; border: 1px solid #e5e7eb; border-radius: 12px; padding: 30px; margin: 32px 0;">
                                                            <h3 style="color: #374151; margin: 0 0 20px 0; font-size: 18px; font-weight: 600; text-align: center;">
                                                                🌟 What Makes Us Different?
                                                            </h3>
                                                            <div style="display: flex; justify-content: space-around; text-align: center; flex-wrap: wrap;">
                                                                <div style="flex: 1; min-width: 150px; margin: 10px;">
                                                                    <div style="background: linear-gradient(135deg, #10b981, #059669); color: white; width: 50px; height: 50px; border-radius: 50%%; display: flex; align-items: center; justify-content: center; margin: 0 auto 10px; font-size: 20px;">🚚</div>
                                                                    <h4 style="color: #374151; margin: 0 0 8px 0; font-size: 14px; font-weight: 600;">Real-time Tracking</h4>
                                                                    <p style="color: #6b7280; margin: 0; font-size: 12px;">Track every shipment in real-time</p>
                                                                </div>
                                                                <div style="flex: 1; min-width: 150px; margin: 10px;">
                                                                    <div style="background: linear-gradient(135deg, #3b82f6, #1d4ed8); color: white; width: 50px; height: 50px; border-radius: 50%%; display: flex; align-items: center; justify-content: center; margin: 0 auto 10px; font-size: 20px;">🗺️</div>
                                                                    <h4 style="color: #374151; margin: 0 0 8px 0; font-size: 14px; font-weight: 600;">Route Optimization</h4>
                                                                    <p style="color: #6b7280; margin: 0; font-size: 12px;">AI-powered route planning</p>
                                                                </div>
                                                                <div style="flex: 1; min-width: 150px; margin: 10px;">
                                                                    <div style="background: linear-gradient(135deg, #f59e0b, #d97706); color: white; width: 50px; height: 50px; border-radius: 50%%; display: flex; align-items: center; justify-content: center; margin: 0 auto 10px; font-size: 20px;">🔔</div>
                                                                    <h4 style="color: #374151; margin: 0 0 8px 0; font-size: 14px; font-weight: 600;">Instant Alerts</h4>
                                                                    <p style="color: #6b7280; margin: 0; font-size: 12px;">Get notified of every update</p>
                                                                </div>
                                                            </div>
                                                        </div>
                        
                                                        <!-- Support Section -->
                                                        <div style="text-align: center; margin: 40px 0 20px 0; padding: 24px; background: linear-gradient(135deg, #fafafa, #f4f4f5); border-radius: 12px;">
                                                            <h3 style="color: #374151; margin: 0 0 12px 0; font-size: 18px; font-weight: 600;">
                                                                Need Help? 🤝
                                                            </h3>
                                                            <p style="color: #6b7280; margin: 0 0 16px 0; font-size: 14px; line-height: 1.5;">
                                                                Our friendly support team is here to help you get started
                                                            </p>
                                                            <div style="display: inline-block;">
                                                                <a href="mailto:%s" style="background: linear-gradient(135deg, #10b981, #059669); color: #ffffff; text-decoration: none; padding: 12px 24px; border-radius: 8px; font-weight: 600; font-size: 14px; display: inline-block; margin-right: 12px;">
                                                                    📧 Get Support
                                                                </a>
                                                                <span style="color: #6b7280; font-size: 14px; font-weight: 500;">
                                                                    📞 %s
                                                                </span>
                                                            </div>
                                                        </div>
                        
                                                    </td>
                                                </tr>
                                            </table>
                        
                                            <!-- Footer -->
                                            <table role="presentation" class="responsive-table" style="background: #1f2937;">
                                                <tr>
                                                    <td class="mobile-padding" style="padding: 40px; text-align: center;">
                                                        <div style="margin-bottom: 24px;">
                                                            <h4 style="color: #ffffff; margin: 0 0 12px 0; font-size: 18px; font-weight: 600;">
                                                                Goods Transport System
                                                            </h4>
                                                            <p style="color: #9ca3af; margin: 0; font-size: 14px; line-height: 1.5;">
                                                                Logistics reimagined, for a seamless experience
                                                            </p>
                                                        </div>
                        
                                                        <!-- Welcome Message -->
                                                        <div style="background: rgba(16, 185, 129, 0.1); border: 1px solid rgba(16, 185, 129, 0.2); border-radius: 8px; padding: 16px; margin: 24px 0;">
                                                            <p style="color: #10b981; font-size: 14px; margin: 0; font-weight: 500;">
                                                                🎉 Welcome to the future of logistics management!
                                                            </p>
                                                        </div>
                        
                                                        <!-- Social Links -->
                                                        <div style="margin: 24px 0;">
                                                            <span style="color: #6b7280; font-size: 12px;">Follow us: </span>
                                                            <span style="color: #10b981; font-size: 18px; margin: 0 8px;">📱</span>
                                                            <span style="color: #10b981; font-size: 18px; margin: 0 8px;">🌐</span>
                                                            <span style="color: #10b981; font-size: 18px; margin: 0 8px;">📧</span>
                                                        </div>
                        
                                                        <div style="border-top: 1px solid #374151; padding-top: 24px; margin-top: 24px;">
                                                            <p style="color: #6b7280; font-size: 12px; margin: 0 0 8px 0; line-height: 1.4;">
                                                                This is an automated welcome message from Goods Transport System.
                                                            </p>
                                                            <p style="color: #6b7280; font-size: 12px; margin: 0; line-height: 1.4;">
                                                                © 2025 Goods Transport System. All rights reserved. | Privacy Policy | Terms of Service
                                                            </p>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </table>
                        
                                        </div>
                                    </td>
                                </tr>
                            </table>
                        
                            <!-- Analytics Tracking Pixel -->
                            <img src="data:image/gif;base64,R0lNT0lhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7" alt="" style="display: none;" />
                        
                        </body>
                        </html>
                        """,
                otp, username, currentTime, otp, verificationId, supportEmail, supportPhone);
    }

    private String createEnhancedEmailContent(String email, String otp) {
        String username = extractUsername(email);
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a"));
        String requestId = "REQ-" + System.currentTimeMillis();

        return String.format("""
                        <!DOCTYPE html>
                        <html lang="en" xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:o="urn:schemas-microsoft-com:office:office">
                        <head>
                            <meta charset="utf-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <meta http-equiv="X-UA-Compatible" content="IE=edge">
                            <meta name="x-apple-disable-message-reformatting">
                            <meta name="format-detection" content="telephone=no,address=no,email=no,date=no,url=no">
                            <title>Password Reset Request - Goods Transport System</title>
                        
                            <!--[if mso]>
                            <noscript>
                                <xml>
                                    <o:OfficeDocumentSettings>
                                        <o:AllowPNG/>
                                        <o:PixelsPerInch>96</o:PixelsPerInch>
                                    </o:OfficeDocumentSettings>
                                </xml>
                            </noscript>
                            <![endif]-->
                        
                            <style>
                                * { box-sizing: border-box; }
                                body { margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segou UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif; }
                                .email-container { max-width: 680px; margin: 0 auto; background: #ffffff; }
                                .content-block { padding: 0; }
                                .responsive-table { width: 100%%; border-collapse: collapse; }
                        
                                @media screen and (max-width: 600px) {
                                    .mobile-padding { padding: 20px 15px !important; }
                                    .mobile-text { font-size: 16px !important; line-height: 1.5 !important; }
                                    .mobile-otp { font-size: 28px !important; padding: 12px 15px !important; }
                                    .mobile-hide { display: none !important; }
                                    .mobile-center { text-align: center !important; }
                                }
                        
                                @media (prefers-color-scheme: dark) {
                                    .dark-mode { background-color: #1a1a1a !important; color: #ffffff !important; }
                                    .dark-text { color: #e0e0e0 !important; }
                                }
                            </style>
                        </head>
                        
                        <body style="margin: 0; padding: 0; background-color: #f4f7fa; font-family: -apple-system, BlinkMacSystemFont, 'Segou UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;">
                            <!-- Preheader Text -->
                            <div style="display: none; font-size: 1px; color: #f4f7fa; line-height: 1px; max-height: 0px; max-width: 0px; opacity: 0; overflow: hidden;">
                                Your secure password reset code: %s - Valid for 10 minutes only. Goods Transport System Security Team.
                            </div>
                        
                            <!-- Email Container -->
                            <table role="presentation" cellspacing="0" cellpadding="0" style="width: 100%%; background-color: #f4f7fa; padding: 20px 0;">
                                <tr>
                                    <td align="center">
                                        <div class="email-container" style="max-width: 680px; margin: 0 auto; background: #ffffff; border-radius: 16px; box-shadow: 0 8px 32px rgba(0,0,0,0.08); overflow: hidden;">
                        
                                            <!-- Header Section with Integrated Logo -->
                                            <table role="presentation" class="responsive-table" style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);">
                                                <tr>
                                                    <td class="mobile-padding" style="padding: 50px 40px; text-align: center;">
                        
                                                        <!-- Professional Logo Section -->
                                                        <div style="background: rgba(255,255,255,0.1); backdrop-filter: blur(20px); display: inline-block; padding: 20px; border-radius: 20px; margin-bottom: 24px; border: 2px solid rgba(255,255,255,0.2); box-shadow: 0 8px 32px rgba(0,0,0,0.15);">
                                                            <!-- SVG Logo Implementation -->
                                                            <svg width="80" height="80" viewBox="0 0 120 120" xmlns="http://www.w3.org/2000/svg" style="display: block; margin: 0 auto;">
                                                                <!-- Background Elements -->
                                                                <defs>
                                                                    <linearGradient id="logoGradient" x1="0%%" y1="0%%" x2="100%%" y2="100%%">
                                                                        <stop offset="0%%" style="stop-color:#ffffff"/>
                                                                        <stop offset="100%%" style="stop-color:#f1f5f9"/>
                                                                    </linearGradient>
                                                                    <linearGradient id="truckGradient" x1="0%%" y1="0%%" x2="100%%" y2="100%%">
                                                                        <stop offset="0%%" style="stop-color:#667eea"/>
                                                                        <stop offset="100%%" style="stop-color:#764ba2"/>
                                                                    </linearGradient>
                                                                </defs>
                        
                                                                <!-- Outer Ring -->
                                                                <circle cx="60" cy="60" r="58" fill="url(#logoGradient)" opacity="0.8"/>
                                                                <circle cx="60" cy="60" r="50" fill="none" stroke="url(#truckGradient)" stroke-width="2" opacity="0.9"/>
                        
                                                                <!-- Modern Truck Icon -->
                                                                <g transform="translate(25, 35)">
                                                                    <!-- Truck Body -->
                                                                    <rect x="15" y="15" width="45" height="25" rx="3" fill="url(#truckGradient)"/>
                                                                    <!-- Truck Cab -->
                                                                    <rect x="5" y="20" width="15" height="20" rx="2" fill="url(#truckGradient)"/>
                                                                    <!-- Wheels -->
                                                                    <circle cx="25" cy="45" r="6" fill="none" stroke="url(#truckGradient)" stroke-width="2"/>
                                                                    <circle cx="50" cy="45" r="6" fill="none" stroke="url(#truckGradient)" stroke-width="2"/>
                                                                    <!-- Motion Lines -->
                                                                    <line x1="0" y1="10" x2="8" y2="10" stroke="url(#truckGradient)" stroke-width="2" opacity="0.7"/>
                                                                    <line x1="2" y1="15" x2="10" y2="15" stroke="url(#truckGradient)" stroke-width="2" opacity="0.5"/>
                                                                </g>
                        
                                                                <!-- Connection Points/Network -->
                                                                <circle cx="30" cy="30" r="3" fill="url(#truckGradient)" opacity="0.8"/>
                                                                <circle cx="90" cy="30" r="3" fill="url(#truckGradient)" opacity="0.8"/>
                                                                <circle cx="90" cy="90" r="3" fill="url(#truckGradient)" opacity="0.8"/>
                                                                <line x1="30" y1="30" x2="90" y2="30" stroke="url(#truckGradient)" stroke-width="1" opacity="0.4"/>
                                                                <line x1="90" y1="30" x2="90" y2="90" stroke="url(#truckGradient)" stroke-width="1" opacity="0.4"/>
                                                            </svg>
                                                        </div>
                        
                                                        <h1 style="color: #ffffff; margin: 0 0 8px 0; font-size: 32px; font-weight: 700; letter-spacing: -0.5px;">
                                                            Goods Transport System
                                                        </h1>
                                                        <p style="color: rgba(255,255,255,0.9); margin: 0 0 8px 0; font-size: 18px; font-weight: 500;">
                                                            Logistics reimagined, for a seamless experience
                                                        </p>
                                                        <p style="color: rgba(255,255,255,0.8); margin: 0; font-size: 16px; font-weight: 400;">
                                                            🔐 Secure Password Reset Request
                                                        </p>
                        
                                                        <!-- Security Badge -->
                                                        <div style="background: rgba(34, 197, 94, 0.2); border: 1px solid rgba(34, 197, 94, 0.3); color: #ffffff; display: inline-block; padding: 8px 16px; border-radius: 20px; font-size: 12px; font-weight: 600; margin-top: 16px; backdrop-filter: blur(10px);">
                                                            ✅ VERIFIED SECURITY REQUEST
                                                        </div>
                                                    </td>
                                                </tr>
                                            </table>
                        
                                            <!-- Main Content -->
                                            <table role="presentation" class="responsive-table">
                                                <tr>
                                                    <td class="mobile-padding" style="padding: 50px 40px;">
                        
                                                        <!-- Greeting -->
                                                        <div style="margin-bottom: 32px;">
                                                            <h2 style="color: #1f2937; margin: 0 0 16px 0; font-size: 28px; font-weight: 700;">
                                                                Hello %s! 👋
                                                            </h2>
                                                            <p style="color: #6b7280; font-size: 16px; line-height: 1.6; margin: 0;">
                                                                We received a request to reset your password for your Goods Transport System account on <strong>%s</strong>.
                                                            </p>
                                                        </div>
                        
                                                        <!-- Security Alert -->
                                                        <div style="background: linear-gradient(135deg, #fef3c7, #fde68a); border-left: 4px solid #f59e0b; padding: 20px; margin: 32px 0; border-radius: 8px;">
                                                            <div style="display: flex; align-items: flex-start;">
                                                                <span style="font-size: 24px; margin-right: 12px; line-height: 1;">⚡</span>
                                                                <div>
                                                                    <h3 style="color: #92400e; margin: 0 0 8px 0; font-size: 16px; font-weight: 600;">Security Notice</h3>
                                                                    <p style="color: #92400e; margin: 0; font-size: 14px; line-height: 1.5;">
                                                                        If you didn't request this password reset, please ignore this email and ensure your account is secure. 
                                                                        Your password will remain unchanged.
                                                                    </p>
                                                                </div>
                                                            </div>
                                                        </div>
                        
                                                        <!-- OTP Section -->
                                                        <div style="background: linear-gradient(135deg, #eff6ff, #dbeafe); border: 2px solid #3b82f6; border-radius: 16px; padding: 40px 30px; text-align: center; margin: 40px 0; position: relative; overflow: hidden;">
                                                            <!-- Decorative Elements -->
                                                            <div style="position: absolute; top: -10px; left: -10px; width: 40px; height: 40px; background: linear-gradient(45deg, #3b82f6, #06b6d4); border-radius: 50%%; opacity: 0.1;"></div>
                                                            <div style="position: absolute; bottom: -15px; right: -15px; width: 60px; height: 60px; background: linear-gradient(45deg, #06b6d4, #0891b2); border-radius: 50%%; opacity: 0.1;"></div>
                        
                                                            <div style="position: relative; z-index: 1;">
                                                                <h3 style="color: #1e40af; margin: 0 0 12px 0; font-size: 20px; font-weight: 700;">
                                                                    🔑 Your Secure Verification Code
                                                                </h3>
                                                                <p style="color: #1e40af; margin: 0 0 24px 0; font-size: 14px; opacity: 0.8;">
                                                                    Use this code to reset your password
                                                                </p>
                        
                                                                <!-- OTP Display -->
                                                                <div style="background: linear-gradient(135deg, #667eea, #764ba2); color: #ffffff; font-size: 42px; font-weight: 900; padding: 20px 30px; border-radius: 12px; display: inline-block; letter-spacing: 8px; font-family: 'Courier New', monospace; box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3); border: 3px solid rgba(255,255,255,0.2);">
                                                                    %s
                                                                </div>
                        
                                                                <!-- Timer -->
                                                                <div style="margin-top: 24px; padding: 12px 20px; background: rgba(239, 68, 68, 0.1); border: 1px solid rgba(239, 68, 68, 0.2); border-radius: 8px; display: inline-block;">
                                                                    <p style="color: #dc2626; margin: 0; font-size: 14px; font-weight: 600;">
                                                                        ⏰ Expires in 10 minutes | Request ID: %s
                                                                    </p>
                                                                </div>
                                                            </div>
                                                        </div>
                        
                                                        <!-- Instructions -->
                                                        <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 24px; margin: 32px 0;">
                                                            <h3 style="color: #374151; margin: 0 0 16px 0; font-size: 18px; font-weight: 600;">
                                                                📋 Next Steps:
                                                            </h3>
                                                            <ol style="color: #6b7280; margin: 0; padding-left: 20px; line-height: 1.6;">
                                                                <li style="margin-bottom: 8px;">Return to the password reset page in your browser</li>
                                                                <li style="margin-bottom: 8px;">Enter the 6-digit verification code shown above</li>
                                                                <li style="margin-bottom: 8px;">Create a strong new password</li>
                                                                <li>Confirm your new password and submit</li>
                                                            </ol>
                                                        </div>
                        
                                                        <!-- Security Tips -->
                                                        <div style="background: linear-gradient(135deg, #ecfdf5, #d1fae5); border-left: 4px solid #10b981; padding: 20px; margin: 32px 0; border-radius: 8px;">
                                                            <h3 style="color: #065f46; margin: 0 0 12px 0; font-size: 16px; font-weight: 600;">
                                                                🛡️ Security Best Practices:
                                                            </h3>
                                                            <ul style="color: #065f46; margin: 0; padding-left: 20px; font-size: 14px; line-height: 1.5;">
                                                                <li>Never share this verification code with anyone</li>
                                                                <li>Use a unique, strong password with 8+ characters</li>
                                                                <li>Include uppercase, lowercase, numbers, and symbols</li>
                                                                <li>Enable two-factor authentication when available</li>
                                                            </ul>
                                                        </div>
                        
                                                        <!-- Support Section -->
                                                        <div style="text-align: center; margin: 40px 0 20px 0; padding: 24px; background: linear-gradient(135deg, #fafafa, #f4f4f5); border-radius: 12px;">
                                                            <h3 style="color: #374151; margin: 0 0 12px 0; font-size: 18px; font-weight: 600;">
                                                                Need Help? 🤝
                                                            </h3>
                                                            <p style="color: #6b7280; margin: 0 0 16px 0; font-size: 14px; line-height: 1.5;">
                                                                Our support team is here to help you 24/7
                                                            </p>
                                                            <div style="display: inline-block;">
                                                                <a href="mailto:%s" style="background: linear-gradient(135deg, #667eea, #764ba2); color: #ffffff; text-decoration: none; padding: 12px 24px; border-radius: 8px; font-weight: 600; font-size: 14px; display: inline-block; margin-right: 12px;">
                                                                    📧 Email Support
                                                                </a>
                                                                <span style="color: #6b7280; font-size: 14px; font-weight: 500;">
                                                                    📞 %s
                                                                </span>
                                                            </div>
                                                        </div>
                        
                                                    </td>
                                                </tr>
                                            </table>
                        
                                            <!-- Footer -->
                                            <table role="presentation" class="responsive-table" style="background: #1f2937;">
                                                <tr>
                                                    <td class="mobile-padding" style="padding: 40px; text-align: center;">
                                                        <div style="margin-bottom: 24px;">
                                                            <h4 style="color: #ffffff; margin: 0 0 12px 0; font-size: 18px; font-weight: 600;">
                                                                Goods Transport System
                                                            </h4>
                                                            <p style="color: #9ca3af; margin: 0; font-size: 14px; line-height: 1.5;">
                                                                Logistics reimagined, for a seamless experience
                                                            </p>
                                                        </div>
                        
                                                        <!-- Social Links -->
                                                        <div style="margin: 24px 0;">
                                                            <span style="color: #6b7280; font-size: 12px;">Follow us: </span>
                                                            <span style="color: #667eea; font-size: 18px; margin: 0 8px;">📱</span>
                                                            <span style="color: #667eea; font-size: 18px; margin: 0 8px;">🌐</span>
                                                            <span style="color: #667eea; font-size: 18px; margin: 0 8px;">📧</span>
                                                        </div>
                        
                                                        <div style="border-top: 1px solid #374151; padding-top: 24px; margin-top: 24px;">
                                                            <p style="color: #6b7280; font-size: 12px; margin: 0 0 8px 0; line-height: 1.4;">
                                                                This is an automated security message from Goods Transport System.
                                                            </p>
                                                            <p style="color: #6b7280; font-size: 12px; margin: 0; line-height: 1.4;">
                                                                © 2025 Goods Transport System. All rights reserved. | Privacy Policy | Terms of Service
                                                            </p>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </table>
                        
                                        </div>
                                    </td>
                                </tr>
                            </table>
                        
                            <!-- Analytics Tracking Pixel -->
                            <img src="data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7" alt="" style="display: none;" />
                        
                        </body>
                        </html>
                        """, otp, username, currentTime, otp, requestId, supportEmail, supportPhone);
    }

    // Add the missing extractUsername method if it doesn't exist
    private String extractUsername(String email) {
        if (email == null || !email.contains("@")) {
            return "User";
        }
        String username = email.substring(0, email.indexOf("@"));
        // Capitalize first letter
        return username.substring(0, 1).toUpperCase() + username.substring(1).toLowerCase();
    }

    // Complete EmailService interface - Add missing method
    public interface EmailService {
        void sendOtpEmail(String to, String otp);

        void sendEmail(String to, String subject, String body);

        void sendEmailVerificationOtp(String to, String otp);
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom(fromEmail);
        javaMailSender.send(message);
        log.info("Email sent to: {}", to);
    }
}