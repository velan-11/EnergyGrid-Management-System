package com.cts.identity_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String name;

    @Column(unique = true, nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // deleted part (Soft delete) (Only done by Admin)
    @Column(nullable = false)
    private boolean deleted = false;

    // deletedAt part (for audit)
    private LocalDateTime deletedAt;


    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if(this.role == Role.CUSTOMER){
            this.status= Status.ACTIVE;
        }

        if(this.status == null) {
            this.status = Status.PENDING;
        }

    }

    public enum Role {
        OPERATOR, TECHNICIAN, PRODUCER, CUSTOMER, ADMIN, AUDITOR
    }

    public enum Status {
        ACTIVE, PENDING, INACTIVE, LOCKED
    }

}
