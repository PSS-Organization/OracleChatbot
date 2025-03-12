package com.springboot.MyTodoList.util;

public enum BotLabels {

    SHOW_MAIN_SCREEN("🏠 Menú Principal"),
    HIDE_MAIN_SCREEN("❌ Cerrar Menú"),
    LIST_ALL_ITEMS("📋 Ver Todas las Tareas"),
    ADD_NEW_ITEM("➕ Nueva Tarea"),
    VIEW_BY_SPRINT("🏃 Ver por Sprint"),
    VIEW_BY_STATE("📊 Ver por Estado"),
    VIEW_MY_TASKS("👤 Mis Tareas"),
    DONE("✅ Completar"),
    UNDO("↩️ Deshacer"),
    DELETE("❌ Eliminar"),
    MY_TODO_LIST("MY TODO LIST"),
    DASH("-");

    private String label;

    BotLabels(String enumLabel) {
        this.label = enumLabel;
    }

    public String getLabel() {
        return label;
    }

}
