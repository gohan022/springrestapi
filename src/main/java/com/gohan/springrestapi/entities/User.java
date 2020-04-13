package com.gohan.springrestapi.entities;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.*;

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
public class User extends Auditable implements UserDetails {
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
    @Column(nullable = false)
    private boolean enabled;

    @Transient
    @Setter(AccessLevel.NONE)
    private String fullName;
    @Transient
    @JsonIgnore
    private String confirmPassword;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Role role;

   /* @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL)
    @Setter(AccessLevel.NONE)
    //@JsonIgnoreProperties(value={"user", "hibernateLazyInitializer", "handler"}, allowSetters=true)
    //@JsonBackReference
    @JsonIgnore
    private List<Todo> todos = new ArrayList<>();

    public void addTodo(Todo todo) {
        todos.add(todo);
    }

    public void addTodos(List<Todo> todos) {
        todos.forEach(this::addTodo);
    }*/

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        /*List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role.name()));
        return authorities;*/
        return Collections.singletonList(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
