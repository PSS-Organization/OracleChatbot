package com.springboot.MyTodoList.controller.bot;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.model.Tarea;
import com.springboot.MyTodoList.service.SprintService;
import com.springboot.MyTodoList.service.TareaService;
import com.springboot.MyTodoList.util.BotHelper;
import com.springboot.MyTodoList.util.BotLabels;

public class SprintBotController {

    private static final Logger logger = LoggerFactory.getLogger(SprintBotController.class);

    private final SprintService sprintService;
    private final TareaService tareaService;

    public SprintBotController(SprintService sprintService, TareaService tareaService) {
        this.sprintService = sprintService;
        this.tareaService = tareaService;
    }

    public boolean canHandle(String messageText) {
        return messageText.equals(BotLabels.VIEW_BY_SPRINT.getLabel()) || messageText.startsWith("Sprint ");
    }

    public void handleMessage(String messageText, Long chatId, TelegramLongPollingBot bot) {
        if (messageText.equals(BotLabels.VIEW_BY_SPRINT.getLabel())) {
            mostrarListaSprints(chatId, bot);
        } else if (messageText.startsWith("Sprint ")) {
            try {
                Long sprintId = Long.parseLong(messageText.split(" ")[1]);
                mostrarTareasPorSprint(sprintId, chatId, bot);
            } catch (NumberFormatException e) {
                BotHelper.sendMessageToTelegram(chatId, "❌ Sprint inválido.", bot);
            }
        }
    }

    private void mostrarListaSprints(Long chatId, TelegramLongPollingBot bot) {
        List<Sprint> sprints = sprintService.getAllSprints();

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("🏃 *Seleccione un Sprint:* (Ej. Sprint 1)");
        message.setParseMode("Markdown");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow menuRow = new KeyboardRow();
        menuRow.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
        keyboard.add(menuRow);

        for (Sprint sprint : sprints) {
            KeyboardRow row = new KeyboardRow();
            row.add("Sprint " + sprint.getSprintID());
            keyboard.add(row);
        }

        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        try {
            bot.execute(message);
        } catch (Exception e) {
            logger.error("Error al mostrar lista de sprints", e);
        }
    }

    private void mostrarTareasPorSprint(Long sprintId, Long chatId, TelegramLongPollingBot bot) {
        List<Tarea> tareas = tareaService.getTareasBySprint(sprintId);

        if (tareas.isEmpty()) {
            BotHelper.sendMessageToTelegram(chatId, "No hay tareas en este sprint.", bot);
            return;
        }

        StringBuilder messageText = new StringBuilder();
        messageText.append("🏃 *TAREAS EN SPRINT " + sprintId + "*\n\n");

        for (Tarea tarea : tareas) {
            messageText.append("🔸 *").append(tarea.getTareaNombre()).append("*\n")
                    .append("📝 ").append(tarea.getDescripcion()).append("\n")
                    .append("📅 Fecha entrega: ").append(tarea.getFechaEntrega()).append("\n")
                    .append("⏱️ Horas estimadas: ").append(tarea.getHorasEstimadas()).append("\n\n");
        }

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText.toString());
        message.setParseMode("Markdown");

        try {
            bot.execute(message);
        } catch (Exception e) {
            logger.error("Error al mostrar tareas por sprint", e);
        }
    }
}
