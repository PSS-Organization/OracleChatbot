package com.springboot.MyTodoList.util;

import java.util.HashMap;
import java.util.Map;

import com.springboot.MyTodoList.model.TareaCreationState;

public class TareaCreationManager {

    private static Map<Long, TareaCreationState> creationStates = new HashMap<>();

    public static void startCreation(Long chatId) {
        creationStates.put(chatId, new TareaCreationState(chatId));
    }

    public static TareaCreationState getState(Long chatId) {
        return creationStates.get(chatId);
    }

    public static void clearState(Long chatId) {
        creationStates.remove(chatId);
    }

    public static boolean isInCreationProcess(Long chatId) {
        return creationStates.containsKey(chatId);
    }
}
