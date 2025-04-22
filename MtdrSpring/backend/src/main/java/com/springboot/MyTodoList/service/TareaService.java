package com.springboot.MyTodoList.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.springboot.MyTodoList.model.Estado;
import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.model.Tarea;
import com.springboot.MyTodoList.model.Usuario;
import com.springboot.MyTodoList.repository.EstadoRepository;
import com.springboot.MyTodoList.repository.SprintRepository;
import com.springboot.MyTodoList.repository.TareaRepository;
import com.springboot.MyTodoList.repository.UsuarioRepository;

@Service
public class TareaService {

    @Autowired
    private TareaRepository tareaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SprintRepository sprintRepository;

    @Autowired
    private EstadoRepository estadoRepository;

    public List<Tarea> getAllTareas() {
        return tareaRepository.findAll();
    }

    public ResponseEntity<Tarea> getTareaById(Long id) {
        Optional<Tarea> tareaData = tareaRepository.findById(id);
        if (tareaData.isPresent()) {
            return new ResponseEntity<>(tareaData.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

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

        System.out.println("✅ Guardando tarea en BD: " + tarea.toString());

        return tareaRepository.save(tarea); // ✅ Devuelve solo la tarea creada
    }

    public Tarea updateTarea(Long id, Tarea tarea) {
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

    public boolean deleteTarea(Long id) {
        try {
            tareaRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<Tarea> getTareasByUsuario(Long usuarioID) {
        return tareaRepository.findByUsuarioID(usuarioID);
    }

    public List<Tarea> getTareasBySprint(Long sprintID) {
        return tareaRepository.findBySprintID(sprintID);
    }

    public List<Tarea> getTareasByEstado(Long estadoID) {
        return tareaRepository.findByEstadoID(estadoID);
    }

    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public List<Sprint> getAllSprints() {
        return sprintRepository.findAll();
    }

    public List<Estado> getAllEstados() {
        return estadoRepository.findAll();
    }
}
