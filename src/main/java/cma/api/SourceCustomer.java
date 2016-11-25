package cma.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;


/**
 * Created by sebastianestevez on 11/22/16.
 */
public class SourceCustomer {

    @JsonProperty
    private String sourceid;

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

    SourceCustomer(){}
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getSourceid() {
        return sourceid;
    }

    public void setSourceid(String sourceid) {
        this.sourceid = sourceid;
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    public SourceCustomer(String sourceid, int dob, String system_name, String firstname, String lastname, String ssn, String phone, String gender, String address) {
        this.sourceid = sourceid;
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
