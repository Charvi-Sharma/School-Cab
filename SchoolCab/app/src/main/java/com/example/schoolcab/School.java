package com.example.schoolcab;

public class School {

    private String name;
    private String board;
    private Boolean verifiedStatus;
    private String licenseNo;
    private String password;
    private String email;
    private String mobileNo;

    private Integer busCapacity;

    public School(String name, String board, Boolean verifiedStatus, String email, String password, String licenseNo, String mobileNo, Integer busCapacity) {
        this.name = name;
        this.board = board;
        this.verifiedStatus = verifiedStatus;
        this.licenseNo = licenseNo;
        this.password = password;
        this.email = email;
        this.busCapacity = busCapacity;
        this.mobileNo = mobileNo;
    }

    public School() {

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

    public String getBoard() {
        return board;
    }

    public void setBoard(String board) {
        this.board = board;
    }

    public Boolean getVerifiedStatus() {
        return verifiedStatus;
    }

    public void setVerifiedStatus(Boolean verifiedStatus) {
        this.verifiedStatus = verifiedStatus;
    }

    public String getLicenseNo() {
        return licenseNo;
    }

    public void setLicenseNo(String licenseNo) {
        this.licenseNo = licenseNo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }



    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public Integer getBusCapacity() {
        return busCapacity;
    }

    public void setBusCapacity(Integer busCapacity) {
        this.busCapacity = busCapacity;
    }
}
