package com.controle.teamservice.repository;


import com.controle.teamservice.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;



public interface TeamRepository extends JpaRepository<Team, Long> {

    boolean existsByName(String name);
    Team findByName(String name);

}

