package com.sustbbgz.virtualspringbootbackend.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name = "users")
public class User {

    @Id
    @TableGenerator(name ="id")
    private Long id;

    @TableGenerator(name="username")
    private String username;

    @TableGenerator(name="password")
    private String password;

    @TableGenerator(name="email")
    private String email;

    @TableGenerator(name="phone")
    private String phone;

    @TableGenerator(name="role")
    private String role;

    @TableGenerator(name="is_deleted")
    private int is_deleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getIs_deleted() {
        return is_deleted;
    }

    public void setIs_deleted(int is_deleted) {
        this.is_deleted = is_deleted;
    }
}
