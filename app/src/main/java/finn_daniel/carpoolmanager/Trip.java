package finn_daniel.carpoolmanager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Trip {
    private String trip_id;
    private Date date;
    private List<List<Double>> fromTo = new ArrayList<>();
    private List<String> searchString = new ArrayList<>();
    private List<String> locationName = new ArrayList<>();
    private String distance;
    private boolean twoWay;
    private String carId;
    private double fuelCost;
    private double cost;
    private String driverId;
    private String polylineString;
    private boolean isSaved = true;
    private boolean isBookmark;

    public enum type {
        TRIP, BOOKMARK
    }

    public Trip() {
            trip_id = "trip_" + UUID.randomUUID().toString();
    }

    public String getTrip_id() {
        return trip_id;
    }

    public void setTrip_id(String trip_id) {
        this.trip_id = trip_id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<List<Double>> getFromTo() {
        return fromTo;
    }

    public void setFromTo(List<List<Double>> fromTo) {
        this.fromTo = fromTo;
    }

    public List<String> getSearchString() {
        return searchString;
    }

    public void setSearchString(List<String> searchString) {
        this.searchString = searchString;
    }

    public List<String> getLocationName() {
        return locationName;
    }

    public void setLocationName(List<String> locationName) {
        this.locationName = locationName;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public boolean isTwoWay() {
        return twoWay;
    }

    public void setTwoWay(boolean twoWay) {
        this.twoWay = twoWay;
    }

    public String getCarId() {
        return carId;
    }

    public void setCarId(String carId) {
        this.carId = carId;
    }

    public double getFuelCost() {
        return fuelCost;
    }

    public void setFuelCost(double fuelCost) {
        this.fuelCost = fuelCost;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getPolylineString() {
        return polylineString;
    }

    public void setPolylineString(String polylineString) {
        this.polylineString = polylineString;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }

    public boolean isBookmark() {
        return isBookmark;
    }

    public void setBookmark(boolean bookmark) {
        isBookmark = bookmark;
    }
}
