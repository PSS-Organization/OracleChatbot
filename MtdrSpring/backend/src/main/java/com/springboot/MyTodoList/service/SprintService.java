package com.springboot.MyTodoList.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.repository.SprintRepository;

@Service
public class SprintService {

    @Autowired
    private SprintRepository sprintRepository;

    /**
     * 1) Obtener todos los sprints (usa findAll de JpaRepository).
     */
    public List<Sprint> getAllSprints() {
        return sprintRepository.findAll();
    }

    /**
     * 2) Obtener un sprint por ID (usa findById).
     */
    public Optional<Sprint> getSprintById(Long id) {
        return sprintRepository.findById(id);
    }

    /**
     * 3) Crear un nuevo sprint (usa save).
     */
    public Sprint createSprint(Sprint sprint) {
        return sprintRepository.save(sprint);
    }

    /**
     * 4) Actualizar un sprint existente.
     *    - Buscamos por ID, si existe, copiamos valores y guardamos.
     */
    public Sprint updateSprint(Long id, Sprint sprintDetails) {
        return sprintRepository.findById(id).map(sprint -> {
            sprint.setNombreSprint(sprintDetails.getNombreSprint());
            sprint.setNumeroSprint(sprintDetails.getNumeroSprint());
            sprint.setStartDate(sprintDetails.getStartDate());
            sprint.setEndDate(sprintDetails.getEndDate());
            return sprintRepository.save(sprint);
        }).orElse(null);
    }

    /**
     * 5) Eliminar un sprint por su ID.
     */
    public boolean deleteSprint(Long id) {
        if (sprintRepository.existsById(id)) {
            sprintRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * 6) Obtener sprints activos (startDate < now < endDate).
     *    - Requiere un método personalizado en SprintRepository,
     *      ej. findByStartDateBeforeAndEndDateAfter(Timestamp start, Timestamp end).
     */
    public List<Sprint> getActiveSprints() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return sprintRepository.findByStartDateBeforeAndEndDateAfter(now, now);
    }

    /**
     * 7) Buscar sprints que comienzan en cierta fecha (usa findByStartDate).
     */
    public List<Sprint> getSprintsByStartDate(Timestamp date) {
        return sprintRepository.findByStartDate(date);
    }

    /**
     * 8) Calcular la duración de un sprint en días (endDate - startDate).
     */
    public Long getSprintDuration(Long id) {
        Optional<Sprint> sprintOpt = sprintRepository.findById(id);
        if (sprintOpt.isPresent()) {
            Sprint sprint = sprintOpt.get();
            if (sprint.getStartDate() != null && sprint.getEndDate() != null) {
                // Conversión a milisegundos y luego a días
                long diff = sprint.getEndDate().getTime() - sprint.getStartDate().getTime();
                return diff / (1000 * 60 * 60 * 24); // milisegundos en un día
            }
        }
        return null;
    }
}
