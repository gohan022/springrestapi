package com.gohan.springrestapi.entities;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "todos")
/*@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")*/
public class Todo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    @NotEmpty(message = "it cant be empty")
    @NonNull
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @NotNull(message = "Target date required!")
    @NonNull
    // @Temporal(TemporalType.DATE) // To store only date
    @Column(nullable = false)
    //@JsonFormat(pattern = "yyyy-mm-dd")
    private Date targetDate;

    private boolean isDone;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "work_by", updatable = false, nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    //@JsonIgnoreProperties(value={"todos", "hibernateLazyInitializer", "handler"}, allowSetters=true)
    //@JsonManagedReference
    @JsonIgnore
    private User user;

    /*
    @PrePersist
    private void onCreate(){}
    @PreUpdate
    private void onUpdate(){}
    */
}
