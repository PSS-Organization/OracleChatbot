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
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

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
                || messageText.equals("📋 Historial Completadas")
                || messageText.equals("📊 Ver por Estado")
                || messageText.startsWith("ESTADO_") // For state filtering
                || messageText.equals("📋 Ver Todas las Tareas")
                || messageText.startsWith("✅ ")
                || messageText.equals("COMPLETAR_TAREAS")
                || messageText.equals("HISTORIAL_COMPLETADAS")
                || messageText.equals("VER_POR_ESTADO")
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
                                + "⏱️ Horas reales: " + horasReales,
                        bot);

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
            case "📋 Historial Completadas":
                showCompletedTasks(chatId, telegramId, bot);
                break;
            case "📊 Ver por Estado":
                showStateFilterMenu(chatId, telegramId, bot);
                break;
            default:
                if (messageText.startsWith("✅ ")) {
                    handleTaskCompletion(messageText, chatId, telegramId, bot);
                } else if (messageText.startsWith("ESTADO_")) {
                    showTasksByState(messageText.substring(7), chatId, telegramId, bot);
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

        // Crear teclado inline
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        // Agregar cada usuario como un botón
        for (Usuario usuario : usuarios) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(usuario.getNombre());
            button.setCallbackData("USUARIO_" + usuario.getUsuarioID());
            row.add(button);
            rowsInline.add(row);
        }

        markupInline.setKeyboard(rowsInline);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("👤 Selecciona un *usuario* para asignar la tarea:");
        message.setParseMode("Markdown");
        message.setReplyMarkup(markupInline);

        try {
            bot.execute(message);
        } catch (Exception e) {
            logger.error("Error mostrando usuarios", e);
        }
    }

    private void mostrarOpcionesSprints(Long chatId, TelegramLongPollingBot bot) {
        List<Sprint> sprints = sprintService.getAllSprints();

        // Crear teclado inline
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        // Agregar cada sprint como un botón
        for (Sprint sprint : sprints) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(sprint.getNombreSprint());
            button.setCallbackData("SPRINT_" + sprint.getSprintID());
            row.add(button);
            rowsInline.add(row);
        }

        markupInline.setKeyboard(rowsInline);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("🏃 Selecciona un *sprint* para esta tarea:");
        message.setParseMode("Markdown");
        message.setReplyMarkup(markupInline);

        try {
            bot.execute(message);
        } catch (Exception e) {
            logger.error("Error mostrando sprints", e);
        }
    }

    private void mostrarOpcionesHoras(Long chatId, TelegramLongPollingBot bot) {
        // Crear teclado inline
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        // Primera fila: 1 y 2 horas
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("1 hora");
        button1.setCallbackData("HORAS_1");

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("2 horas");
        button2.setCallbackData("HORAS_2");

        row1.add(button1);
        row1.add(button2);
        rowsInline.add(row1);

        // Segunda fila: 3 y 4 horas
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText("3 horas");
        button3.setCallbackData("HORAS_3");

        InlineKeyboardButton button4 = new InlineKeyboardButton();
        button4.setText("4 horas");
        button4.setCallbackData("HORAS_4");

        row2.add(button3);
        row2.add(button4);
        rowsInline.add(row2);

        markupInline.setKeyboard(rowsInline);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("⏱️ *¿Cuántas horas estimadas tomará esta tarea?*\n\nSelecciona una opción (máximo 4 horas):");
        message.setParseMode("Markdown");
        message.setReplyMarkup(markupInline);

        try {
            bot.execute(message);
        } catch (Exception e) {
            logger.error("Error mostrando opciones de horas", e);
        }
    }

    private void mostrarOpcionesFecha(Long chatId, TelegramLongPollingBot bot) {
        // Obtenemos la fecha actual
        LocalDate today = LocalDate.now();

        // Crear teclado inline
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        // Fila 1: Próximos 3 días
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            LocalDate date = today.plusDays(i);
            String formattedDate = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(formattedDate + " (" + getDayName(date) + ")");
            button.setCallbackData("FECHA_" + formattedDate);
            row1.add(button);
        }
        rowsInline.add(row1);

        // Fila 2: Próximos 3 días
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        for (int i = 4; i <= 6; i++) {
            LocalDate date = today.plusDays(i);
            String formattedDate = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(formattedDate + " (" + getDayName(date) + ")");
            button.setCallbackData("FECHA_" + formattedDate);
            row2.add(button);
        }
        rowsInline.add(row2);

        // Fila 3: Próxima semana
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        LocalDate nextWeek = today.plusWeeks(1);
        String formattedNextWeek = nextWeek.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        InlineKeyboardButton nextWeekButton = new InlineKeyboardButton();
        nextWeekButton.setText("Próxima semana (" + formattedNextWeek + ")");
        nextWeekButton.setCallbackData("FECHA_" + formattedNextWeek);
        row3.add(nextWeekButton);
        rowsInline.add(row3);

        // Fila 4: Ingresar fecha manualmente
        List<InlineKeyboardButton> row4 = new ArrayList<>();
        InlineKeyboardButton manualButton = new InlineKeyboardButton();
        manualButton.setText("Ingresar fecha manualmente");
        manualButton.setCallbackData("FECHA_MANUAL");
        row4.add(manualButton);
        rowsInline.add(row4);

        markupInline.setKeyboard(rowsInline);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("📅 *Selecciona la fecha de entrega:*");
        message.setParseMode("Markdown");
        message.setReplyMarkup(markupInline);

        try {
            bot.execute(message);
        } catch (Exception e) {
            logger.error("Error mostrando opciones de fecha", e);
        }
    }

    private String getDayName(LocalDate date) {
        String[] days = { "Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado" };
        return days[date.getDayOfWeek().getValue() % 7];
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
                // This is only used for manual input, now handled by callbacks
                try {
                    Long userId = Long.parseLong(messageText.trim());
                    state.getTarea().setUsuarioID(userId);
                    state.setCurrentField("SPRINT");
                    mostrarOpcionesSprints(chatId, bot);
                } catch (NumberFormatException e) {
                    BotHelper.sendMessageToTelegram(chatId, "❌ Formato inválido. Selecciona desde el menú.", bot);
                }
                break;

            case "SPRINT":
                // This is only used for manual input, now handled by callbacks
                try {
                    Long sprintId = Long.parseLong(messageText.trim());
                    state.getTarea().setSprintID(sprintId);
                    state.setCurrentField("HORAS");
                    mostrarOpcionesHoras(chatId, bot);
                } catch (NumberFormatException e) {
                    BotHelper.sendMessageToTelegram(chatId, "❌ Ingresa un ID de sprint válido.", bot);
                }
                break;

            case "HORAS":
                // This is only used for manual input, now handled by callbacks
                try {
                    int horas = Integer.parseInt(messageText.trim());

                    if (horas > 4) {
                        BotHelper.sendMessageToTelegram(chatId,
                                "⚠️ Las tareas no pueden tener más de 4 horas estimadas.\n" +
                                        "Por favor, ingresa un número menor o igual a 4.",
                                bot);
                        return; // 👈 no avanza, vuelve a pedir las horas
                    }

                    state.getTarea().setHorasEstimadas(horas);
                    state.setCurrentField("FECHA");
                    mostrarOpcionesFecha(chatId, bot);
                } catch (NumberFormatException e) {
                    BotHelper.sendMessageToTelegram(chatId,
                            "❌ Ingresa un número válido de horas.", bot);
                }
                break;

            case "FECHA":
                // This is for manual date input
                try {
                    LocalDate fecha = LocalDate.parse(messageText.trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    state.getTarea().setFechaEntrega(fecha.atStartOfDay().atOffset(ZoneOffset.UTC));
                    state.getTarea().setCompletado(0); // Set initial completion status
                    state.getTarea().setEstadoID(1L); // Set initial state ID

                    // Create the task
                    tareaService.createTarea(state.getTarea());

                    // Clear the creation state
                    TareaCreationManager.clearState(chatId);

                    // Show success message
                    BotHelper.sendMessageToTelegram(chatId,
                            "✅ ¡Tarea creada exitosamente!\n"
                                    + "🔸 " + state.getTarea().getTareaNombre() + "\n"
                                    + "📅 Fecha entrega: " + fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            bot);

                    // Return to main menu
                    MenuBotHelper.showMainMenu(chatId, bot);
                } catch (Exception e) {
                    BotHelper.sendMessageToTelegram(chatId, "❌ Formato de fecha inválido. Usa DD/MM/YYYY", bot);
                }
                break;

            case "FECHA_MANUAL":
                // Special case for manual date entry
                try {
                    LocalDate fecha = LocalDate.parse(messageText.trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    state.getTarea().setFechaEntrega(fecha.atStartOfDay().atOffset(ZoneOffset.UTC));
                    state.getTarea().setCompletado(0);
                    state.getTarea().setEstadoID(1L);

                    // Create the task
                    tareaService.createTarea(state.getTarea());

                    // Clear the creation state
                    TareaCreationManager.clearState(chatId);

                    // Show success message
                    BotHelper.sendMessageToTelegram(chatId,
                            "✅ ¡Tarea creada exitosamente!\n"
                                    + "🔸 " + state.getTarea().getTareaNombre() + "\n"
                                    + "📅 Fecha entrega: " + fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            bot);

                    // Return to main menu
                    MenuBotHelper.showMainMenu(chatId, bot);
                } catch (Exception e) {
                    BotHelper.sendMessageToTelegram(chatId, "❌ Formato de fecha inválido. Usa DD/MM/YYYY", bot);
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
                    .collect(Collectors.toList());

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

            // Crear teclado inline
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

            // Fila 1
            List<InlineKeyboardButton> row1 = new ArrayList<>();
            InlineKeyboardButton completarButton = new InlineKeyboardButton();
            completarButton.setText("✅ Completar Tareas");
            completarButton.setCallbackData("COMPLETAR_TAREAS");
            row1.add(completarButton);
            rowsInline.add(row1);

            // Fila 2
            List<InlineKeyboardButton> row2 = new ArrayList<>();
            InlineKeyboardButton historialButton = new InlineKeyboardButton();
            historialButton.setText("📋 Historial Completadas");
            historialButton.setCallbackData("HISTORIAL_COMPLETADAS");

            InlineKeyboardButton estadoButton = new InlineKeyboardButton();
            estadoButton.setText("📊 Ver por Estado");
            estadoButton.setCallbackData("VER_POR_ESTADO");

            row2.add(historialButton);
            row2.add(estadoButton);
            rowsInline.add(row2);

            // Fila 3
            List<InlineKeyboardButton> row3 = new ArrayList<>();
            InlineKeyboardButton menuButton = new InlineKeyboardButton();
            menuButton.setText(BotLabels.SHOW_MAIN_SCREEN.getLabel());
            menuButton.setCallbackData("MENU_PRINCIPAL");
            row3.add(menuButton);
            rowsInline.add(row3);

            markupInline.setKeyboard(rowsInline);
            message.setReplyMarkup(markupInline);

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
                    .collect(Collectors.toList());

            if (tareasPendientes.isEmpty()) {
                BotHelper.sendMessageToTelegram(chatId, "No tienes tareas pendientes para completar.", bot);
                MenuBotHelper.showMainMenu(chatId, bot);
                return;
            }

            // Crear teclado inline
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

            // Agregar cada tarea como un botón
            for (Tarea tarea : tareasPendientes) {
                List<InlineKeyboardButton> row = new ArrayList<>();
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText("✅ " + tarea.getTareaNombre());
                button.setCallbackData("COMPLETAR_TAREA_" + tarea.getTareaID());
                row.add(button);
                rowsInline.add(row);
            }

            // Botón para volver al menú principal
            List<InlineKeyboardButton> lastRow = new ArrayList<>();
            InlineKeyboardButton menuButton = new InlineKeyboardButton();
            menuButton.setText(BotLabels.SHOW_MAIN_SCREEN.getLabel());
            menuButton.setCallbackData("MENU_PRINCIPAL");
            lastRow.add(menuButton);
            rowsInline.add(lastRow);

            markupInline.setKeyboard(rowsInline);

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("Selecciona la tarea que deseas marcar como completada:");
            message.setReplyMarkup(markupInline);

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
            String taskName = messageText.substring(2); // Remove first 2 characters ("✅ ")

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
                            + "🔸 " + tarea.getTareaNombre(),
                    bot);

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
                    .collect(Collectors.toList());

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

            // Crear teclado inline
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton menuButton = new InlineKeyboardButton();
            menuButton.setText(BotLabels.SHOW_MAIN_SCREEN.getLabel());
            menuButton.setCallbackData("MENU_PRINCIPAL");
            row.add(menuButton);
            rowsInline.add(row);

            markupInline.setKeyboard(rowsInline);
            message.setReplyMarkup(markupInline);

            bot.execute(message);
        } catch (Exception e) {
            logger.error("Error al mostrar tareas completadas", e);
            BotHelper.sendMessageToTelegram(chatId, "❌ Error al obtener tus tareas completadas.", bot);
            MenuBotHelper.showMainMenu(chatId, bot);
        }
    }

    private void showStateFilterMenu(Long chatId, Long telegramId, TelegramLongPollingBot bot) {
        try {
            // Crear teclado inline
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

            // Agregar opciones de estado
            String[] estados = { "PENDIENTE", "EN_PROCESO", "COMPLETADA" };
            for (String estado : estados) {
                List<InlineKeyboardButton> row = new ArrayList<>();
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(estado.replace("_", " "));
                button.setCallbackData("ESTADO_" + estado);
                row.add(button);
                rowsInline.add(row);
            }

            // Agregar opciones para volver
            List<InlineKeyboardButton> lastRow = new ArrayList<>();

            InlineKeyboardButton misTasksButton = new InlineKeyboardButton();
            misTasksButton.setText("👤 Mis Tareas");
            misTasksButton.setCallbackData("MIS_TAREAS");

            InlineKeyboardButton menuButton = new InlineKeyboardButton();
            menuButton.setText(BotLabels.SHOW_MAIN_SCREEN.getLabel());
            menuButton.setCallbackData("MENU_PRINCIPAL");

            lastRow.add(misTasksButton);
            lastRow.add(menuButton);
            rowsInline.add(lastRow);

            markupInline.setKeyboard(rowsInline);

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("📊 *Selecciona un estado para filtrar tus tareas:*");
            message.setParseMode("Markdown");
            message.setReplyMarkup(markupInline);

            bot.execute(message);
        } catch (Exception e) {
            logger.error("Error al mostrar menú de estados", e);
            BotHelper.sendMessageToTelegram(chatId, "❌ Error al cargar estados.", bot);
            MenuBotHelper.showMainMenu(chatId, bot);
        }
    }

    private void showTasksByState(String estado, Long chatId, Long telegramId, TelegramLongPollingBot bot) {
        try {
            Optional<Usuario> usuarioOpt = usuarioService.getUsuarioByTelegramId(telegramId);
            if (usuarioOpt.isEmpty()) {
                BotHelper.sendMessageToTelegram(chatId, "❌ No se encontró tu cuenta.", bot);
                return;
            }

            Usuario usuario = usuarioOpt.get();
            List<Tarea> tareas = tareaService.getTareasByUsuario(usuario.getUsuarioID());

            // Filter by state
            List<Tarea> tareasFiltradas = tareas.stream()
                    .filter(t -> {
                        switch (estado) {
                            case "PENDIENTE":
                                return t.getEstadoID() == 1L;
                            case "EN_PROCESO":
                                return t.getEstadoID() == 2L;
                            case "COMPLETADA":
                                return t.getEstadoID() == 3L;
                            default:
                                return false;
                        }
                    })
                    .collect(Collectors.toList());

            if (tareasFiltradas.isEmpty()) {
                BotHelper.sendMessageToTelegram(chatId,
                        "No tienes tareas en estado: " + estado.replace("_", " "), bot);
                showStateFilterMenu(chatId, telegramId, bot);
                return;
            }

            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("📊 *TAREAS EN ESTADO: ").append(estado.replace("_", " ")).append("*\n\n");

            for (Tarea tarea : tareasFiltradas) {
                messageBuilder.append("🔸 *").append(tarea.getTareaNombre()).append("*\n")
                        .append("📝 ").append(tarea.getDescripcion()).append("\n")
                        .append("📅 Entrega: ").append(tarea.getFechaEntrega().toLocalDate()
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                        .append("\n")
                        .append("⏱️ Horas estimadas: ").append(tarea.getHorasEstimadas()).append("\n");

                if (tarea.getHorasReales() != null) {
                    messageBuilder.append("⏱️ Horas reales: ").append(tarea.getHorasReales()).append("\n");
                }
                messageBuilder.append("\n");
            }

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(messageBuilder.toString());
            message.setParseMode("Markdown");

            // Crear teclado inline
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

            List<InlineKeyboardButton> row1 = new ArrayList<>();
            InlineKeyboardButton estadoButton = new InlineKeyboardButton();
            estadoButton.setText("📊 Ver por Estado");
            estadoButton.setCallbackData("VER_POR_ESTADO");
            row1.add(estadoButton);
            rowsInline.add(row1);

            List<InlineKeyboardButton> row2 = new ArrayList<>();
            InlineKeyboardButton menuButton = new InlineKeyboardButton();
            menuButton.setText(BotLabels.SHOW_MAIN_SCREEN.getLabel());
            menuButton.setCallbackData("MENU_PRINCIPAL");
            row2.add(menuButton);
            rowsInline.add(row2);

            markupInline.setKeyboard(rowsInline);
            message.setReplyMarkup(markupInline);

            bot.execute(message);
        } catch (Exception e) {
            logger.error("Error al mostrar tareas por estado", e);
            BotHelper.sendMessageToTelegram(chatId, "❌ Error al filtrar tareas.", bot);
            MenuBotHelper.showMainMenu(chatId, bot);
        }
    }

    public void setTaskForCompletion(Long tareaId, Long chatId) {
        try {
            ResponseEntity<Tarea> response = tareaService.getTareaById(tareaId);
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                return;
            }

            Tarea tarea = response.getBody();
            completionTasks.put(chatId, tarea);
            awaitingHoursReal.put(chatId, true);
        } catch (Exception e) {
            logger.error("Error al preparar la tarea para completar", e);
        }
    }

    public void handleCreationCallback(String callbackData, Long chatId, TelegramLongPollingBot bot) {
        TareaCreationState state = TareaCreationManager.getState(chatId);

        if (state == null) {
            BotHelper.sendMessageToTelegram(chatId, "❌ No hay una tarea en creación.", bot);
            return;
        }

        if (callbackData.startsWith("USUARIO_")) {
            // Handle user selection
            try {
                Long userId = Long.parseLong(callbackData.substring("USUARIO_".length()));
                state.getTarea().setUsuarioID(userId);
                state.setCurrentField("SPRINT");
                mostrarOpcionesSprints(chatId, bot);
            } catch (NumberFormatException e) {
                BotHelper.sendMessageToTelegram(chatId, "❌ Error al seleccionar usuario.", bot);
            }
        } else if (callbackData.startsWith("SPRINT_")) {
            // Handle sprint selection
            try {
                Long sprintId = Long.parseLong(callbackData.substring("SPRINT_".length()));
                state.getTarea().setSprintID(sprintId);
                state.setCurrentField("HORAS");
                mostrarOpcionesHoras(chatId, bot);
            } catch (NumberFormatException e) {
                BotHelper.sendMessageToTelegram(chatId, "❌ Error al seleccionar sprint.", bot);
            }
        } else if (callbackData.startsWith("HORAS_")) {
            // Handle hours selection
            try {
                int horas = Integer.parseInt(callbackData.substring("HORAS_".length()));
                state.getTarea().setHorasEstimadas(horas);
                state.setCurrentField("FECHA");
                mostrarOpcionesFecha(chatId, bot);
            } catch (NumberFormatException e) {
                BotHelper.sendMessageToTelegram(chatId, "❌ Error al seleccionar horas.", bot);
            }
        } else if (callbackData.equals("FECHA_MANUAL")) {
            // Switch to manual date entry mode
            state.setCurrentField("FECHA_MANUAL");
            BotHelper.sendMessageToTelegram(chatId,
                    "📅 Ingresa la fecha en formato *DD/MM/YYYY*:", bot);
        } else if (callbackData.startsWith("FECHA_")) {
            // Handle date selection
            try {
                String dateStr = callbackData.substring("FECHA_".length());
                LocalDate fecha = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                state.getTarea().setFechaEntrega(fecha.atStartOfDay().atOffset(ZoneOffset.UTC));
                state.getTarea().setCompletado(0);
                state.getTarea().setEstadoID(1L);

                // Create the task
                tareaService.createTarea(state.getTarea());

                // Clear the creation state
                TareaCreationManager.clearState(chatId);

                // Show success message
                BotHelper.sendMessageToTelegram(chatId,
                        "✅ ¡Tarea creada exitosamente!\n"
                                + "🔸 " + state.getTarea().getTareaNombre() + "\n"
                                + "📅 Fecha entrega: " + fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        bot);

                // Return to main menu
                MenuBotHelper.showMainMenu(chatId, bot);
            } catch (Exception e) {
                logger.error("Error al procesar fecha: " + e.getMessage());
                BotHelper.sendMessageToTelegram(chatId, "❌ Error al procesar la fecha seleccionada.", bot);
            }
        }
    }
}
