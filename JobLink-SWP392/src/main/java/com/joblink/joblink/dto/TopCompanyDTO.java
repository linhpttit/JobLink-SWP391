package com.joblink.joblink.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopCompanyDTO {
    private int employerId;
    private String companyName;
    private String location;
    private int openPositions;

    public int getEmployerId() {
        return employerId;
    }

    public void setEmployerId(int employerId) {
        this.employerId = employerId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getOpenPositions() {
        return openPositions;
    }

    public void setOpenPositions(int openPositions) {
        this.openPositions = openPositions;
    }
    public String getCompanyInitials() {
        if (companyName != null && companyName.length() >= 2) {
            return companyName.substring(0, 2).toUpperCase();
        }
        return "CO";
    }

    public String getCompanyColor() {
        if (companyName != null) {
            int hash = companyName.hashCode();
            // Sử dụng bitwise AND trong Java thay vì SpEL
            return String.format("#%06X", (hash & 0xFFFFFF));
        }
        return "#3b82f6"; // Default blue color
    }
}