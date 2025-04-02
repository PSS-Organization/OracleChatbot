package com.springboot.MyTodoList.controller.bot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import com.springboot.MyTodoList.model.Tarea;
import com.springboot.MyTodoList.model.Usuario;
import com.springboot.MyTodoList.service.TareaService;
import com.springboot.MyTodoList.service.UsuarioService;
import com.springboot.MyTodoList.util.BotHelper;
import com.springboot.MyTodoList.util.BotLabels;

public class UsuarioBotController {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioBotController.class);
    private final TareaService tareaService;
    private final UsuarioService usuarioService;

    public UsuarioBotController(TareaService tareaService, UsuarioService usuarioService) {
        this.tareaService = tareaService;
        this.usuarioService = usuarioService;
    }

    public boolean canHandle(String messageText) {
        return messageText.equals(BotLabels.VIEW_MY_TASKS.getLabel())
                || messageText.equals("✅ Completar Tareas")
                || messageText.startsWith("✅ "); // Para manejar la selección de tareas
    }

    public void handleMessage(String messageText, Long chatId, Long telegramId, TelegramLongPollingBot bot) {
        if (messageText.equals(BotLabels.VIEW_MY_TASKS.getLabel())) {
            showUserTasks(chatId, telegramId, bot);
        } else if (messageText.equals("✅ Completar Tareas")) {
            showTaskCompletionMenu(chatId, telegramId, bot);
        } else if (messageText.startsWith("✅ ")) {
            handleTaskCompletion(messageText, chatId, telegramId, bot);
        }
    }

    private void showUserTasks(Long chatId, Long telegramId, TelegramLongPollingBot bot) {
        try {
            Optional<Usuario> usuarioOpt = usuarioService.getUsuarioByTelegramId(telegramId);
            if (usuarioOpt.isEmpty()) {
                BotHelper.sendMessageToTelegram(chatId, "❌ No se encontró tu cuenta.", bot);
                return;
            }

            Usuario usuario = usuarioOpt.get();
            List<Tarea> tareas = tareaService.getTareasByUsuario(usuario.getUsuarioID());

            if (tareas.isEmpty()) {
                BotHelper.sendMessageToTelegram(chatId, "No tienes tareas asignadas.", bot);
                MenuBotHelper.showMainMenu(chatId, bot);
                return;
            }

            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("👤 *MIS TAREAS:*\n\n");
            for (Tarea tarea : tareas) {
                messageBuilder.append("🔸 *").append(tarea.getTareaNombre()).append("*\n")
                        .append("📝 ").append(tarea.getDescripcion()).append("\n")
                        .append("📅 Entrega: ").append(tarea.getFechaEntrega()).append("\n\n");
            }

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(messageBuilder.toString());
            message.setParseMode("Markdown");

            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> keyboard = new ArrayList<>();

            KeyboardRow row1 = new KeyboardRow();
            row1.add("✅ Completar Tareas");
            keyboard.add(row1);

            KeyboardRow row2 = new KeyboardRow();
            row2.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
            keyboard.add(row2);

            keyboardMarkup.setKeyboard(keyboard);
            message.setReplyMarkup(keyboardMarkup);

            bot.execute(message);
        } catch (Exception e) {
            logger.error("Error al mostrar tareas", e);
            BotHelper.sendMessageToTelegram(chatId, "❌ Error al obtener tus tareas.", bot);
            MenuBotHelper.showMainMenu(chatId, bot);
        }
    }

    private void showTaskCompletionMenu(Long chatId, Long telegramId, TelegramLongPollingBot bot) {
        try {
            Optional<Usuario> usuarioOpt = usuarioService.getUsuarioByTelegramId(telegramId);
            if (usuarioOpt.isEmpty()) {
                BotHelper.sendMessageToTelegram(chatId, "❌ No se encontró tu cuenta.", bot);
                return;
            }

            Usuario usuario = usuarioOpt.get();
            List<Tarea> tareas = tareaService.getTareasByUsuario(usuario.getUsuarioID())
                    .stream()
                    .filter(t -> t.getCompletado() == 0)
                    .collect(Collectors.toList());

            if (tareas.isEmpty()) {
                BotHelper.sendMessageToTelegram(chatId, "No tienes tareas pendientes para completar.", bot);
                MenuBotHelper.showMainMenu(chatId, bot);
                return;
            }

            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> keyboard = new ArrayList<>();

            for (Tarea tarea : tareas) {
                KeyboardRow row = new KeyboardRow();
                row.add("✅ " + tarea.getTareaNombre());
                keyboard.add(row);
            }

            KeyboardRow lastRow = new KeyboardRow();
            lastRow.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
            keyboard.add(lastRow);

            keyboardMarkup.setKeyboard(keyboard);
            keyboardMarkup.setResizeKeyboard(true);
            keyboardMarkup.setOneTimeKeyboard(true);

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("Selecciona la tarea que deseas marcar como completada:");
            message.setReplyMarkup(keyboardMarkup);

            bot.execute(message);
        } catch (Exception e) {
            logger.error("Error al mostrar menú de completar tareas", e);
            BotHelper.sendMessageToTelegram(chatId, "❌ Error al cargar tus tareas.", bot);
            MenuBotHelper.showMainMenu(chatId, bot);
        }
    }

    private void handleTaskCompletion(String messageText, Long chatId, Long telegramId, TelegramLongPollingBot bot) {
        try {
            // Obtener el nombre de la tarea seleccionada (removiendo el emoji)
            String taskName = messageText.substring(2).trim();

            Optional<Usuario> usuarioOpt = usuarioService.getUsuarioByTelegramId(telegramId);
            if (usuarioOpt.isEmpty()) {
                BotHelper.sendMessageToTelegram(chatId, "❌ No se encontró tu cuenta.", bot);
                return;
            }

            Usuario usuario = usuarioOpt.get();
            List<Tarea> tareas = tareaService.getTareasByUsuario(usuario.getUsuarioID());

            // Buscar la tarea por nombre
            Optional<Tarea> tareaOpt = tareas.stream()
                    .filter(t -> t.getTareaNombre().equals(taskName))
                    .findFirst();

            if (tareaOpt.isEmpty()) {
                BotHelper.sendMessageToTelegram(chatId, "❌ No se encontró la tarea.", bot);
                return;
            }

            // Actualizar el estado de la tarea
            Tarea tarea = tareaOpt.get();
            tarea.setCompletado(1);
            tarea.setEstadoID(3L); // ID del estado "Completado"
            tareaService.updateTarea(tarea.getTareaID(), tarea);

            BotHelper.sendMessageToTelegram(chatId,
                    "✅ ¡Tarea completada exitosamente!\n"
                    + "🔸 " + tarea.getTareaNombre(), bot);

            // Volver al menú principal
            MenuBotHelper.showMainMenu(chatId, bot);

        } catch (Exception e) {
            logger.error("Error al completar tarea", e);
            BotHelper.sendMessageToTelegram(chatId, "❌ Error al completar la tarea.", bot);
            MenuBotHelper.showMainMenu(chatId, bot);
        }
    }
}
