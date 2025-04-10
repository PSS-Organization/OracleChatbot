package com.springboot.MyTodoList.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.MyTodoList.model.Estado;
import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.model.Tarea;
import com.springboot.MyTodoList.model.Usuario;
import com.springboot.MyTodoList.repository.EstadoRepository;
import com.springboot.MyTodoList.repository.SprintRepository;
import com.springboot.MyTodoList.repository.TareaRepository;
import com.springboot.MyTodoList.repository.UsuarioRepository;

@Service
@Transactional(readOnly = true)
public class TareaService {

    private static final Logger logger = LoggerFactory.getLogger(TareaService.class);

    @Autowired
    private TareaRepository tareaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SprintRepository sprintRepository;

    @Autowired
    private EstadoRepository estadoRepository;

    @Cacheable("tareas")
    public List<Tarea> getAllTareas() {
        logger.debug("Fetching all tareas from database");
        return tareaRepository.findAll();
    }

    @Async
    public CompletableFuture<List<Tarea>> getAllTareasAsync() {
        logger.debug("Fetching all tareas asynchronously");
        return CompletableFuture.completedFuture(tareaRepository.findAll());
    }

    @Cacheable(value = "tareas", key = "#id")
    public ResponseEntity<Tarea> getTareaById(Long id) {
        logger.debug("Fetching tarea with id: {}", id);
        Optional<Tarea> tareaData = tareaRepository.findById(id);
        if (tareaData.isPresent()) {
            return new ResponseEntity<>(tareaData.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Transactional
    @CacheEvict(value = "tareas", allEntries = true)
    public Tarea createTarea(Tarea tarea) {
        // ✅ Asignar valores por defecto solo si no están presentes
        if (tarea.getFechaCreacion() == null) {
            tarea.setFechaCreacion(OffsetDateTime.now());
        }
        if (tarea.getCompletado() == null) {
            tarea.setCompletado(0); // 0 = false en base de datos
        }
        if (tarea.getPrioridad() == null) {
            tarea.setPrioridad("BAJA");
        }

        logger.info("Guardando tarea en BD: {}", tarea.getTareaNombre());

        return tareaRepository.save(tarea); // ✅ Devuelve solo la tarea creada
    }

    @Transactional
    @CacheEvict(value = "tareas", allEntries = true)
    public Tarea updateTarea(Long id, Tarea tarea) {
        logger.debug("Updating tarea with id: {}", id);
        Optional<Tarea> tareaData = tareaRepository.findById(id);
        if (tareaData.isPresent()) {
            Tarea existingTarea = tareaData.get();
            existingTarea.setTareaNombre(tarea.getTareaNombre());
            existingTarea.setDescripcion(tarea.getDescripcion());
            existingTarea.setFechaEntrega(tarea.getFechaEntrega());
            existingTarea.setCompletado(tarea.getCompletado());
            existingTarea.setPrioridad(tarea.getPrioridad());
            existingTarea.setHorasEstimadas(tarea.getHorasEstimadas());
            existingTarea.setHorasReales(tarea.getHorasReales());
            existingTarea.setUsuarioID(tarea.getUsuarioID());
            existingTarea.setSprintID(tarea.getSprintID());
            existingTarea.setEstadoID(tarea.getEstadoID());
            return tareaRepository.save(existingTarea);
        }
        return null;
    }

    @Transactional
    @CacheEvict(value = "tareas", allEntries = true)
    public boolean deleteTarea(Long id) {
        try {
            tareaRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            logger.error("Error deleting tarea with id: {}", id, e);
            return false;
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "tareas_usuario", key = "#usuarioID")
    public List<Tarea> getTareasByUsuario(Long usuarioID) {
        logger.debug("Fetching tareas for usuario with id: {}", usuarioID);
        return tareaRepository.findByUsuarioID(usuarioID);
    }

    @Transactional(readOnly = true)
    public Page<Tarea> getTareasByUsuarioPaginated(Long usuarioID, int page, int size) {
        logger.debug("Fetching paginated tareas for usuario with id: {}", usuarioID);
        Pageable pageable = PageRequest.of(page, size);
        return tareaRepository.findByUsuarioID(usuarioID, pageable);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "tareas_sprint", key = "#sprintID")
    public List<Tarea> getTareasBySprint(Long sprintID) {
        logger.debug("Fetching tareas for sprint with id: {}", sprintID);
        return tareaRepository.findBySprintID(sprintID);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "tareas_estado", key = "#estadoID")
    public List<Tarea> getTareasByEstado(Long estadoID) {
        logger.debug("Fetching tareas for estado with id: {}", estadoID);
        return tareaRepository.findByEstadoID(estadoID);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "usuarios")
    public List<Usuario> getAllUsuarios() {
        logger.debug("Fetching all usuarios");
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "sprints")
    public List<Sprint> getAllSprints() {
        logger.debug("Fetching all sprints");
        return sprintRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "estados")
    public List<Estado> getAllEstados() {
        logger.debug("Fetching all estados");
        return estadoRepository.findAll();
    }

    @Transactional
    @CacheEvict(value = { "tareas", "tareas_usuario", "tareas_sprint", "tareas_estado" }, allEntries = true)
    public List<Tarea> createTareas(List<Tarea> tareas) {
        logger.info("Batch saving {} tareas", tareas.size());
        for (Tarea tarea : tareas) {
            if (tarea.getFechaCreacion() == null) {
                tarea.setFechaCreacion(OffsetDateTime.now());
            }
            if (tarea.getCompletado() == null) {
                tarea.setCompletado(0);
            }
            if (tarea.getPrioridad() == null) {
                tarea.setPrioridad("BAJA");
            }
        }
        return tareaRepository.saveAll(tareas);
    }
}
