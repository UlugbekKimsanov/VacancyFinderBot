package com.example.vacancyfinderbot.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    @Id
    private Long userId;
    @Enumerated(EnumType.STRING)
    private State state;
    private String kalitSoz;
    private String name;

    {
        this.state = State.Default;
    }
}
