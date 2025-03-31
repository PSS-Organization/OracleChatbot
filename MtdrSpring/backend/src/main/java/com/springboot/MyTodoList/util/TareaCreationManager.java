package com.springboot.MyTodoList.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.springboot.MyTodoList.model.TareaCreationState;

public class TareaCreationManager {

    // Utilizamos ConcurrentHashMap para seguridad en múltiples hilos (bot puede tener múltiples usuarios al mismo tiempo)
    private static final Map<Long, TareaCreationState> creationStates = new ConcurrentHashMap<>();

    // Iniciar proceso de creación de tarea para un usuario
    public static void startCreation(Long chatId) {
        creationStates.put(chatId, new TareaCreationState(chatId));
    }

    // Obtener el estado actual del usuario
    public static TareaCreationState getState(Long chatId) {
        return creationStates.get(chatId);
    }

    // Verificar si el usuario está en proceso de creación
    public static boolean isInCreationProcess(Long chatId) {
        return creationStates.containsKey(chatId);
    }

    // Limpiar el estado después de completar o cancelar
    public static void clearState(Long chatId) {
        creationStates.remove(chatId);
    }
}
