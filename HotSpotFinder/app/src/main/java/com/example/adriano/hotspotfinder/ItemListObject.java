package com.example.adriano.hotspotfinder;

/**
 * Created by adriano on 06/07/17.
 */

public class ItemListObject {
    String ssid;
    String type;
    double signal;

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getSignal(){
        return signal;
    }

    public void setSignal(double s){
        signal = s;
    }

}
