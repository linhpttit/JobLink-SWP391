package com.joblink.joblink.auth.model;
public class Skill {
    private Integer id;
    private String name;
    private Integer level; // 0..100
    // getters/setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
}
