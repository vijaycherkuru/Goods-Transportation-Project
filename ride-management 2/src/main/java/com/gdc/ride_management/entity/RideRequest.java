package com.gdc.ride_management.entity;

// com.gdc.ride_management.entity.RideRequest
import com.gdc.ride_management.enums.GoodsType;
import com.gdc.ride_management.enums.RequiredSpaceType;
import com.gdc.ride_management.enums.RequestStatus; // Assuming you'll have a status for requests
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "ride_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideRequest {
 @Id
 @GeneratedValue(strategy = GenerationType.AUTO)
 @Column(name = "ride_request_id", nullable = false, updatable = false)
 private UUID id;

 @Column(name = "sender_user_id", nullable = false) // Renamed from userId for clarity
 private UUID senderUserId; // Reference to the user who sent the goods request

 @ManyToOne // Link to the Ride entity
 @JoinColumn(name = "ride_id", nullable = false) // Foreign key to Ride table
 private Ride ride;

 @Column(name = "from_location", nullable = false)
 private String from;

 @Column(name = "to_location", nullable = false)
 private String to;

 @Column(name = "fare")
 private Double fare;

 @Column(name = "ride_date", nullable = false)
 private LocalDate date; // This should ideally come from the linked Ride entity

 @Enumerated(EnumType.STRING)
 @Column(name = "goods_type", nullable = false)
 private GoodsType goodsType;

 @Column(name = "goods_weight_kg", nullable = false)
 private Double goodsWeightInKg;

 @Enumerated(EnumType.STRING)
 @Column(name = "required_space", nullable = false)
 private RequiredSpaceType requiredSpace;

 @Column(name = "phone_number", nullable = false) // Sender's phone number
 private String phoneNumber;

 @Column(name = "from_latitude")
 private Double fromLatitude;

 @Column(name = "from_longitude")
 private Double fromLongitude;

 @Column(name = "to_latitude")
 private Double toLatitude;

 @Column(name = "to_longitude")
 private Double toLongitude;

 @Column(name = "goods_quantity")
 private Integer goodsQuantity;


 @Enumerated(EnumType.STRING) // Add request status
 @Column(name = "request_status", nullable = false)
 private RequestStatus requestStatus; // E.g., PENDING, ACCEPTED, DECLINED, COMPLETED

 @Column(name = "created_at", nullable = false, updatable = false)
 private Timestamp createdAt;

 @Column(name = "updated_at")
 private Timestamp updatedAt;

 private Timestamp fareCreatedAt;

 @PrePersist
 protected void onCreate() {
  createdAt = new Timestamp(System.currentTimeMillis());
  if (requestStatus == null) {
   requestStatus = RequestStatus.PENDING; // Default status
  }
 }

 @PreUpdate
 protected void onUpdate() {
  updatedAt = new Timestamp(System.currentTimeMillis());
 }
}
