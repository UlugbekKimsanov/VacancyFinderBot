package com.example.vacancyfinderbot.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class Channel {
    private String lastPost;
    @Id
    private String name;
    @ManyToOne
    @JoinColumn(name = "userId")
    private UserEntity userEntity;
}
