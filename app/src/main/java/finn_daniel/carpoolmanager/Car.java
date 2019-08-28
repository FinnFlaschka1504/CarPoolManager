package finn_daniel.carpoolmanager;

import java.util.Objects;
import java.util.UUID;

public class Car {
    private String car_id;
    private String name;
    private Double consumption;
    private Double wear;
    private fuelType fuelType;
    public enum fuelType
    {
        DIESEL, E5, E10
    }


    public Car() {
        car_id = "car_" + UUID.randomUUID().toString();
    }

    public String getCar_id() {
        return car_id;
    }

    public void setCar_id(String car_id) {
        this.car_id = car_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getConsumption() {
        return consumption;
    }

    public void setConsumption(Double consumption) {
        this.consumption = consumption;
    }

    public Double getWear() {
        return wear;
    }

    public void setWear(Double wear) {
        this.wear = wear;
    }

    public Car.fuelType getFuelType() {
        return fuelType;
    }

    public void setFuelType(Car.fuelType fuelType) {
        this.fuelType = fuelType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Car car = (Car) o;
        return car_id.equals(car.car_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(car_id);
    }
}
