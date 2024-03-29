package jr.project.reactsafe.extras.model;

public class UserModel {

    String uid,name,email,title,phone,lng,lat,profileImage,pairedBy,pairedOn;
    boolean isParent;
    boolean isActive;

    public UserModel(){/* empty */}

    public UserModel(String uid,String name,String email,String profileImage,String lat,String lng){
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.profileImage = profileImage;
        this.lat = lat;
        this.lng = lng;
    }

    public UserModel(String uid,String name,String email,String phone,String profileImage,String lat,String lng,String title){
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.profileImage = profileImage;
        this.phone = phone;
        this.lat = lat;
        this.lng = lng;
        this.title = title;
    }

    public UserModel(String uid,String name,String email,String phone,String profileImage,String lat,String lng,String title,boolean isActive){
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.profileImage = profileImage;
        this.phone = phone;
        this.lat = lat;
        this.lng = lng;
        this.title = title;
        this.isActive = isActive;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTitle() {
        return title;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getPairedBy() {
        return pairedBy;
    }

    public void setPairedBy(String pairedBy) {
        this.pairedBy = pairedBy;
    }

    public String getPairedOn() {
        return pairedOn;
    }

    public void setPairedOn(String pairedOn) {
        this.pairedOn = pairedOn;
    }

    public boolean isParent() {
        return isParent;
    }

    public void setParent(boolean parent) {
        isParent = parent;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
