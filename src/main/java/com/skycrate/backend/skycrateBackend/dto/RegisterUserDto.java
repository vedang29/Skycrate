package com.skycrate.backend.skycrateBackend.dto;

public class RegisterUserDto {
 
    private String email;
    private String password;
    private String firstname;
    private String lastname;

    public String getEmail() {
        return email;
    }

    public RegisterUserDto setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public RegisterUserDto setPassword(String password) {
        this.password = password;
        return this;

    }

    public String getFirstname() {
        return firstname;
    }

    public RegisterUserDto setFirstname(String firstname) {
        this.firstname = firstname;
        return this;
    }

    public String getLastname() {
        return lastname;
    }

    public RegisterUserDto setLastname(String lastname) {
        this.lastname = lastname;
        return this;
    }
}
