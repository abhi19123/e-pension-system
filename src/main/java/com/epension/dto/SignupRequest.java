package com.epension.dto;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class SignupRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(\\+91)?[0-9]{10}$", message = "Please enter a 10-digit phone number. +91 prefix is optional")
    private String phoneNumber;

    @NotBlank(message = "Role is required")
    private String role = "PENSIONER";

    private String employeeId;
    private String department;
    private String aadharNumber;
    private String dateOfBirth;
    private String retirementDate;
    private String pensionType;
}
