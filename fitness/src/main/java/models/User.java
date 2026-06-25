package models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class User {

    private int id;
    private String fullName;
    private String email;
    private String password;
    private String phone;
    private LocalDate birthDate;
    private String role;
    private Integer membershipId;
    private LocalDate membershipStart;
    private LocalDate membershipEnd;
    private LocalDateTime createdAt;

    public User() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getMembershipId() { return membershipId; }
    public void setMembershipId(Integer membershipId) { this.membershipId = membershipId; }

    public LocalDate getMembershipStart() { return membershipStart; }
    public void setMembershipStart(LocalDate membershipStart) { this.membershipStart = membershipStart; }

    public LocalDate getMembershipEnd() { return membershipEnd; }
    public void setMembershipEnd(LocalDate membershipEnd) { this.membershipEnd = membershipEnd; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}