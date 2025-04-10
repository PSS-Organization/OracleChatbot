package com.springboot.MyTodoList.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springboot.MyTodoList.model.Tarea;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {

    List<Tarea> findByUsuarioID(Long usuarioID);

    // Add pagination support
    Page<Tarea> findByUsuarioID(Long usuarioID, Pageable pageable);

    List<Tarea> findBySprintID(Long sprintID);

    // Add pagination support
    Page<Tarea> findBySprintID(Long sprintID, Pageable pageable);

    List<Tarea> findByEstadoID(Long estadoID);

    // Add pagination support
    Page<Tarea> findByEstadoID(Long estadoID, Pageable pageable);

    // Obtener tareas por usuario ordenadas por fecha de entrega
    List<Tarea> findByUsuarioIDOrderByFechaEntregaAsc(Long usuarioID);

    List<Tarea> findByCompletado(Integer completado);

    // Add pagination support
    Page<Tarea> findByCompletado(Integer completado, Pageable pageable);

    // Obtener tareas por prioridad
    List<Tarea> findByPrioridad(String prioridad);

    // Add pagination support
    Page<Tarea> findByPrioridad(String prioridad, Pageable pageable);
}
