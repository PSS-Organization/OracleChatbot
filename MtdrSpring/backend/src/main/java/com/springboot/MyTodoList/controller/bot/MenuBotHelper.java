package com.springboot.MyTodoList.controller.bot;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import com.springboot.MyTodoList.util.BotLabels;

public class MenuBotHelper {

    private static final Logger logger = LoggerFactory.getLogger(MenuBotHelper.class);

    public static void showMainMenu(Long chatId, TelegramLongPollingBot bot) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("🎯 Bienvenido al Gestor de Tareas\n\nSeleccione una opción:");

            // Mostrar menú con botones inline (aparecen debajo del mensaje)
            message.setReplyMarkup(createInlineKeyboard());

            // Enviar mensaje con menú
            bot.execute(message);

        } catch (Exception e) {
            logger.error("❌ Error al mostrar el menú principal: " + e.getMessage(), e);
        }
    }

    // Método anterior con teclado de respuesta (mantener por compatibilidad)
    public static void showMainMenuWithReplyKeyboard(Long chatId, TelegramLongPollingBot bot) {
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
            row1.add("👥 Ver Tareas");
            row1.add(BotLabels.ADD_NEW_ITEM.getLabel()); // ➕ Nueva Tarea
            keyboard.add(row1);

            // Fila 2
            KeyboardRow row2 = new KeyboardRow();
            row2.add(BotLabels.VIEW_MY_TASKS.getLabel()); // 👤 Mis Tareas
            row2.add(BotLabels.VIEW_BY_SPRINT.getLabel()); // 🏃 Ver por Sprint
            keyboard.add(row2);

            // Fila 3
            KeyboardRow row3 = new KeyboardRow();
            row3.add(BotLabels.VIEW_BY_STATE.getLabel()); // 📊 Ver por Estado
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

    // Crear un teclado inline (botones debajo del mensaje)
    private static InlineKeyboardMarkup createInlineKeyboard() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        // Fila 1
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("➕ Nueva Tarea");
        button2.setCallbackData("NUEVA_TAREA");

        InlineKeyboardButton button6 = new InlineKeyboardButton();
        button6.setText("👥 Ver Tareas");
        button6.setCallbackData("VER_TAREAS");

        row1.add(button6);
        row1.add(button2);
        rowsInline.add(row1);

        // Fila 2
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText("👤 Mis Tareas");
        button3.setCallbackData("MIS_TAREAS");

        InlineKeyboardButton button4 = new InlineKeyboardButton();
        button4.setText("🏃 Ver por Sprint");
        button4.setCallbackData("VER_POR_SPRINT");

        row2.add(button3);
        row2.add(button4);
        rowsInline.add(row2);

        // Fila 3
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        InlineKeyboardButton button5 = new InlineKeyboardButton();
        button5.setText("📊 KPIs");
        button5.setCallbackData("VER_KPIS");

        row3.add(button5);
        rowsInline.add(row3);

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
}
