package jr.project.reactsafe.extras.model;

import java.security.PublicKey;

public class AcceptModel {

    String TIMESTAMP;
    String LATITUDE;
    String LONGITUDE;
    String STATUS;
    UserModel PATIENT;
    UserModel PARENT;
    UserModel HOSPITAL;
    UserModel AMBULANCE;
    UserModel POLICE;

    public AcceptModel(){

    }

    public AcceptModel(String TIMESTAMP, String LATITUDE , String LONGITUDE , String STATUS,
                       UserModel PATIENT,UserModel PARENT,UserModel HOSPITAL,UserModel AMBULANCE,UserModel POLICE){
        this.TIMESTAMP = TIMESTAMP;
        this.LATITUDE = LATITUDE;
        this.LONGITUDE = LONGITUDE;
        this.STATUS = STATUS;
        this.PARENT = PARENT;
        this.PATIENT = PATIENT;
        this.HOSPITAL = HOSPITAL;
        this.AMBULANCE = AMBULANCE;
        this.POLICE = POLICE;
    }

    public String getTIMESTAMP() {
        return TIMESTAMP;
    }

    public void setTIMESTAMP(String TIMESTAMP) {
        this.TIMESTAMP = TIMESTAMP;
    }

    public String getLATITUDE() {
        return LATITUDE;
    }

    public void setLATITUDE(String LATITUDE) {
        this.LATITUDE = LATITUDE;
    }

    public String getLONGITUDE() {
        return LONGITUDE;
    }

    public void setLONGITUDE(String LONGITUDE) {
        this.LONGITUDE = LONGITUDE;
    }

    public String getSTATUS() {
        return STATUS;
    }

    public void setSTATUS(String STATUS) {
        this.STATUS = STATUS;
    }

    public UserModel getPATIENT() {
        return PATIENT;
    }

    public void setPATIENT(UserModel PATIENT) {
        this.PATIENT = PATIENT;
    }

    public UserModel getPARENT() {
        return PARENT;
    }

    public void setPARENT(UserModel PARENT) {
        this.PARENT = PARENT;
    }

    public UserModel getHOSPITAL() {
        return HOSPITAL;
    }

    public void setHOSPITAL(UserModel HOSPITAL) {
        this.HOSPITAL = HOSPITAL;
    }

    public UserModel getAMBULANCE() {
        return AMBULANCE;
    }

    public void setAMBULANCE(UserModel AMBULANCE) {
        this.AMBULANCE = AMBULANCE;
    }

    public UserModel getPOLICE() {
        return POLICE;
    }

    public void setPOLICE(UserModel POLICE) {
        this.POLICE = POLICE;
    }
}
