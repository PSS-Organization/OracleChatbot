package com.springboot.MyTodoList.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.springboot.MyTodoList.model.Tarea;
import com.springboot.MyTodoList.model.Usuario;
import com.springboot.MyTodoList.service.TareaService;
import com.springboot.MyTodoList.service.UsuarioService;

@RestController
@RequestMapping("/home")
@CrossOrigin(origins = "*")
public class HomeController {

    @Autowired
    private TareaService tareaService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/user-kpis")
    public ResponseEntity<Map<String, Object>> getUserKPIs(@RequestHeader("userId") Long loggedInUserId) {
        try {
            if (loggedInUserId == null) {
                return ResponseEntity.status(403).body(Map.of("error", "User ID is missing"));
            }

            // Fetch user details
            ResponseEntity<Usuario> userResponse = usuarioService.getUsuarioById(loggedInUserId);
            if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }
            Usuario user = userResponse.getBody();

            // Fetch tasks for the logged-in user
            List<Tarea> userTasks = tareaService.getTareasByUsuario(loggedInUserId);

            // Calculate KPIs
            int totalTasks = userTasks.size();
            int completedTasks = (int) userTasks.stream()
                    .filter(task -> task.getCompletado() != null && task.getCompletado() == 1)
                    .count();
            int percentageCompleted = totalTasks > 0 ? (completedTasks * 100) / totalTasks : 0;

            int totalRealHours = userTasks.stream()
                    .filter(task -> task.getHorasReales() != null)
                    .mapToInt(Tarea::getHorasReales)
                    .sum();

            int totalEstimatedHours = userTasks.stream()
                    .filter(task -> task.getHorasEstimadas() != null)
                    .mapToInt(Tarea::getHorasEstimadas)
                    .sum();

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("userName", user.getNombre());
            response.put("totalTasks", totalTasks);
            response.put("completedTasks", completedTasks);
            response.put("percentageCompleted", percentageCompleted);
            response.put("totalRealHours", totalRealHours);
            response.put("totalEstimatedHours", totalEstimatedHours);
            response.put("tasks", userTasks);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error fetching user KPIs: " + e.getMessage()));
        }
    }
}