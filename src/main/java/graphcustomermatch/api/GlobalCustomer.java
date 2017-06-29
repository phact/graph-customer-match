package graphcustomermatch.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by sebastianestevez on 11/22/16.
 */
public class GlobalCustomer {

    @JsonProperty
    private int dob;

    @JsonProperty
    private String system_name;

    @JsonProperty
    private String firstname;

    @JsonProperty
    private String lastname;

    @JsonProperty
    private String ssn;

    @JsonProperty
    private String phone;

    @JsonProperty
    private String gender;

    @JsonProperty
    private String address;

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getDob() {
        return dob;
    }

    public void setDob(int dob) {
        this.dob = dob;
    }

    public String getSystem_name() {
        return system_name;
    }

    public void setSystem_name(String system_name) {
        this.system_name = system_name;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }



    public GlobalCustomer(int dob, String system_name, String firstname, String lastname, String ssn, String phone, String gender, String address) {
        this.dob = dob;
        this.system_name = system_name;
        this.firstname = firstname;
        this.lastname = lastname;
        this.ssn = ssn;
        this.phone = phone;
        this.gender = gender;
        this.address = address;
    }
}
