package main;

public class Phone {
    private String model;
    private int storage;
    private String damageDescription;
    private double estimatedPrice;

    public Phone(String model, int storage, String damageDescription, double estimatedPrice) {
        this.model = model;
        this.storage = storage;
        this.damageDescription = damageDescription;
        this.estimatedPrice = estimatedPrice;
    }

    public String getModel(){ 
        return model; 
    }

    public int getStorage(){ 
        return storage;
    }

    public String getDamageDescription(){ 
        return damageDescription; 
    }
    public double getEstimatedPrice(){
        return estimatedPrice;
    }

    public void setEstimatedPrice(double price) {
        this.estimatedPrice = price;
    }

    @Override
    public String toString() {
        return String.format("%-25s %-13s %-35s $%.2f",
            model, storage + "GB", damageDescription, estimatedPrice);
    }

}
