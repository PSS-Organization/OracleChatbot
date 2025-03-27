package com.springboot.MyTodoList.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springboot.MyTodoList.model.Sprint;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, Long> {
    // OPCIONALES - Métodos personalizados

    // Ejemplo: Encuentra sprints que comiencen en fecha exacta
    List<Sprint> findByStartDate(Timestamp startDate);

    // Ejemplo: Encuentra sprints activos entre 'startDate < now < endDate'
    // Podrías usar algo como:
    List<Sprint> findByStartDateBeforeAndEndDateAfter(Timestamp now1, Timestamp now2);

    List<Sprint> findByNombreSprint(String nombreSprint);

    List<Sprint> findByOrderByNumeroSprintAsc();


    




}
