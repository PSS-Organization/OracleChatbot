package com.springboot.MyTodoList.model;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "TAREAS")
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TAREAID")
    private Long tareaID;

    @Column(name = "TAREANOMBRE", length = 100)
    private String tareaNombre;

    @Column(name = "DESCRIPCION", nullable = false)
    @Lob
    private String descripcion;

    @Column(name = "FECHAENTREGA")
    private OffsetDateTime fechaEntrega;

    @Column(name = "FECHACREACION")
    private OffsetDateTime fechaCreacion;

    @Column(name = "COMPLETADO", nullable = false)
    private Boolean completado;

    @Column(name = "PRIORIDAD")
    private String prioridad;

    @Column(name = "HORASESTIMADAS")
    private Integer horasEstimadas;

    @Column(name = "HORASREALES")
    private Integer horasReales;

    @Column(name = "USUARIOID")
    private Long usuarioID;

    @Column(name = "SPRINTID")
    private Long sprintID;

    @Column(name = "ESTADOID")
    private Long estadoID;

    // Constructor vacío
    public Tarea() {
    }

    // Constructor completo
    public Tarea(String tareaNombre, String descripcion, OffsetDateTime fechaEntrega,
            Boolean completado, String prioridad, Integer horasEstimadas,
            Integer horasReales, Long usuarioID, Long sprintID, Long estadoID) {
        this.tareaNombre = tareaNombre;
        this.descripcion = descripcion;
        this.fechaEntrega = fechaEntrega;
        this.fechaCreacion = OffsetDateTime.now();
        this.completado = completado;
        this.prioridad = prioridad;
        this.horasEstimadas = horasEstimadas;
        this.horasReales = horasReales;
        this.usuarioID = usuarioID;
        this.sprintID = sprintID;
        this.estadoID = estadoID;
    }

    // Getters y Setters
    public Long getTareaID() {
        return tareaID;
    }

    public void setTareaID(Long tareaID) {
        this.tareaID = tareaID;
    }

    public String getTareaNombre() {
        return tareaNombre;
    }

    public void setTareaNombre(String tareaNombre) {
        this.tareaNombre = tareaNombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public OffsetDateTime getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(OffsetDateTime fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public OffsetDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(OffsetDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Boolean getCompletado() {
        return completado;
    }

    public void setCompletado(Boolean completado) {
        this.completado = completado;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public Integer getHorasEstimadas() {
        return horasEstimadas;
    }

    public void setHorasEstimadas(Integer horasEstimadas) {
        this.horasEstimadas = horasEstimadas;
    }

    public Integer getHorasReales() {
        return horasReales;
    }

    public void setHorasReales(Integer horasReales) {
        this.horasReales = horasReales;
    }

    public Long getUsuarioID() {
        return usuarioID;
    }

    public void setUsuarioID(Long usuarioID) {
        this.usuarioID = usuarioID;
    }

    public Long getSprintID() {
        return sprintID;
    }

    public void setSprintID(Long sprintID) {
        this.sprintID = sprintID;
    }

    public Long getEstadoID() {
        return estadoID;
    }

    public void setEstadoID(Long estadoID) {
        this.estadoID = estadoID;
    }

    @Override
    public String toString() {
        return "Tarea{"
                + "tareaID=" + tareaID
                + ", tareaNombre='" + tareaNombre + '\''
                + ", descripcion='" + descripcion + '\''
                + ", fechaEntrega=" + fechaEntrega
                + ", fechaCreacion=" + fechaCreacion
                + ", completado=" + completado
                + ", prioridad='" + prioridad + '\''
                + ", horasEstimadas=" + horasEstimadas
                + ", horasReales=" + horasReales
                + ", usuarioID=" + usuarioID
                + ", sprintID=" + sprintID
                + ", estadoID=" + estadoID
                + '}';
    }
}
