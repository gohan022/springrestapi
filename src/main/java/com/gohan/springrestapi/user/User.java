package com.gohan.springrestapi.user;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@ToString
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;
    @NotEmpty(message = "Please enter a first name.")
    @NonNull
    @Column(nullable = false, length = 50)
    private String firstName;
    @NotEmpty(message = "Please enter a last name")
    @NonNull
    @Column(length = 30)
    private String lastName;
    @NotEmpty(message = "Please enter a username.")
    @NonNull
    @Column(nullable = false, length = 20)
    private String username;
    @NotEmpty(message = "Please enter a email.")
    @Size(min = 4, max = 30)
    @NonNull
    @Email
    @Column(nullable = false, unique = true)
    private String email;
    @NotEmpty(message = "Please enter a password.")
    @NonNull
    @Column(nullable = false)
    private String password;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Transient
    @Setter(AccessLevel.NONE)
    private String fullName;
    @Transient
    private String confirmPassword;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    @Setter(AccessLevel.NONE)
    private Set<Role> roles = new HashSet<>();

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public void addRole(Role role) {
        roles.add(role);
    }

    public void addRoles(Set<Role> roles) {
        roles.forEach(this::addRole);
    }
}
