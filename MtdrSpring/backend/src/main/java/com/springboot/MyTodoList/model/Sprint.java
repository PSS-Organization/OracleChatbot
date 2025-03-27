package com.springboot.MyTodoList.model;

//import jakarta.persistence.*;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
//import javax.persistence.Lob;

@Entity
@Table(name = "SPRINTS")  // Asegúrate que el nombre coincida con tu tabla real
public class Sprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SPRINTID")
    private Long sprintID;

    @Column(name = "NOMBRESPRINT", nullable = false)
    private String nombreSprint;

    @Column(name = "NUMEROSPRINT", nullable = false)
    private int numeroSprint;

    @Column(name = "STARTDATE")
    private Timestamp startDate;

    @Column(name = "ENDDATE")
    private Timestamp endDate;

    // ✅ Getters y Setters
    public Long getSprintID() {
        return sprintID;
    }

    public void setSprintID(Long sprintID) {
        this.sprintID = sprintID;
    }

    public String getNombreSprint() {
        return nombreSprint;
    }

    public void setNombreSprint(String nombreSprint) {
        this.nombreSprint = nombreSprint;
    }

    public int getNumeroSprint() {
        return numeroSprint;
    }

    public void setNumeroSprint(int numeroSprint) {
        this.numeroSprint = numeroSprint;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }
}
