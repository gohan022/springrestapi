package com.gohan.springrestapi.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@EqualsAndHashCode(callSuper = true)
@Data
@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    @NonNull
    @Column(nullable = false, length = 100)
    private String firstName;

    @NonNull
    @Column(nullable = false, length = 60)
    private String lastName;

    @NonNull
    @Column(nullable = false, length = 50)
    private String username;

    @NonNull
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
}
