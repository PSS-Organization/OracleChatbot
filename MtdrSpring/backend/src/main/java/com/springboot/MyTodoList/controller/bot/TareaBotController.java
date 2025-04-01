package com.springboot.MyTodoList.controller.bot;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.model.Tarea;
import com.springboot.MyTodoList.model.TareaCreationState;
import com.springboot.MyTodoList.model.Usuario;
import com.springboot.MyTodoList.service.SprintService;
import com.springboot.MyTodoList.service.TareaService;
import com.springboot.MyTodoList.service.UsuarioService;
import com.springboot.MyTodoList.util.BotHelper;
import com.springboot.MyTodoList.util.BotLabels;
import com.springboot.MyTodoList.util.TareaCreationManager;

public class TareaBotController {

    private static final Logger logger = LoggerFactory.getLogger(TareaBotController.class);
    private final TareaService tareaService;
    private final UsuarioService usuarioService;
    private final SprintService sprintService;

    private static Map<Long, Tarea> completionTasks = new HashMap<>();
    private static Map<Long, Boolean> awaitingHoursReal = new HashMap<>();

    public TareaBotController(TareaService tareaService, UsuarioService usuarioService, SprintService sprintService) {
        this.tareaService = tareaService;
        this.usuarioService = usuarioService;
        this.sprintService = sprintService;
    }

    public boolean canHandle(String messageText, Long chatId) {
        return messageText.equals(BotLabels.LIST_ALL_ITEMS.getLabel())
                || messageText.equals(BotLabels.ADD_NEW_ITEM.getLabel())
                || messageText.equals("👤 Mis Tareas")
                || messageText.equals("✅ Completar Tareas")
                || messageText.equals("📋 Tareas Completadas")
                || messageText.equals("📋 Ver Todas las Tareas")
                || messageText.startsWith("✅ ")
                || TareaCreationManager.isInCreationProcess(chatId);
    }

    public void handleMessage(String messageText, Long chatId, Long telegramId, TelegramLongPollingBot bot) {
        if (awaitingHoursReal.containsKey(chatId)) {
            try {
                int horasReales = Integer.parseInt(messageText);
                Tarea tarea = completionTasks.get(chatId);
                tarea.setHorasReales(horasReales);
                tarea.setCompletado(1);
                tarea.setEstadoID(3L);
                tareaService.updateTarea(tarea.getTareaID(), tarea);

                BotHelper.sendMessageToTelegram(chatId,
                        "✅ ¡Tarea completada exitosamente!\n"
                        + "🔸 " + tarea.getTareaNombre() + "\n"
                        + "⏱️ Horas reales: " + horasReales, bot);

                completionTasks.remove(chatId);
                awaitingHoursReal.remove(chatId);
                MenuBotHelper.showMainMenu(chatId, bot);
                return;
            } catch (NumberFormatException e) {
                BotHelper.sendMessageToTelegram(chatId, "❌ Por favor ingresa un número válido.", bot);
                return;
            }
        }

        // Existing task creation logic
        if (TareaCreationManager.isInCreationProcess(chatId)) {
            handleTareaCreation(messageText, chatId, bot);
            return;
        }

        switch (messageText) {
            case "📋 Ver Todas las Tareas":
                mostrarTodasLasTareas(chatId, bot);
                break;
            case "➕ Nueva Tarea":
                solicitarNombreTarea(chatId, bot);
                break;
            case "👤 Mis Tareas":
                showUserTasks(chatId, telegramId, bot);
                break;
            case "✅ Completar Tareas":
                showTaskCompletionMenu(chatId, telegramId, bot);
                break;
            case "📋 Tareas Completadas":
                showCompletedTasks(chatId, telegramId, bot);
                break;
            default:
                if (messageText.startsWith("✅ ")) {
                    handleTaskCompletion(messageText, chatId, telegramId, bot);
                }
                break;
        }
    }

    public void handleFallback(String messageText, Long chatId, TelegramLongPollingBot bot) {
        if (TareaCreationManager.isInCreationProcess(chatId)) {
            handleTareaCreation(messageText, chatId, bot);
        } else if (awaitingHoursReal.getOrDefault(chatId, false)) {
            handleRealHoursInput(messageText, chatId, bot);
        } else {
            BotHelper.sendMessageToTelegram(chatId, "⚠️ No entendí ese mensaje. Usa /start para volver al menú.", bot);
        }
    }

