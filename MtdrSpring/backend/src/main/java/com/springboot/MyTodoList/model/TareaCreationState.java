package com.springboot.MyTodoList.model;

import java.time.OffsetDateTime;

public class TareaCreationState {

    private Long chatId;
    private String currentField;
    private Tarea tarea;

    public TareaCreationState(Long chatId) {
        this.chatId = chatId;
        this.currentField = "NOMBRE";
        this.tarea = new Tarea();
        // Valores predeterminados
        this.tarea.setCompletado(0);
        this.tarea.setEstadoID(1L); // Estado "Por Hacer" por defecto
        this.tarea.setFechaCreacion(OffsetDateTime.now());
    }

    public Long getChatId() {
        return chatId;
    }

    public String getCurrentField() {
        return currentField;
    }

    public void setCurrentField(String currentField) {
        this.currentField = currentField;
    }

    public Tarea getTarea() {
        return tarea;
    }

    public boolean isComplete() {
        return tarea.getTareaNombre() != null
                && tarea.getDescripcion() != null
                && tarea.getFechaEntrega() != null
                && tarea.getPrioridad() != null
                && tarea.getHorasEstimadas() != null
                && tarea.getUsuarioID() != null
                && tarea.getSprintID() != null
                && tarea.getEstadoID() != null;
    }
}
