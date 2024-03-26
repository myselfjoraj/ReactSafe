package jr.project.reactsafe.extras.model;

public class LocationModel {

    String lat,lng,uid,timestamp;

    public LocationModel(String lat,String lng,String uid,String timestamp){
        this.lat = lat;
        this.lng = lng;
        this.uid = uid;
        this.timestamp = timestamp;
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