    private void mostrarTodasLasTareas(Long chatId, TelegramLongPollingBot bot) {
        List<Tarea> tareas = tareaService.getAllTareas();

        if (tareas.isEmpty()) {
            BotHelper.sendMessageToTelegram(chatId, "No hay tareas registradas.", bot);
            return;
        }

        StringBuilder messageText = new StringBuilder("📋 *Lista de Tareas:*\n\n");
        for (Tarea tarea : tareas) {
            messageText.append("🔹 ").append(tarea.getTareaNombre()).append("\n")
                    .append("📄 ").append(tarea.getDescripcion()).append("\n\n");
        }

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText.toString());
        message.setParseMode("Markdown");

        try {
            bot.execute(message);
        } catch (Exception e) {
            logger.error("Error mostrando lista de tareas", e);
        }
    }

    private void solicitarNombreTarea(Long chatId, TelegramLongPollingBot bot) {
        TareaCreationManager.startCreation(chatId);
        BotHelper.sendMessageToTelegram(chatId, "✏️ Ingresa el *nombre* de la nueva tarea:", bot);
    }

    private void mostrarOpcionesUsuarios(Long chatId, TelegramLongPollingBot bot) {
        List<Usuario> usuarios = usuarioService.getAllUsuarios();
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        for (Usuario usuario : usuarios) {
            KeyboardRow row = new KeyboardRow();
            row.add(usuario.getUsuarioID() + " - " + usuario.getNombre());
            keyboard.add(row);
        }

        keyboardMarkup.setKeyboard(keyboard);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("👤 Selecciona un *usuario* para asignar la tarea:");
        message.setParseMode("Markdown");
        message.setReplyMarkup(keyboardMarkup);

        try {
            bot.execute(message);
        } catch (Exception e) {
            logger.error("Error mostrando usuarios", e);
        }
    }

    private void mostrarOpcionesSprints(Long chatId, TelegramLongPollingBot bot) {
        List<Sprint> sprints = sprintService.getAllSprints();
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        for (Sprint sprint : sprints) {
            KeyboardRow row = new KeyboardRow();
            row.add(sprint.getSprintID() + " - " + sprint.getNombreSprint());
            keyboard.add(row);
        }

        keyboardMarkup.setKeyboard(keyboard);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("🏃 Selecciona un *sprint* para esta tarea:");
        message.setParseMode("Markdown");
        message.setReplyMarkup(keyboardMarkup);

        try {
            bot.execute(message);
        } catch (Exception e) {
            logger.error("Error mostrando sprints", e);
        }
    }

    private void handleTareaCreation(String messageText, Long chatId, TelegramLongPollingBot bot) {
        TareaCreationState state = TareaCreationManager.getState(chatId);

        switch (state.getCurrentField()) {
            case "NOMBRE":
                state.getTarea().setTareaNombre(messageText);
                state.setCurrentField("DESCRIPCION");
                BotHelper.sendMessageToTelegram(chatId, "📝 Ingresa una *descripción* para la tarea:", bot);
                break;

            case "DESCRIPCION":
                state.getTarea().setDescripcion(messageText);
                state.setCurrentField("PRIORIDAD");
                BotHelper.sendMessageToTelegram(chatId, "📌 Especifica la *prioridad* (BAJA, MEDIA, ALTA):", bot);
                break;

            case "PRIORIDAD":
                if (!Pattern.matches("(?i)BAJA|MEDIA|ALTA", messageText)) {
                    BotHelper.sendMessageToTelegram(chatId, "❌ Prioridad inválida. Usa BAJA, MEDIA o ALTA.", bot);
                    return;
                }
                state.getTarea().setPrioridad(messageText.toUpperCase());
                state.setCurrentField("USUARIO");
                mostrarOpcionesUsuarios(chatId, bot);
                break;

            case "USUARIO":
                if (!messageText.contains("-")) {
                    BotHelper.sendMessageToTelegram(chatId, "❌ Selecciona un usuario válido de la lista.", bot);
                    return;
                }
                try {
                    Long userId = Long.parseLong(messageText.split("-")[0].trim());
                    state.getTarea().setUsuarioID(userId);
                    state.setCurrentField("SPRINT");
                    mostrarOpcionesSprints(chatId, bot);
                } catch (NumberFormatException e) {
                    BotHelper.sendMessageToTelegram(chatId, "❌ Formato inválido. Selecciona desde el menú.", bot);
                }
                break;

            case "SPRINT":
                if (!messageText.contains("-")) {
                    BotHelper.sendMessageToTelegram(chatId, "❌ Selecciona un sprint válido de la lista.", bot);
                    return;
                }
                try {
                    Long sprintId = Long.parseLong(messageText.split("-")[0].trim());
                    state.getTarea().setSprintID(sprintId);
                    state.setCurrentField("HORAS");
                    BotHelper.sendMessageToTelegram(chatId, "⏱️ ¿Cuántas *horas estimadas* tomará esta tarea?", bot);
                } catch (NumberFormatException e) {
                    BotHelper.sendMessageToTelegram(chatId, "❌ Ingresa un ID de sprint válido.", bot);
                }
                break;

            case "HORAS":
                try {
                    int horas = Integer.parseInt(messageText.trim());
                    state.getTarea().setHorasEstimadas(horas);
                    state.setCurrentField("FECHA");
                    BotHelper.sendMessageToTelegram(chatId, "📅 Ingresa la *fecha de entrega* (formato: DD/MM/YYYY):", bot);
                } catch (NumberFormatException e) {
                    BotHelper.sendMessageToTelegram(chatId, "❌ Ingresa un número válido de horas.", bot);
                }
                break;

            case "FECHA":
                try {
                    LocalDate fecha = LocalDate.parse(messageText.trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    state.getTarea().setFechaEntrega(fecha.atStartOfDay().atOffset(ZoneOffset.UTC));
                    tareaService.createTarea(state.getTarea());
                    TareaCreationManager.clearState(chatId);
                    BotHelper.sendMessageToTelegram(chatId, "✅ Tarea creada exitosamente.", bot);
                } catch (Exception e) {
                    BotHelper.sendMessageToTelegram(chatId, "❌ Formato inválido. Usa DD/MM/YYYY", bot);
                }
                break;
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
            List<Tarea> tareas = tareaService.getTareasByUsuario(usuario.getUsuarioID())
                    .stream()
                    .filter(t -> t.getCompletado() == 0) // Only show incomplete tasks
                    .toList();

            if (tareas.isEmpty()) {
                BotHelper.sendMessageToTelegram(chatId, "No tienes tareas asignadas.", bot);
                MenuBotHelper.showMainMenu(chatId, bot);
                return;
            }

            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("👤 *MIS TAREAS:*\n\n");
            for (Tarea tarea : tareas) {
                String estado = tarea.getCompletado() == 1 ? "✅" : "⏳";
                messageBuilder.append(estado).append(" *").append(tarea.getTareaNombre()).append("*\n")
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
            keyboardMarkup.setResizeKeyboard(true);
            message.setReplyMarkup(keyboardMarkup);

            bot.execute(message);
        } catch (Exception e) {
            logger.error("Error al mostrar tareas del usuario", e);
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
            List<Tarea> tareasPendientes = tareaService.getTareasByUsuario(usuario.getUsuarioID())
                    .stream()
                    .filter(t -> t.getCompletado() == 0)
                    .toList();

            if (tareasPendientes.isEmpty()) {
                BotHelper.sendMessageToTelegram(chatId, "No tienes tareas pendientes para completar.", bot);
                MenuBotHelper.showMainMenu(chatId, bot);
                return;
            }

            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> keyboard = new ArrayList<>();

            for (Tarea tarea : tareasPendientes) {
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
            // Remove the "✅ " prefix from the incoming message
            String taskName = messageText.substring(2);  // Remove first 2 characters ("✅ ")

            Optional<Usuario> usuarioOpt = usuarioService.getUsuarioByTelegramId(telegramId);
            if (usuarioOpt.isEmpty()) {
                BotHelper.sendMessageToTelegram(chatId, "❌ No se encontró tu cuenta.", bot);
                return;
            }

            Usuario usuario = usuarioOpt.get();
            List<Tarea> tareas = tareaService.getTareasByUsuario(usuario.getUsuarioID());

            Optional<Tarea> tareaOpt = tareas.stream()
                    .filter(t -> t.getTareaNombre().equals(taskName) && t.getCompletado() == 0)
                    .findFirst();

            if (tareaOpt.isEmpty()) {
                BotHelper.sendMessageToTelegram(chatId, "❌ No se encontró la tarea o ya está completada.", bot);
                MenuBotHelper.showMainMenu(chatId, bot);
                return;
            }

            Tarea tarea = tareaOpt.get();
            BotHelper.sendMessageToTelegram(chatId,
                    "⏱️ Por favor, ingresa las horas reales que tomó completar la tarea:", bot);

            completionTasks.put(chatId, tarea);
            awaitingHoursReal.put(chatId, true);

        } catch (Exception e) {
            logger.error("Error al completar tarea", e);
            BotHelper.sendMessageToTelegram(chatId, "❌ Error al completar la tarea.", bot);
            MenuBotHelper.showMainMenu(chatId, bot);
        }
    }

    private void handleRealHoursInput(String messageText, Long chatId, TelegramLongPollingBot bot) {
        try {
            int horasReales = Integer.parseInt(messageText.trim());
            Tarea tarea = completionTasks.get(chatId);

            if (tarea == null) {
                BotHelper.sendMessageToTelegram(chatId, "❌ No se encontró la tarea para completar.", bot);
                awaitingHoursReal.put(chatId, false);
                MenuBotHelper.showMainMenu(chatId, bot);
                return;
            }

            tarea.setHorasReales(horasReales);
            tarea.setCompletado(1);
            tarea.setEstadoID(3L);
            tareaService.updateTarea(tarea.getTareaID(), tarea);

            BotHelper.sendMessageToTelegram(chatId,
                    "✅ ¡Tarea completada exitosamente!\n"
                    + "🔸 " + tarea.getTareaNombre(), bot);

            completionTasks.remove(chatId);
            awaitingHoursReal.put(chatId, false);
            MenuBotHelper.showMainMenu(chatId, bot);

        } catch (NumberFormatException e) {
            BotHelper.sendMessageToTelegram(chatId, "❌ Ingresa un número válido de horas.", bot);
        } catch (Exception e) {
            logger.error("Error al completar tarea con horas reales", e);
            BotHelper.sendMessageToTelegram(chatId, "❌ Error al completar la tarea.", bot);
            MenuBotHelper.showMainMenu(chatId, bot);
        }
    }

    private void showCompletedTasks(Long chatId, Long telegramId, TelegramLongPollingBot bot) {
        try {
            Optional<Usuario> usuarioOpt = usuarioService.getUsuarioByTelegramId(telegramId);
            if (usuarioOpt.isEmpty()) {
                BotHelper.sendMessageToTelegram(chatId, "❌ No se encontró tu cuenta.", bot);
                return;
            }

            Usuario usuario = usuarioOpt.get();
            List<Tarea> tareasCompletadas = tareaService.getTareasByUsuario(usuario.getUsuarioID())
                    .stream()
                    .filter(t -> t.getCompletado() == 1)
                    .toList();

            if (tareasCompletadas.isEmpty()) {
                BotHelper.sendMessageToTelegram(chatId, "No tienes tareas completadas.", bot);
                MenuBotHelper.showMainMenu(chatId, bot);
                return;
            }

            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("📋 *TAREAS COMPLETADAS:*\n\n");

            for (Tarea tarea : tareasCompletadas) {
                messageBuilder.append("🔸 ").append(tarea.getTareaNombre()).append("\n")
                        .append("📝 ").append(tarea.getDescripcion()).append("\n")
                        .append("📅 Fecha entrega: ").append(tarea.getFechaEntrega()).append("\n")
                        .append("⏱️ Horas estimadas: ").append(tarea.getHorasEstimadas()).append("\n")
                        .append("⏱️ Horas reales: ").append(tarea.getHorasReales()).append("\n")
                        .append("Estado: COMPLETADA\n\n");
            }

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(messageBuilder.toString());
            message.setParseMode("Markdown");

            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> keyboard = new ArrayList<>();

            KeyboardRow row = new KeyboardRow();
            row.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
            keyboard.add(row);

            keyboardMarkup.setKeyboard(keyboard);
            keyboardMarkup.setResizeKeyboard(true);
            message.setReplyMarkup(keyboardMarkup);

            bot.execute(message);
        } catch (Exception e) {
            logger.error("Error al mostrar tareas completadas", e);
            BotHelper.sendMessageToTelegram(chatId, "❌ Error al obtener tus tareas completadas.", bot);
            MenuBotHelper.showMainMenu(chatId, bot);
        }
    }
}
