package com.gdc.ride_management.entity;

import com.gdc.ride_management.enums.RequiredSpaceType;
import com.gdc.ride_management.enums.RideStatus;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rides")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ride {

 @Id
 @GeneratedValue(strategy = GenerationType.AUTO)
 @Column(name = "ride_id", updatable = false, nullable = false)
 private UUID id;

 @OneToMany(mappedBy = "ride", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
 private List<RideRequest> rideRequests;

 @Column(name = "from_location", nullable = false)
 private String from;

 @Column(name = "to_location", nullable = false)
 private String to;

 @Column(name = "ride_date", nullable = false)
 private LocalDate date;

 @Column(name = "ride_time", nullable = false)
 private LocalTime time;

 @Enumerated(EnumType.STRING)
 @Column(name = "ride_status")
 private RideStatus rideStatus;

 @Column(name = "vehicle_type", nullable = false)
 private String vehicleType;

 @Column(name = "from_latitude")
 private Double fromLatitude;

 @Column(name = "from_longitude")
 private Double fromLongitude;

 @Column(name = "to_latitude")
 private Double toLatitude;

 @Column(name = "to_longitude")
 private Double toLongitude;

 @Enumerated(EnumType.STRING)
 @Column(name = "luggage_space", nullable = false)
 private RequiredSpaceType luggageSpace;

 @Column(name = "driving_license_number", nullable = false)
 private String drivingLicenseNumber;


 @Column(name = "ride_user_id", nullable = false)
 private UUID rideUserId;



 @Column(name = "created_at", nullable = false, updatable = false)
 private Timestamp createdAt;

 @Column(name = "updated_at")
 private Timestamp updatedAt;
}
