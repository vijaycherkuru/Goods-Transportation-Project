package com.gdc.ride_management.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;

public class FareUtils {

    private static final double BASE_FARE = 25.0;
    private static final double PER_KM_RATE = 3.0;
    private static final double PER_KG_RATE = 2.0;
    private static final double PER_ITEM_RATE = 10.0;

    private static final String VALID_COUPON_CODE = "carryconnect2025";
    private static final double DISCOUNT_PERCENTAGE = 10.0; // 10%

    private static final LocalTime COUPON_START_TIME = LocalTime.of(8, 0);  // 8:00 AM
    private static final LocalTime COUPON_END_TIME = LocalTime.of(23, 0);   // 11:00 PM

    public static double calculateFare(double distanceInKm, double weightPerItemInKg, int goodsQuantity) {
        double singleItemFare = BASE_FARE +
                (distanceInKm * PER_KM_RATE) +
                (weightPerItemInKg * PER_KG_RATE) +
                PER_ITEM_RATE;

        double totalFare = singleItemFare * goodsQuantity;
        return roundToTwoDecimalPlaces(totalFare);
    }

    public static double calculateFareWithCoupon(double distanceInKm, double weightPerItemInKg, int goodsQuantity, String couponCode) {
        double originalFare = calculateFare(distanceInKm, weightPerItemInKg, goodsQuantity);

        if (isCouponActiveNow() && VALID_COUPON_CODE.equalsIgnoreCase(couponCode)) {
            double discountAmount = (DISCOUNT_PERCENTAGE / 100.0) * originalFare;
            double discountedFare = originalFare - discountAmount;
            return roundToTwoDecimalPlaces(discountedFare);
        }

        return originalFare;
    }

    private static boolean isCouponActiveNow() {
        LocalTime now = LocalTime.now();
        return !now.isBefore(COUPON_START_TIME) && !now.isAfter(COUPON_END_TIME);
    }

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;
        double latDist = Math.toRadians(lat2 - lat1);
        double lonDist = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDist / 2) * Math.sin(latDist / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDist / 2) * Math.sin(lonDist / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    private static double roundToTwoDecimalPlaces(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
