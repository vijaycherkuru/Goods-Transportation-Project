package com.gdc.user_registration_and_authentication.entity;

import com.gdc.user_registration_and_authentication.enums.OtpType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "users_otp")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Otp {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_otps_user"))
    private User user;

    @Column(name = "otp", nullable = false)
    private String otp;

    @Enumerated(EnumType.STRING)
    @Column(name = "otp_type", nullable = false)
    private OtpType otpType;

    @Column(name = "expires_at", nullable = false)
    private Timestamp expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;
}
