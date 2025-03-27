package com.springboot.MyTodoList.controller;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.service.SprintService;

@RestController
@RequestMapping("/sprints")
@CrossOrigin(origins = "*")
public class SprintController {

    @Autowired
    private SprintService sprintService;

    @GetMapping
    public List<Sprint> getAllSprints() {
        return sprintService.getAllSprints();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sprint> getSprintById(@PathVariable Long id) {
        Optional<Sprint> sprintOpt = sprintService.getSprintById(id);
        if (sprintOpt.isPresent()) {
            return ResponseEntity.ok(sprintOpt.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Sprint> createSprint(@RequestBody Sprint sprint) {
        Sprint newSprint = sprintService.createSprint(sprint);
        return ResponseEntity.ok(newSprint);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sprint> updateSprint(@PathVariable Long id, @RequestBody Sprint sprintDetails) {
        Sprint updated = sprintService.updateSprint(id, sprintDetails);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSprint(@PathVariable Long id) {
        boolean deleted = sprintService.deleteSprint(id);
        if (deleted) {
            return ResponseEntity.ok("Sprint deleted successfully.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Sprints activos
    @GetMapping("/active")
    public List<Sprint> getActiveSprints() {
        return sprintService.getActiveSprints();
    }

    // Sprints por fecha de inicio
    @GetMapping("/startdate/{date}")
    public List<Sprint> getSprintsByStartDate(@PathVariable String date) {
        // Simple parse a Timestamp, ajusta si requieres otro formato
        Timestamp ts = Timestamp.valueOf(date + " 00:00:00");
        return sprintService.getSprintsByStartDate(ts);
    }

    // Duración de un sprint
    @GetMapping("/duration/{id}")
    public ResponseEntity<Long> getSprintDuration(@PathVariable Long id) {
        Long duration = sprintService.getSprintDuration(id);
        if (duration != null) {
            return ResponseEntity.ok(duration);
        }
        return ResponseEntity.notFound().build();
    }
}
