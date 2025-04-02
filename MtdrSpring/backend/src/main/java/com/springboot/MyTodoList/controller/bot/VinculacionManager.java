package com.springboot.MyTodoList.controller.bot;

import java.util.HashMap;
import java.util.Map;

public class VinculacionManager {
    private static final Map<Long, Boolean> esperandoTelefono = new HashMap<>();

    public static void iniciarProceso(Long chatId) {
        esperandoTelefono.put(chatId, true);
    }

    public static boolean estaEsperandoTelefono(Long chatId) {
        return esperandoTelefono.getOrDefault(chatId, false);
    }

    public static void finalizarProceso(Long chatId) {
        esperandoTelefono.remove(chatId);
    }
}

