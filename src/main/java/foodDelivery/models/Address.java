package foodDelivery.models;

import lombok.Getter;

@Getter
public class Address {
    private String street;
    private String city;
    private String pinCode;
    private double latitude;
    private double longitude;

    public Address(String street, String city, String pinCode, double latitude, double longitude) {
        this.street = street;
        this.city = city;
        this.pinCode = pinCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double distanceTo(Address other) {
        double dLat = this.latitude - other.latitude;
        double dLon = this.longitude - other.longitude;
        return Math.sqrt(dLat * dLat + dLon * dLon);
    }
}
