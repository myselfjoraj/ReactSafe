package jr.project.reactsafe.extras.model;

public class AlertModel {

    String ambulance,hospital,police,lat,lng,status,timestamp;

    public AlertModel(){

    }

    public AlertModel(String ambulance,String hospital,String police,String lat,String lng,String status,String timestamp){
        this.ambulance = ambulance;
        this.hospital = hospital;
        this.police = police;
        this.lat = lat;
        this.lng = lng;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getAmbulance() {
        return ambulance;
    }

    public void setAmbulance(String ambulance) {
        this.ambulance = ambulance;
    }

    public String getHospital() {
        return hospital;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }

    public String getPolice() {
        return police;
    }

    public void setPolice(String police) {
        this.police = police;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
