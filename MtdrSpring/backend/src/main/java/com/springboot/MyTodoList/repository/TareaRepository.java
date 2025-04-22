package com.springboot.MyTodoList.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springboot.MyTodoList.model.Tarea;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {

    List<Tarea> findByUsuarioID(Long usuarioID);

    List<Tarea> findBySprintID(Long sprintID);

    List<Tarea> findByEstadoID(Long estadoID);

    // Obtener tareas por usuario ordenadas por fecha de entrega
    List<Tarea> findByUsuarioIDOrderByFechaEntregaAsc(Long usuarioID);
    

    List<Tarea> findByCompletado(Integer completado);


    // Obtener tareas por prioridad
    List<Tarea> findByPrioridad(String prioridad);
}
