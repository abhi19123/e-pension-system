package com.epension.dto;

import com.epension.model.User;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UserDto {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String role;
    private String employeeId;
    private String department;
    private String aadharNumber;
    private LocalDate dateOfBirth;
    private LocalDate retirementDate;
    private String pensionType;
    private String profilePicture;
    private boolean emailVerified;
    private boolean active;

    public static UserDto fromUser(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole());
        dto.setEmployeeId(user.getEmployeeId());
        dto.setDepartment(user.getDepartment());
        dto.setAadharNumber(user.getAadharNumber());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setRetirementDate(user.getRetirementDate());
        dto.setPensionType(user.getPensionType());
        dto.setProfilePicture(user.getProfilePicture());
        dto.setEmailVerified(user.isEmailVerified());
        dto.setActive(user.isActive());
        return dto;
    }
}
