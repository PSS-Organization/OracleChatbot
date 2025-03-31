package com.springboot.MyTodoList.controller.bot;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import com.springboot.MyTodoList.util.BotLabels;

public class MenuBotHelper {

    private static final Logger logger = LoggerFactory.getLogger(MenuBotHelper.class);

    public static void showMainMenu(Long chatId, TelegramLongPollingBot bot) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("🎯 Bienvenido al Gestor de Tareas\n\nSeleccione una opción:");

            // Crear teclado
            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
            keyboardMarkup.setResizeKeyboard(true);
            keyboardMarkup.setOneTimeKeyboard(false);

            List<KeyboardRow> keyboard = new ArrayList<>();

            // Fila 1
            KeyboardRow row1 = new KeyboardRow();
            row1.add(BotLabels.LIST_ALL_ITEMS.getLabel()); // 📋 Ver Todas las Tareas
            row1.add(BotLabels.ADD_NEW_ITEM.getLabel());   // ➕ Nueva Tarea
            keyboard.add(row1);

            // Fila 2
            KeyboardRow row2 = new KeyboardRow();
            row2.add(BotLabels.VIEW_MY_TASKS.getLabel());  // 👤 Mis Tareas
            row2.add(BotLabels.VIEW_BY_SPRINT.getLabel()); // 🏃 Ver por Sprint
            keyboard.add(row2);

            // Fila 3
            KeyboardRow row3 = new KeyboardRow();
            row3.add(BotLabels.VIEW_BY_STATE.getLabel());  // 📊 Ver por Estado
            row3.add(BotLabels.HIDE_MAIN_SCREEN.getLabel());// ❌ Cerrar Menú
            keyboard.add(row3);

            keyboardMarkup.setKeyboard(keyboard);
            message.setReplyMarkup(keyboardMarkup);

            // Enviar mensaje con menú
            bot.execute(message);

        } catch (Exception e) {
            logger.error("❌ Error al mostrar el menú principal: " + e.getMessage(), e);
        }
    }
}
