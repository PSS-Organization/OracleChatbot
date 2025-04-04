package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.Tarea;
import com.springboot.MyTodoList.model.Usuario;
import com.springboot.MyTodoList.service.TareaService;
import com.springboot.MyTodoList.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private TareaService tareaService;

    @Autowired
    private UsuarioService usuarioService;

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
}