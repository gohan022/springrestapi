package com.gohan.springrestapi.entities;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@ToString
@Entity
@Table(name = "users")
/*@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")*/
public class User extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;
    @NotEmpty(message = "Please enter a first name.")
    @Size(min = 3, max = 100)
    @NonNull
    @Column(nullable = false, length = 100)
    private String firstName;
    @NotEmpty(message = "Please enter a last name")
    @Size(min = 2, max = 60)
    @NonNull
    @Column(length = 60)
    private String lastName;
    @NotEmpty(message = "Please enter a username.")
    @Size(min = 4, max = 50)
    @NonNull
    @Column(nullable = false, length = 50)
    private String username;
    @NotEmpty(message = "Please enter a email.")
    @Size(min = 4, max = 80)
    @NonNull
    @Email
    @Column(nullable = false, unique = true, length = 80)
    private String email;
    @NotEmpty(message = "Please enter a password.")
    @NonNull
    @Column(nullable = false)
    private String password;

    @Transient
    @Setter(AccessLevel.NONE)
    private String fullName;
    @Transient
    @JsonIgnore
    private String confirmPassword;

    @JsonIgnoreProperties(value={"users", "hibernateLazyInitializer", "handler" })
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    @Setter(AccessLevel.NONE)
    private Set<Role> roles = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL)
    @Setter(AccessLevel.NONE)
    //@JsonIgnoreProperties(value={"user", "hibernateLazyInitializer", "handler"}, allowSetters=true)
    //@JsonBackReference
    @JsonIgnore
    private List<Todo> todos = new ArrayList<>();

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public void addRole(Role role) {
        roles.add(role);
    }

    public void addRoles(Set<Role> roles) {
        roles.forEach(this::addRole);
    }

    public void addTodo(Todo todo) {
        todos.add(todo);
    }

    public void addTodos(List<Todo> todos) {
        todos.forEach(this::addTodo);
    }
}
