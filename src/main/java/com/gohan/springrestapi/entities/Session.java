package com.gohan.springrestapi.entities;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Data
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "sessions")
public class Session extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;
    @Column(length = 60, nullable = false)
    @NonNull
    private String ipAddress;
    @Column(columnDefinition = "TEXT", nullable = false)
    @NonNull
    private String userAgent;
    @Column(columnDefinition = "TEXT")
    @NonNull
    private String payload;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "user_id", updatable = false, nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NonNull
    private User user;
}
