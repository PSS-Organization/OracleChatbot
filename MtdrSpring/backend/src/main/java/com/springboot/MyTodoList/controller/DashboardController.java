package com.springboot.MyTodoList.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.MyTodoList.model.Tarea;
import com.springboot.MyTodoList.model.Usuario;
import com.springboot.MyTodoList.service.TareaService;
import com.springboot.MyTodoList.service.UsuarioService;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private TareaService tareaService;

    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/user-stats")
    public ResponseEntity<List<Map<String, Object>>> getUserStats() {
        try {
            // Fetch all tasks and users
            List<Tarea> allTasks = tareaService.getAllTareas();
            List<Usuario> allUsers = usuarioService.getAllUsuarios();

            // Map to store aggregated data for each user
            List<Map<String, Object>> userStats = new ArrayList<>();

            for (Usuario user : allUsers) {
                // Filter tasks for the current user
                List<Tarea> userTasks = allTasks.stream()
                        .filter(task -> task.getUsuarioID() != null && task.getUsuarioID().equals(user.getUsuarioID()))
                        .collect(Collectors.toList());

                // Calculate task statistics
                int totalTasks = userTasks.size();
                int completedTasks = (int) userTasks.stream()
                        .filter(task -> task.getCompletado() != null && task.getCompletado() == 1) // Explicitly check for "completed"
                        .count();
                int tasksToDo = totalTasks - completedTasks;
                int percentageCompleted = totalTasks > 0 ? (completedTasks * 100) / totalTasks : 0;

                // Calculate total real hours
                int totalRealHours = userTasks.stream()
                        .filter(task -> task.getCompletado() != null && task.getCompletado() == 1) // Explicitly check for "completed"
                        .mapToInt(task -> task.getHorasReales() != null ? task.getHorasReales() : 0)
                        .sum();

                // Add user stats to the list
                Map<String, Object> userStat = new HashMap<>();
                userStat.put("userName", user.getNombre());
                userStat.put("userId", user.getUsuarioID());
                userStat.put("totalTasks", totalTasks);
                userStat.put("tasksToDo", tasksToDo);
                userStat.put("completedTasks", completedTasks);
                userStat.put("percentageCompleted", percentageCompleted);
                userStat.put("totalRealHours", totalRealHours);

                userStats.add(userStat);
            }

            return ResponseEntity.ok(userStats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonList(Map.of("error", "Error fetching user stats: " + e.getMessage())));
        }
    }
    
    @GetMapping("/task-deviation")
    public ResponseEntity<List<Map<String, Object>>> getTaskDeviation(
            @RequestParam(required = false) List<Integer> sprintIds) {
        try {
            // Default to sprints 6,7 if no specific sprints are provided
            String sprintFilter = " WHERE s.numeroSprint IN (6, 7) ";
            
            // If specific sprint IDs are provided, use those instead
            if (sprintIds != null && !sprintIds.isEmpty()) {
                sprintFilter = " WHERE s.sprintID IN (" + 
                    sprintIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(",")) + 
                    ") ";
            }
            
            // SQL query based on the provided SQL
            String sql = "WITH tareas_por_usuario AS (\n" +
                    "    SELECT \n" +
                    "        u.usuarioID,\n" +
                    "        u.nombre AS usuario,\n" +
                    "        COUNT(t.tareaID) AS tareas_asignadas\n" +
                    "    FROM Tareas t\n" +
                    "    JOIN Usuarios u ON t.usuarioID = u.usuarioID\n" +
                    "    JOIN Sprints s ON t.sprintID = s.sprintID\n" +
                    sprintFilter +
                    "    GROUP BY u.usuarioID, u.nombre\n" +
                    "),\n" +
                    "media_global AS (\n" +
                    "    SELECT \n" +
                    "        ROUND(AVG(tareas_asignadas), 2) AS promedio_global\n" +
                    "    FROM tareas_por_usuario\n" +
                    ")\n" +
                    "SELECT \n" +
                    "    tu.usuarioID,\n" +
                    "    tu.usuario,\n" +
                    "    tu.tareas_asignadas,\n" +
                    "    ROUND(tu.tareas_asignadas - mg.promedio_global, 2) AS desviacion_individual\n" +
                    "FROM tareas_por_usuario tu\n" +
                    "CROSS JOIN media_global mg\n" +
                    "ORDER BY desviacion_individual DESC";

            // Execute the query
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
            
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.singletonList(Map.of("error", "Error fetching task deviation: " + e.getMessage())));
        }
    }
}