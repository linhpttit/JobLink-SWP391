package com.joblink.joblink.auth.model;
public class Experience {
    private Integer id;
    private String title;
    private String company;
    private String period;       // ví dụ "2022 - nay"
    private String description;
    // getters/setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
