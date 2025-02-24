package ru.ffanjex.weatherforecast.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @SequenceGenerator(name = "users_id_gen", sequenceName = "users_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "city_id")
    private City city;

}