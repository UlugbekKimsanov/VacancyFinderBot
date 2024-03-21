package com.example.vacancyfinderbot.repository;


import com.example.vacancyfinderbot.entity.UserEntity;
import com.example.vacancyfinderbot.entity.Vacancy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VacancyRepository extends JpaRepository<Vacancy,String> {
    List<Vacancy> findAllByOwnerIs(UserEntity user);
}
