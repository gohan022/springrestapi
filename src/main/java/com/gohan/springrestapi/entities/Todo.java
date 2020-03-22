package com.gohan.springrestapi.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@ToString
@Entity
@Table(name = "todos")
public class Todo extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;
    @NotEmpty(message = "it cant be empty")
    @NonNull
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;
    private boolean isDone;

    @JsonIgnoreProperties(value={"todos", "hibernateLazyInitializer", "handler"}, allowSetters=true)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_by", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;
}
