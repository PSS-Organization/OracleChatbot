package com.springboot.MyTodoList.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.MyTodoList.model.Tarea;
import com.springboot.MyTodoList.service.TareaService;

@RestController
@RequestMapping("/tareas")  // Define la ruta base
@CrossOrigin(origins = "*")     // Permite peticiones de cualquier origen (CORS)
public class TareaController {

    @Autowired
    private TareaService tareaService;

    @GetMapping
    public ResponseEntity<List<Tarea>> getAllTareas() {
        List<Tarea> tareas = tareaService.getAllTareas();
        return new ResponseEntity<>(tareas, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTareaById(@PathVariable Long id) {
        ResponseEntity<Tarea> tarea = tareaService.getTareaById(id);
        if (tarea.getBody() != null) {
            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "tarea", tarea.getBody()
            ));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> createTarea(@RequestBody Tarea tarea) {
        try {
            System.out.println("📌 Recibiendo solicitud para crear tarea: " + tarea.toString());
    
            // ✅ Validar que los campos esenciales no sean nulos
            if (tarea.getTareaNombre() == null || tarea.getDescripcion() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "El nombre de la tarea y la descripción son obligatorios."
                ));
            }
    
            // ✅ Dejar que `TareaService` maneje los valores por defecto
            Tarea nuevaTarea = tareaService.createTarea(tarea);
    
            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "message", "Tarea creada exitosamente",
                    "tarea", nuevaTarea
            ));
        } catch (Exception e) {
            System.err.println("❌ Error al crear la tarea: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error al crear la tarea: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTarea(@PathVariable Long id, @RequestBody Tarea tarea) {
        try {
            Tarea tareaActualizada = tareaService.updateTarea(id, tarea);
            if (tareaActualizada != null) {
                return ResponseEntity.ok().body(Map.of(
                        "success", true,
                        "message", "Tarea actualizada exitosamente",
                        "tarea", tareaActualizada
                ));
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error al actualizar la tarea: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTarea(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean deleted = tareaService.deleteTarea(id);
            if (deleted) {
                response.put("success", true);
                response.put("message", "Tarea eliminada exitosamente");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "No se encontró la tarea");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al eliminar la tarea");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/usuario/{usuarioID}")
    public ResponseEntity<List<Tarea>> getTareasByUsuario(@PathVariable Long usuarioID) {
        List<Tarea> tareas = tareaService.getTareasByUsuario(usuarioID);
        return new ResponseEntity<>(tareas, HttpStatus.OK);
    }

    @GetMapping("/sprint/{sprintID}")
    public ResponseEntity<List<Tarea>> getTareasBySprint(@PathVariable Long sprintID) {
        List<Tarea> tareas = tareaService.getTareasBySprint(sprintID);
        return new ResponseEntity<>(tareas, HttpStatus.OK);
    }

    @GetMapping("/estado/{estadoID}")
    public ResponseEntity<List<Tarea>> getTareasByEstado(@PathVariable Long estadoID) {
        List<Tarea> tareas = tareaService.getTareasByEstado(estadoID);
        return new ResponseEntity<>(tareas, HttpStatus.OK);
    }
}
