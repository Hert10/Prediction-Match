package com.controle.teamservice.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int foundedYear;

    private String stadium;

    private int capacity;

    private String logo;

    private String league;

    private String city;
}

