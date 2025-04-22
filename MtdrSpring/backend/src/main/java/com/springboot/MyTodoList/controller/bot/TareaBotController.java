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
                || messageText.equals("📊 KPIs")
                || messageText.equals("VER_KPIS")
                || messageText.startsWith("ESTADO_") // For state filtering
                || messageText.equals("👥 Ver Tareas")
                || messageText.equals("VER_TAREAS")
                || messageText.startsWith("✅ ")
                || messageText.equals("COMPLETAR_TAREAS")
                || messageText.equals("HISTORIAL_COMPLETADAS")
                || messageText.equals("VER_POR_ESTADO")
                || TareaCreationManager.isInCreationProcess(chatId);
    }

    public void handleMessage(String messageText, Long chatId, Long telegramId, TelegramLongPollingBot bot) {
        logger.info("Handling message: '" + messageText + "' from chatId: " + chatId);

        // First check for specific commands that should bypass other state checks
        if (messageText.equals(BotLabels.SHOW_MAIN_SCREEN.getLabel()) ||
                messageText.equals("/start")) {
            // Clear any pending states to avoid issues
            if (awaitingHoursReal.containsKey(chatId)) {
                awaitingHoursReal.remove(chatId);
                completionTasks.remove(chatId);
            }
            if (TareaCreationManager.isInCreationProcess(chatId)) {
                TareaCreationManager.clearState(chatId);
            }
            MenuBotHelper.showMainMenu(chatId, bot);
            return;
        }

        if (messageText.equals("👥 Ver Tareas") || messageText.equals("VER_TAREAS")) {
            mostrarSeleccionUsuarios(chatId, bot);
            return;
        }

        if (messageText.equals("📊 KPIs") || messageText.equals("VER_KPIS")) {
            mostrarOpcionesKPIs(chatId, bot);
            return;
        }

        // Now process hours input if we're waiting for it
        if (awaitingHoursReal.containsKey(chatId) && awaitingHoursReal.get(chatId)) {
            try {
                int horasReales = Integer.parseInt(messageText);
                Tarea tarea = completionTasks.get(chatId);
                if (tarea == null) {
                    BotHelper.sendMessageToTelegram(chatId,
                            "❌ Error al procesar la tarea. Volviendo al menú principal.", bot);
                    awaitingHoursReal.remove(chatId);
                    MenuBotHelper.showMainMenu(chatId, bot);
                    return;
                }

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
                BotHelper.sendMessageToTelegram(chatId, "❌ Por favor ingresa un número válido de horas.", bot);
                return;
            }
        }

        // Existing task creation logic
        if (TareaCreationManager.isInCreationProcess(chatId)) {
            handleTareaCreation(messageText, chatId, bot);
            return;
        }

        // Process other menu options
        switch (messageText) {
            case "➕ Nueva Tarea":
                solicitarNombreTarea(chatId, bot);
                break;
            case "👤 Mis Tareas":
                showUserTasks(chatId, telegramId, bot);
                break;
            case "✅ Completar Tareas":
            case "COMPLETAR_TAREAS":
                showTaskCompletionMenu(chatId, telegramId, bot);
                break;
            case "📋 Historial Completadas":
            case "HISTORIAL_COMPLETADAS":
                showCompletedTasks(chatId, telegramId, bot);
                break;
            case "📊 Ver por Estado":
            case "VER_POR_ESTADO":
                showStateFilterMenu(chatId, telegramId, bot);
                break;
            default:
                if (messageText.startsWith("✅ ")) {
                    handleTaskCompletion(messageText, chatId, telegramId, bot);
                } else if (messageText.startsWith("ESTADO_")) {
                    showTasksByState(messageText.substring(7), chatId, telegramId, bot);
                } else {
                    // Unknown command
                    BotHelper.sendMessageToTelegram(chatId,
                            "⚠️ Comando no reconocido. Usa /start para ver el menú principal.", bot);
                }
                break;
        }
    }

    public void handleFallback(String messageText, Long chatId, TelegramLongPollingBot bot) {
        // Importante: Registrar todos los mensajes que llegan a handleFallback para
        // diagnóstico
        logger.info("handleFallback recibido: '" + messageText + "' para chatId: " + chatId);

        if (TareaCreationManager.isInCreationProcess(chatId)) {
            logger.info("Usuario en proceso de creación de tarea, delegando a handleTareaCreation");
            handleTareaCreation(messageText, chatId, bot);
        } else if (awaitingHoursReal.getOrDefault(chatId, false)) {
            logger.info("Usuario está ingresando horas reales para completar tarea");
            handleRealHoursInput(messageText, chatId, bot);
        } else {
            // Mensaje más amigable en lugar de indicar que no se reconoce
            logger.info("Mensaje no reconocido, mostrando opciones del menú");
            BotHelper.sendMessageToTelegram(chatId, "Para continuar, por favor selecciona una opción del menú:", bot);
            MenuBotHelper.showMainMenu(chatId, bot);
        }
    }

    /**
     * Generates a standardized string representation of a task with consistent
     * formatting and emojis
     * 
     * @param tarea               The task to format
     * @param includeDetailedInfo Whether to include all available details (true) or
     *                            a more compact version (false)
     * @return A formatted string describing the task
     */
    private String formatTareaStandard(Tarea tarea, boolean includeDetailedInfo) {
        if (tarea == null) {
            return "❌ Tarea no disponible";
        }

        StringBuilder tareaInfo = new StringBuilder();

        // Determine status icon
        String statusIcon;
        if (tarea.getCompletado() == 1) {
            statusIcon = "✅";
        } else if (tarea.getEstadoID() != null && tarea.getEstadoID() == 2L) {
            statusIcon = "🔄";
        } else {
            statusIcon = "⏳";
        }

        // Format task name with status icon
        tareaInfo.append(statusIcon).append(" *")
                .append(tarea.getTareaNombre() != null ? tarea.getTareaNombre() : "Sin nombre").append("*\n");

        // Description
        tareaInfo.append("📝 ").append(tarea.getDescripcion() != null ? tarea.getDescripcion() : "Sin descripción")
                .append("\n");

        // Priority
        String prioridadIcon;
        if (tarea.getPrioridad() != null) {
            switch (tarea.getPrioridad()) {
                case "ALTA":
                    prioridadIcon = "⬆️";
                    break;
                case "MEDIA":
                    prioridadIcon = "↔️";
                    break;
                default:
                    prioridadIcon = "⬇️";
                    break;
            }
            tareaInfo.append(prioridadIcon).append(" Prioridad: ").append(tarea.getPrioridad()).append("\n");
        }

        // Due date
        if (tarea.getFechaEntrega() != null) {
            String formattedDate = tarea.getFechaEntrega().toLocalDate()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            tareaInfo.append("📅 Entrega: ").append(formattedDate).append("\n");
        } else {
            tareaInfo.append("📅 Entrega: Sin fecha\n");
        }

        // Estimated hours
        if (tarea.getHorasEstimadas() != null) {
            tareaInfo.append("⏱️ Horas estimadas: ").append(tarea.getHorasEstimadas()).append("\n");
        }

        // Real hours (only if task is completed)
        if (tarea.getCompletado() == 1 && tarea.getHorasReales() != null) {
            tareaInfo.append("⏱️ Horas reales: ").append(tarea.getHorasReales()).append("\n");
        }

        // Only include detailed info if requested
        if (includeDetailedInfo) {
            // Creation date
            if (tarea.getFechaCreacion() != null) {
                String formattedCreationDate = tarea.getFechaCreacion().toLocalDate()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                tareaInfo.append("🗓️ Creada: ").append(formattedCreationDate).append("\n");
            }

            // User information if available
            if (tarea.getUsuarioID() != null) {
                try {
                    ResponseEntity<Usuario> usuarioResponse = usuarioService.getUsuarioById(tarea.getUsuarioID());
                    if (usuarioResponse != null && usuarioResponse.getStatusCode().is2xxSuccessful()
                            && usuarioResponse.getBody() != null) {
                        Usuario usuario = usuarioResponse.getBody();
                        tareaInfo.append("👤 Asignada a: ").append(usuario.getNombre()).append("\n");
                    } else {
                        tareaInfo.append("👤 Asignada a: ID ").append(tarea.getUsuarioID()).append("\n");
                    }
                } catch (Exception e) {
                    logger.warn("No se pudo obtener información del usuario para la tarea: " + tarea.getTareaID());
                    tareaInfo.append("👤 Asignada a: ID ").append(tarea.getUsuarioID()).append("\n");
                }
            }

            // Sprint information if available
            if (tarea.getSprintID() != null) {
                try {
                    Optional<Sprint> sprintOpt = sprintService.getSprintById(tarea.getSprintID());
                    if (sprintOpt.isPresent()) {
                        tareaInfo.append("🏃 Sprint: ").append(sprintOpt.get().getNombreSprint()).append("\n");
                    } else {
                        tareaInfo.append("🏃 Sprint: ID ").append(tarea.getSprintID()).append("\n");
                    }
                } catch (Exception e) {
                    logger.warn("No se pudo obtener información del sprint para la tarea: " + tarea.getTareaID());
                    tareaInfo.append("🏃 Sprint: ID ").append(tarea.getSprintID()).append("\n");
                }
            }

            // Estado information
            if (tarea.getEstadoID() != null) {
                String estadoTexto;
                switch (tarea.getEstadoID().intValue()) {
                    case 1:
                        estadoTexto = "PENDIENTE";
                        break;
                    case 2:
                        estadoTexto = "EN PROCESO";
                        break;
                    case 3:
                        estadoTexto = "COMPLETADA";
                        break;
                    default:
                        estadoTexto = "DESCONOCIDO";
                }
                tareaInfo.append("🔍 Estado: ").append(estadoTexto).append("\n");
            }
        }

        return tareaInfo.toString();
    }

    public void mostrarTodasLasTareas(Long chatId, TelegramLongPollingBot bot) {
        logger.info("Iniciando mostrarTodasLasTareas para chatId: " + chatId);
        try {
            logger.info("Obteniendo todas las tareas desde el servicio...");
            List<Tarea> tareas = tareaService.getAllTareas();
            logger.info("Tareas obtenidas: " + (tareas != null ? tareas.size() : "null"));

            if (tareas == null || tareas.isEmpty()) {
                logger.info("No hay tareas para mostrar");
                BotHelper.sendMessageToTelegram(chatId, "No hay tareas registradas.", bot);
                return;
            }

            logger.info("Construyendo mensaje con " + tareas.size() + " tareas");
            StringBuilder messageText = new StringBuilder("📋 *Lista de Tareas:*\n\n");

            for (Tarea tarea : tareas) {
                try {
                    // Use the standardized task formatter with detailed info
                    messageText.append(formatTareaStandard(tarea, true)).append("\n");
                } catch (Exception e) {
                    logger.error("Error procesando tarea individual", e);
                    // Skip this task and continue with the next one
                }
            }

            // Check if we have any content after processing
            if (messageText.length() <= 30) { // Just the header text
                BotHelper.sendMessageToTelegram(chatId, "❌ No se pudieron procesar las tareas correctamente.", bot);
                MenuBotHelper.showMainMenu(chatId, bot);
                return;
            }

            // Check if message is too long - Telegram has 4096 character limit
            if (messageText.length() > 4000) {
                // Split message and send in chunks
                int chunkCount = 1;
                String fullMessage = messageText.toString();
                BotHelper.sendMessageToTelegram(chatId, "📋 *Lista de Tareas (Parte 1):*", bot);

                int start = 0;
                while (start < fullMessage.length()) {
                    int end = Math.min(start + 3800, fullMessage.length());
                    if (end < fullMessage.length()) {
                        // Find a good place to break the message (at a double newline)
                        int breakPoint = fullMessage.lastIndexOf("\n\n", end);
                        if (breakPoint > start + 100) { // At least send some substantial content
                            end = breakPoint;
                        }
                    }

                    String chunk = fullMessage.substring(start, end);
                    BotHelper.sendMessageToTelegram(chatId, chunk, bot);

                    start = end;
                    if (start < fullMessage.length()) {
                        chunkCount++;
                        BotHelper.sendMessageToTelegram(chatId, "📋 *Lista de Tareas (Parte " + chunkCount + "):*",
                                bot);
                    }
                }

                // Send back button in a separate message
                SendMessage backButtonMessage = new SendMessage();
                backButtonMessage.setChatId(chatId);
                backButtonMessage.setText("Selecciona una opción:");

                // Add back button
                InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

                List<InlineKeyboardButton> row = new ArrayList<>();
                InlineKeyboardButton menuButton = new InlineKeyboardButton();
                menuButton.setText("🔙 Volver al Menú");
                menuButton.setCallbackData("MENU_PRINCIPAL");
                row.add(menuButton);
                rowsInline.add(row);

                markupInline.setKeyboard(rowsInline);
                backButtonMessage.setReplyMarkup(markupInline);

                try {
                    bot.execute(backButtonMessage);
                } catch (Exception e) {
                    logger.error("Error al enviar botón de retorno", e);
                    BotHelper.sendMessageToTelegram(chatId, "Usa /start para volver al menú principal.", bot);
                }

                return;
            }

            // Add back button
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton menuButton = new InlineKeyboardButton();
            menuButton.setText("🔙 Volver al Menú");
            menuButton.setCallbackData("MENU_PRINCIPAL");
            row.add(menuButton);
            rowsInline.add(row);

            markupInline.setKeyboard(rowsInline);

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(messageText.toString());
            message.setParseMode("Markdown");
            message.setReplyMarkup(markupInline);

            logger.info("Enviando mensaje final con todas las tareas. Longitud del mensaje: " + messageText.length());
            bot.execute(message);
            logger.info("Mensaje enviado exitosamente");
        } catch (Exception e) {
            logger.error("Error mostrando lista de tareas", e);
            BotHelper.sendMessageToTelegram(chatId, "❌ Error al obtener la lista de tareas.", bot);
            MenuBotHelper.showMainMenu(chatId, bot);
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
        // Show an interactive calendar for the current month
        showCalendar(chatId, bot, LocalDate.now().getYear(), LocalDate.now().getMonthValue());
    }

    private void showCalendar(Long chatId, TelegramLongPollingBot bot, int year, int month) {
        LocalDate date = LocalDate.of(year, month, 1);

        // Build calendar markup
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        // Add month and year header
        List<InlineKeyboardButton> headerRow = new ArrayList<>();

        // Previous month button
        InlineKeyboardButton prevButton = new InlineKeyboardButton();
        prevButton.setText("◀️");
        prevButton.setCallbackData("CAL_PREV_" + year + "_" + month);
        headerRow.add(prevButton);

        // Month and year display
        InlineKeyboardButton monthYearButton = new InlineKeyboardButton();
        monthYearButton.setText(getMonthName(month) + " " + year);
        monthYearButton.setCallbackData("CAL_NONE");
        headerRow.add(monthYearButton);

        // Next month button
        InlineKeyboardButton nextButton = new InlineKeyboardButton();
        nextButton.setText("▶️");
        nextButton.setCallbackData("CAL_NEXT_" + year + "_" + month);
        headerRow.add(nextButton);

        rowsInline.add(headerRow);

        // Add days of week header
        List<InlineKeyboardButton> daysRow = new ArrayList<>();
        String[] dayNames = { "L", "M", "X", "J", "V", "S", "D" };
        for (String day : dayNames) {
            InlineKeyboardButton dayButton = new InlineKeyboardButton();
            dayButton.setText(day);
            dayButton.setCallbackData("CAL_NONE");
            daysRow.add(dayButton);
        }
        rowsInline.add(daysRow);

        // Calculate the first day of week for this month
        int firstDayOfWeek = date.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday

        // Calculate how many days in the month
        int daysInMonth = date.lengthOfMonth();

        // Create calendar grid
        int dayCounter = 1;

        // Add empty days at the beginning for correct alignment
        List<InlineKeyboardButton> weekRow = new ArrayList<>();
        for (int i = 1; i < firstDayOfWeek; i++) {
            InlineKeyboardButton emptyButton = new InlineKeyboardButton();
            emptyButton.setText(" ");
            emptyButton.setCallbackData("CAL_NONE");
            weekRow.add(emptyButton);
        }

        // First week might be partial
        while (weekRow.size() < 7 && dayCounter <= daysInMonth) {
            InlineKeyboardButton dayButton = new InlineKeyboardButton();

            boolean isToday = LocalDate.now().equals(
                    LocalDate.of(year, month, dayCounter));

            // Highlight today
            dayButton.setText(isToday ? "【" + dayCounter + "】" : String.valueOf(dayCounter));

            // Format the date string for callback data (DD/MM/YYYY)
            String dateStr = String.format("%02d/%02d/%04d", dayCounter, month, year);
            dayButton.setCallbackData("FECHA_" + dateStr);

            weekRow.add(dayButton);
            dayCounter++;
        }

        if (!weekRow.isEmpty()) {
            rowsInline.add(weekRow);
        }

        // Remaining weeks
        while (dayCounter <= daysInMonth) {
            weekRow = new ArrayList<>();

            for (int i = 0; i < 7 && dayCounter <= daysInMonth; i++) {
                InlineKeyboardButton dayButton = new InlineKeyboardButton();

                boolean isToday = LocalDate.now().equals(
                        LocalDate.of(year, month, dayCounter));

                dayButton.setText(isToday ? "【" + dayCounter + "】" : String.valueOf(dayCounter));

                // Format the date string for callback data (DD/MM/YYYY)
                String dateStr = String.format("%02d/%02d/%04d", dayCounter, month, year);
                dayButton.setCallbackData("FECHA_" + dateStr);

                weekRow.add(dayButton);
                dayCounter++;
            }

            // Fill empty slots at the end
            while (weekRow.size() < 7) {
                InlineKeyboardButton emptyButton = new InlineKeyboardButton();
                emptyButton.setText(" ");
                emptyButton.setCallbackData("CAL_NONE");
                weekRow.add(emptyButton);
            }

            rowsInline.add(weekRow);
        }

        // Add a row for manual entry
        List<InlineKeyboardButton> manualRow = new ArrayList<>();
        InlineKeyboardButton manualButton = new InlineKeyboardButton();
        manualButton.setText("Ingresar fecha manualmente");
        manualButton.setCallbackData("FECHA_MANUAL");
        manualRow.add(manualButton);
        rowsInline.add(manualRow);

        markupInline.setKeyboard(rowsInline);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("📅 *Selecciona una fecha:*");
        message.setParseMode("Markdown");
        message.setReplyMarkup(markupInline);

        try {
            bot.execute(message);
        } catch (Exception e) {
            logger.error("Error mostrando calendario", e);
        }
    }

    private String getMonthName(int month) {
        String[] monthNames = {
                "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        };
        return monthNames[month - 1];
    }

    private void handleTareaCreation(String messageText, Long chatId, TelegramLongPollingBot bot) {
        TareaCreationState state = TareaCreationManager.getState(chatId);

        if (state == null) {
            BotHelper.sendMessageToTelegram(chatId, "❌ Error en el proceso de creación. Intenta nuevamente con /start",
                    bot);
            return;
        }

        switch (state.getCurrentField()) {
            case "NOMBRE":
                // Validate task name
                if (messageText.trim().isEmpty() || messageText.length() > 100) {
                    BotHelper.sendMessageToTelegram(chatId,
                            "❌ Nombre inválido. Debe tener entre 1 y 100 caracteres.\n" +
                                    "Por favor, ingresa otro nombre:",
                            bot);
                    return;
                }

                state.getTarea().setTareaNombre(messageText);
                state.setCurrentField("DESCRIPCION");
                BotHelper.sendMessageToTelegram(chatId, "📝 Ingresa una *descripción* para la tarea:", bot);
                break;

            case "DESCRIPCION":
                // Validate description
                if (messageText.length() > 500) {
                    BotHelper.sendMessageToTelegram(chatId,
                            "❌ Descripción demasiado larga. Máximo 500 caracteres.\n" +
                                    "Por favor, ingresa una descripción más corta:",
                            bot);
                    return;
                }

                state.getTarea().setDescripcion(messageText);
                state.setCurrentField("PRIORIDAD");
                mostrarOpcionesPrioridad(chatId, bot);
                break;

            case "PRIORIDAD":
                // This is now only used for manual input, normally handled by callbacks
                if (!Pattern.matches("(?i)BAJA|MEDIA|ALTA", messageText)) {
                    BotHelper.sendMessageToTelegram(chatId, "❌ Prioridad inválida. Usa BAJA, MEDIA o ALTA.", bot);
                    mostrarOpcionesPrioridad(chatId, bot);
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
                    // Validate that user exists
                    if (!usuarioService.getAllUsuarios().stream().anyMatch(u -> u.getUsuarioID().equals(userId))) {
                        BotHelper.sendMessageToTelegram(chatId,
                                "❌ Usuario no encontrado. Por favor, selecciona de la lista.", bot);
                        mostrarOpcionesUsuarios(chatId, bot);
                        return;
                    }

                    state.getTarea().setUsuarioID(userId);
                    state.setCurrentField("SPRINT");
                    mostrarOpcionesSprints(chatId, bot);
                } catch (NumberFormatException e) {
                    BotHelper.sendMessageToTelegram(chatId, "❌ Formato inválido. Selecciona desde el menú.", bot);
                    mostrarOpcionesUsuarios(chatId, bot);
                }
                break;

            case "SPRINT":
                // This is only used for manual input, now handled by callbacks
                try {
                    Long sprintId = Long.parseLong(messageText.trim());
                    // Validate that sprint exists
                    if (!sprintService.getAllSprints().stream().anyMatch(s -> s.getSprintID().equals(sprintId))) {
                        BotHelper.sendMessageToTelegram(chatId,
                                "❌ Sprint no encontrado. Por favor, selecciona de la lista.", bot);
                        mostrarOpcionesSprints(chatId, bot);
                        return;
                    }

                    state.getTarea().setSprintID(sprintId);
                    state.setCurrentField("HORAS");
                    mostrarOpcionesHoras(chatId, bot);
                } catch (NumberFormatException e) {
                    BotHelper.sendMessageToTelegram(chatId, "❌ Formato inválido. Selecciona desde el menú.", bot);
                    mostrarOpcionesSprints(chatId, bot);
                }
                break;

            case "HORAS":
                // This is only used for manual input, now handled by callbacks
                try {
                    int horas = Integer.parseInt(messageText.trim());

                    if (horas <= 0) {
                        BotHelper.sendMessageToTelegram(chatId,
                                "⚠️ Las horas deben ser un número positivo.\n" +
                                        "Por favor, ingresa un número válido de horas.",
                                bot);
                        return;
                    }

                    if (horas > 4) {
                        BotHelper.sendMessageToTelegram(chatId,
                                "⚠️ Las tareas no pueden tener más de 4 horas estimadas.\n" +
                                        "Por favor, ingresa un número menor o igual a 4.",
                                bot);
                        return;
                    }

                    state.getTarea().setHorasEstimadas(horas);
                    state.setCurrentField("FECHA");
                    mostrarOpcionesFecha(chatId, bot);
                } catch (NumberFormatException e) {
                    BotHelper.sendMessageToTelegram(chatId,
                            "❌ Ingresa un número válido de horas.", bot);
                    mostrarOpcionesHoras(chatId, bot);
                }
                break;

            case "FECHA":
                // This is for manual date input
                try {
                    LocalDate fecha = LocalDate.parse(messageText.trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                    // Validate the date is not in the past
                    if (fecha.isBefore(LocalDate.now())) {
                        BotHelper.sendMessageToTelegram(chatId,
                                "❌ La fecha no puede ser en el pasado.\n" +
                                        "Por favor, ingresa una fecha futura:",
                                bot);
                        return;
                    }

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
                                    + "📅 Fecha entrega: " + fecha.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                            bot);

                    // Return to main menu
                    MenuBotHelper.showMainMenu(chatId, bot);
                } catch (Exception e) {
                    BotHelper.sendMessageToTelegram(chatId,
                            "❌ Formato de fecha inválido. Usa DD/MM/YYYY\n" +
                                    "Por ejemplo: 31/12/2023",
                            bot);
                }
                break;

            case "FECHA_MANUAL":
                // Special case for manual date entry
                try {
                    LocalDate fecha = LocalDate.parse(messageText.trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                    // Validate the date is not in the past
                    if (fecha.isBefore(LocalDate.now())) {
                        BotHelper.sendMessageToTelegram(chatId,
                                "❌ La fecha no puede ser en el pasado.\n" +
                                        "Por favor, ingresa una fecha futura:",
                                bot);
                        return;
                    }

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
                                    + "📅 Fecha entrega: " + fecha.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                            bot);

                    // Return to main menu
                    MenuBotHelper.showMainMenu(chatId, bot);
                } catch (Exception e) {
                    BotHelper.sendMessageToTelegram(chatId,
                            "❌ Formato de fecha inválido. Usa DD/MM/YYYY\n" +
                                    "Por ejemplo: 31/12/2023",
                            bot);
                }
                break;

            default:
                BotHelper.sendMessageToTelegram(chatId,
                        "❌ Error en el proceso de creación. Volviendo al menú principal.", bot);
                TareaCreationManager.clearState(chatId);
                MenuBotHelper.showMainMenu(chatId, bot);
                break;
        }
    }

    private void mostrarOpcionesPrioridad(Long chatId, TelegramLongPollingBot bot) {
        // Crear teclado inline
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        // Single row with 3 priority options
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton buttonBaja = new InlineKeyboardButton();
        buttonBaja.setText("⬇️ BAJA");
        buttonBaja.setCallbackData("PRIORIDAD_BAJA");

        InlineKeyboardButton buttonMedia = new InlineKeyboardButton();
        buttonMedia.setText("↔️ MEDIA");
        buttonMedia.setCallbackData("PRIORIDAD_MEDIA");

        InlineKeyboardButton buttonAlta = new InlineKeyboardButton();
        buttonAlta.setText("⬆️ ALTA");
        buttonAlta.setCallbackData("PRIORIDAD_ALTA");

        row.add(buttonBaja);
        row.add(buttonMedia);
        row.add(buttonAlta);
        rowsInline.add(row);

        markupInline.setKeyboard(rowsInline);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("📌 *Selecciona la prioridad para esta tarea:*");
        message.setParseMode("Markdown");
        message.setReplyMarkup(markupInline);

        try {
            bot.execute(message);
        } catch (Exception e) {
            logger.error("Error mostrando opciones de prioridad", e);
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

            // Use the standardized task formatter for each task with detailed info
            for (Tarea tarea : tareas) {
                messageBuilder.append(formatTareaStandard(tarea, true)).append("\n");
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

            // Fila nueva para Cambiar Estado
            List<InlineKeyboardButton> rowNueva = new ArrayList<>();
            InlineKeyboardButton cambiarEstadoButton = new InlineKeyboardButton();
            cambiarEstadoButton.setText("🔄 Cambiar estado");
            cambiarEstadoButton.setCallbackData("CAMBIAR_ESTADO");
            rowNueva.add(cambiarEstadoButton);
            rowsInline.add(rowNueva);

            // Fila 2
            List<InlineKeyboardButton> row2 = new ArrayList<>();
            InlineKeyboardButton estadoButton = new InlineKeyboardButton();
            estadoButton.setText("📊 Ver por Estado");
            estadoButton.setCallbackData("VER_POR_ESTADO");
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

            // Use the standardized task formatter for each completed task
            for (Tarea tarea : tareasCompletadas) {
                messageBuilder.append(formatTareaStandard(tarea, true)).append("\n");
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

            // Use the standardized task formatter for each filtered task
            for (Tarea tarea : tareasFiltradas) {
                messageBuilder.append(formatTareaStandard(tarea, true)).append("\n");
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
            MenuBotHelper.showMainMenu(chatId, bot);
            return;
        }

        try {
            // Handle calendar navigation
            if (callbackData.startsWith("CAL_PREV_") || callbackData.startsWith("CAL_NEXT_")) {
                String[] parts = callbackData.split("_");
                int year = Integer.parseInt(parts[2]);
                int month = Integer.parseInt(parts[3]);

                // Calculate new month and year
                if (callbackData.startsWith("CAL_PREV_")) {
                    // Previous month
                    month--;
                    if (month < 1) {
                        month = 12;
                        year--;
                    }
                } else {
                    // Next month
                    month++;
                    if (month > 12) {
                        month = 1;
                        year++;
                    }
                }

                // Show the calendar for the new month
                showCalendar(chatId, bot, year, month);
                return;
            } else if (callbackData.equals("CAL_NONE")) {
                // Ignore button presses on non-functional buttons
                return;
            }

            if (callbackData.startsWith("PRIORIDAD_")) {
                // Handle priority selection
                String prioridad = callbackData.substring("PRIORIDAD_".length());
                if (prioridad.equals("BAJA") || prioridad.equals("MEDIA") || prioridad.equals("ALTA")) {
                    state.getTarea().setPrioridad(prioridad);
                    state.setCurrentField("USUARIO");
                    mostrarOpcionesUsuarios(chatId, bot);
                } else {
                    BotHelper.sendMessageToTelegram(chatId, "❌ Prioridad no válida. Por favor, selecciona una opción.",
                            bot);
                    mostrarOpcionesPrioridad(chatId, bot);
                }
            } else if (callbackData.startsWith("USUARIO_")) {
                // Handle user selection
                try {
                    Long userId = Long.parseLong(callbackData.substring("USUARIO_".length()));
                    // Validate that user exists
                    if (!usuarioService.getAllUsuarios().stream().anyMatch(u -> u.getUsuarioID().equals(userId))) {
                        BotHelper.sendMessageToTelegram(chatId,
                                "❌ Usuario no encontrado. Por favor, selecciona de la lista.", bot);
                        mostrarOpcionesUsuarios(chatId, bot);
                        return;
                    }

                    state.getTarea().setUsuarioID(userId);
                    state.setCurrentField("SPRINT");
                    mostrarOpcionesSprints(chatId, bot);
                } catch (NumberFormatException e) {
                    BotHelper.sendMessageToTelegram(chatId, "❌ Error al seleccionar usuario.", bot);
                    mostrarOpcionesUsuarios(chatId, bot);
                }
            } else if (callbackData.startsWith("SPRINT_")) {
                // Handle sprint selection
                try {
                    Long sprintId = Long.parseLong(callbackData.substring("SPRINT_".length()));
                    // Validate that sprint exists
                    if (!sprintService.getAllSprints().stream().anyMatch(s -> s.getSprintID().equals(sprintId))) {
                        BotHelper.sendMessageToTelegram(chatId,
                                "❌ Sprint no encontrado. Por favor, selecciona de la lista.", bot);
                        mostrarOpcionesSprints(chatId, bot);
                        return;
                    }

                    state.getTarea().setSprintID(sprintId);
                    state.setCurrentField("HORAS");
                    mostrarOpcionesHoras(chatId, bot);
                } catch (NumberFormatException e) {
                    BotHelper.sendMessageToTelegram(chatId, "❌ Error al seleccionar sprint.", bot);
                    mostrarOpcionesSprints(chatId, bot);
                }
            } else if (callbackData.startsWith("HORAS_")) {
                // Handle hours selection
                try {
                    int horas = Integer.parseInt(callbackData.substring("HORAS_".length()));

                    if (horas <= 0 || horas > 4) {
                        BotHelper.sendMessageToTelegram(chatId, "❌ Valor de horas inválido.", bot);
                        mostrarOpcionesHoras(chatId, bot);
                        return;
                    }

                    state.getTarea().setHorasEstimadas(horas);
                    state.setCurrentField("FECHA");
                    mostrarOpcionesFecha(chatId, bot);
                } catch (NumberFormatException e) {
                    BotHelper.sendMessageToTelegram(chatId, "❌ Error al seleccionar horas.", bot);
                    mostrarOpcionesHoras(chatId, bot);
                }
            } else if (callbackData.equals("FECHA_MANUAL")) {
                // Switch to manual date entry mode
                state.setCurrentField("FECHA_MANUAL");
                BotHelper.sendMessageToTelegram(chatId,
                        "📅 Ingresa la fecha en formato *DD/MM/YYYY*:\n" +
                                "Por ejemplo: 31/12/2023",
                        bot);
            } else if (callbackData.startsWith("FECHA_")) {
                // Handle date selection
                try {
                    String dateStr = callbackData.substring("FECHA_".length());
                    LocalDate fecha = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                    // Validate the date is not in the past
                    if (fecha.isBefore(LocalDate.now())) {
                        BotHelper.sendMessageToTelegram(chatId, "❌ La fecha seleccionada está en el pasado.", bot);
                        mostrarOpcionesFecha(chatId, bot);
                        return;
                    }

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
                                    + "📅 Fecha entrega: " + fecha.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                            bot);

                    // Return to main menu
                    MenuBotHelper.showMainMenu(chatId, bot);
                } catch (Exception e) {
                    logger.error("Error al procesar fecha: " + e.getMessage());
                    BotHelper.sendMessageToTelegram(chatId, "❌ Error al procesar la fecha seleccionada.", bot);
                    mostrarOpcionesFecha(chatId, bot);
                }
            } else {
                BotHelper.sendMessageToTelegram(chatId, "❌ Operación no reconocida.", bot);
                MenuBotHelper.showMainMenu(chatId, bot);
            }
        } catch (Exception e) {
            logger.error("Error en handleCreationCallback: " + e.getMessage(), e);
            BotHelper.sendMessageToTelegram(chatId,
                    "❌ Ha ocurrido un error inesperado.\n" +
                            "Por favor, inténtalo de nuevo con /start",
                    bot);
            TareaCreationManager.clearState(chatId);
            MenuBotHelper.showMainMenu(chatId, bot);
        }
    }

    // Método de debugging para diagnóstico
    public void debugTareas(Long chatId, TelegramLongPollingBot bot) {
        logger.info("Iniciando diagnóstico de tareas...");
        try {
            // Verificar que el servicio de tareas funciona
            logger.info("Verificando servicio de tareas...");
            List<Tarea> tareas = tareaService.getAllTareas();

            int totalTareas = (tareas != null) ? tareas.size() : 0;
            logger.info("Total de tareas recuperadas: " + totalTareas);

            StringBuilder debugInfo = new StringBuilder();
            debugInfo.append("📊 *Diagnóstico del Sistema:*\n\n");
            debugInfo.append("Total de tareas: ").append(totalTareas).append("\n\n");

            if (tareas != null && !tareas.isEmpty()) {
                // Mostrar resumen de la primera tarea como ejemplo
                Tarea ejemploTarea = tareas.get(0);
                debugInfo.append("*Ejemplo de Tarea:*\n");
                debugInfo.append("ID: ").append(ejemploTarea.getTareaID()).append("\n");
                debugInfo.append("Nombre: ").append(ejemploTarea.getTareaNombre()).append("\n");
                debugInfo.append("Descripción: ").append(ejemploTarea.getDescripcion()).append("\n");
                debugInfo.append("Usuario ID: ").append(ejemploTarea.getUsuarioID()).append("\n");
                debugInfo.append("Sprint ID: ").append(ejemploTarea.getSprintID()).append("\n");
                debugInfo.append("Estado ID: ").append(ejemploTarea.getEstadoID()).append("\n");
                debugInfo.append("Completado: ").append(ejemploTarea.getCompletado()).append("\n");
            } else {
                debugInfo.append("❌ *No hay tareas en el sistema*\n");
                debugInfo.append("Verifique la conexión a la base de datos y que existan registros.\n");
            }

            // Enviar información de diagnóstico
            BotHelper.sendMessageToTelegram(chatId, debugInfo.toString(), bot);

        } catch (Exception e) {
            logger.error("Error durante el diagnóstico", e);
            BotHelper.sendMessageToTelegram(chatId,
                    "❌ Error durante el diagnóstico: " + e.getMessage(), bot);
        }
    }

    private void mostrarSeleccionUsuarios(Long chatId, TelegramLongPollingBot bot) {
        try {
            logger.info("Mostrando selección de usuarios para ver tareas");
            List<Usuario> usuarios = usuarioService.getAllUsuarios();

            if (usuarios == null || usuarios.isEmpty()) {
                BotHelper.sendMessageToTelegram(chatId, "❌ No hay usuarios registrados en el sistema.", bot);
                MenuBotHelper.showMainMenu(chatId, bot);
                return;
            }

            // Crear teclado inline
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

            // Agregar cada usuario como un botón
            for (Usuario usuario : usuarios) {
                List<InlineKeyboardButton> row = new ArrayList<>();
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(usuario.getNombre());
                button.setCallbackData("VER_TAREAS_USER_" + usuario.getUsuarioID());
                row.add(button);
                rowsInline.add(row);
            }

            // Botón para volver al menú principal
            List<InlineKeyboardButton> lastRow = new ArrayList<>();
            InlineKeyboardButton menuButton = new InlineKeyboardButton();
            menuButton.setText("🔙 Volver al Menú");
            menuButton.setCallbackData("MENU_PRINCIPAL");
            lastRow.add(menuButton);
            rowsInline.add(lastRow);

            markupInline.setKeyboard(rowsInline);

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("👥 *Selecciona un usuario para ver sus tareas:*");
            message.setParseMode("Markdown");
            message.setReplyMarkup(markupInline);

            bot.execute(message);
        } catch (Exception e) {
            logger.error("Error al mostrar selección de usuarios", e);
            BotHelper.sendMessageToTelegram(chatId, "❌ Error al cargar la lista de usuarios.", bot);
            MenuBotHelper.showMainMenu(chatId, bot);
        }
    }

    /**
     * Muestra las tareas de un usuario específico
     * 
     * @param userId ID del usuario
     * @param chatId ID del chat
     * @param bot    Bot de Telegram
     */
    public void mostrarTareasUsuario(Long userId, Long chatId, TelegramLongPollingBot bot) {
        try {
            logger.info("Mostrando tareas del usuario ID: " + userId);

            // Obtener datos del usuario
            ResponseEntity<Usuario> usuarioResponse = usuarioService.getUsuarioById(userId);
            if (usuarioResponse == null || !usuarioResponse.getStatusCode().is2xxSuccessful() ||
                    usuarioResponse.getBody() == null) {
                BotHelper.sendMessageToTelegram(chatId, "❌ No se pudo encontrar el usuario.", bot);
                mostrarSeleccionUsuarios(chatId, bot);
                return;
            }

            Usuario usuario = usuarioResponse.getBody();
            List<Tarea> tareas = tareaService.getTareasByUsuario(userId);

            if (tareas == null || tareas.isEmpty()) {
                BotHelper.sendMessageToTelegram(chatId,
                        "📋 *" + usuario.getNombre() + "* no tiene tareas asignadas.", bot);
                mostrarSeleccionUsuarios(chatId, bot);
                return;
            }

            // Filtrar solo tareas pendientes o en proceso (no completadas)
            List<Tarea> tareasPendientesOEnProceso = tareas.stream()
                    .filter(tarea -> tarea.getCompletado() == 0 ||
                            (tarea.getEstadoID() != null && tarea.getEstadoID() != 3L))
                    .collect(Collectors.toList());

            if (tareasPendientesOEnProceso.isEmpty()) {
                BotHelper.sendMessageToTelegram(chatId,
                        "📋 *" + usuario.getNombre() + "* no tiene tareas pendientes o en proceso actualmente.", bot);
                mostrarSeleccionUsuarios(chatId, bot);
                return;
            }

            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("📋 *Tareas pendientes/en proceso de ").append(usuario.getNombre()).append(":*\n\n");

            for (Tarea tarea : tareasPendientesOEnProceso) {
                try {
                    // Use the standardized task formatter with detailed info
                    messageBuilder.append(formatTareaStandard(tarea, true)).append("\n");
                } catch (Exception e) {
                    logger.error("Error al formatear tarea individual", e);
                    // Continuar con la siguiente tarea
                }
            }

            // Verificar si el mensaje es demasiado largo para Telegram (límite 4096
            // caracteres)
            if (messageBuilder.length() > 4000) {
                enviarMensajePorPartes(messageBuilder.toString(), usuario.getNombre(), chatId, bot);
                return;
            }

            // Botones para opciones adicionales
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

            // Botón para volver a la selección de usuarios
            List<InlineKeyboardButton> row1 = new ArrayList<>();
            InlineKeyboardButton backButton = new InlineKeyboardButton();
            backButton.setText("👥 Volver a Usuarios");
            backButton.setCallbackData("VER_TAREAS");
            row1.add(backButton);
            rowsInline.add(row1);

            // Botón para el menú principal
            List<InlineKeyboardButton> row2 = new ArrayList<>();
            InlineKeyboardButton menuButton = new InlineKeyboardButton();
            menuButton.setText("🔙 Menú Principal");
            menuButton.setCallbackData("MENU_PRINCIPAL");
            row2.add(menuButton);
            rowsInline.add(row2);

            markupInline.setKeyboard(rowsInline);

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(messageBuilder.toString());
            message.setParseMode("Markdown");
            message.setReplyMarkup(markupInline);

            bot.execute(message);

        } catch (Exception e) {
            logger.error("Error al mostrar tareas del usuario", e);
            BotHelper.sendMessageToTelegram(chatId, "❌ Error al obtener las tareas del usuario.", bot);
            mostrarSeleccionUsuarios(chatId, bot);
        }
    }

    /**
     * Envía un mensaje largo dividiéndolo en partes
     * 
     * @param mensaje       Mensaje completo
     * @param nombreUsuario Nombre del usuario para títulos
     * @param chatId        ID del chat
     * @param bot           Bot de Telegram
     */
    private void enviarMensajePorPartes(String mensaje, String nombreUsuario, Long chatId, TelegramLongPollingBot bot) {
        try {
            int chunkCount = 1;
            BotHelper.sendMessageToTelegram(chatId, "📋 *Tareas de " + nombreUsuario + " (Parte 1):*", bot);

            int start = 0;
            while (start < mensaje.length()) {
                int end = Math.min(start + 3800, mensaje.length());
                if (end < mensaje.length()) {
                    // Buscar un buen lugar para cortar el mensaje (en un doble salto de línea)
                    int breakPoint = mensaje.lastIndexOf("\n\n", end);
                    if (breakPoint > start + 100) { // Al menos enviar contenido sustancial
                        end = breakPoint;
                    }
                }

                String chunk = mensaje.substring(start, end);
                BotHelper.sendMessageToTelegram(chatId, chunk, bot);

                start = end;
                if (start < mensaje.length()) {
                    chunkCount++;
                    BotHelper.sendMessageToTelegram(chatId,
                            "📋 *Tareas de " + nombreUsuario + " (Parte " + chunkCount + "):*", bot);
                }
            }

            // Agregar botones después del último fragmento
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

            List<InlineKeyboardButton> row1 = new ArrayList<>();
            InlineKeyboardButton backButton = new InlineKeyboardButton();
            backButton.setText("👥 Volver a Usuarios");
            backButton.setCallbackData("VER_TAREAS");
            row1.add(backButton);
            rowsInline.add(row1);

            List<InlineKeyboardButton> row2 = new ArrayList<>();
            InlineKeyboardButton menuButton = new InlineKeyboardButton();
            menuButton.setText("🔙 Menú Principal");
            menuButton.setCallbackData("MENU_PRINCIPAL");
            row2.add(menuButton);
            rowsInline.add(row2);

            markupInline.setKeyboard(rowsInline);

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("Selecciona una opción:");
            message.setReplyMarkup(markupInline);

            bot.execute(message);
        } catch (Exception e) {
            logger.error("Error al enviar mensaje por partes", e);
            BotHelper.sendMessageToTelegram(chatId,
                    "❌ Error al mostrar todas las tareas. Volviendo al menú principal.", bot);
            mostrarSeleccionUsuarios(chatId, bot);
        }
    }

    /**
     * Muestra la selección de usuarios para ver KPIs
     * 
     * @param chatId ID del chat
     * @param bot    Bot de Telegram
     */
    public void mostrarSeleccionUsuariosParaKPIs(Long chatId, TelegramLongPollingBot bot) {
        try {
            logger.info("Mostrando selección de usuarios para ver KPIs");
            List<Usuario> usuarios = usuarioService.getAllUsuarios();

            if (usuarios == null || usuarios.isEmpty()) {
                BotHelper.sendMessageToTelegram(chatId, "❌ No hay usuarios registrados en el sistema.", bot);
                mostrarOpcionesKPIs(chatId, bot);
                return;
            }

            // Crear teclado inline
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

            // Agregar cada usuario como un botón
            for (Usuario usuario : usuarios) {
                List<InlineKeyboardButton> row = new ArrayList<>();
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(usuario.getNombre());
                button.setCallbackData("VER_KPIS_USER_" + usuario.getUsuarioID());
                row.add(button);
                rowsInline.add(row);
            }

            // Botón para volver a las opciones de KPIs
            List<InlineKeyboardButton> lastRow = new ArrayList<>();
            InlineKeyboardButton backButton = new InlineKeyboardButton();
            backButton.setText("🔙 Volver a Opciones");
            backButton.setCallbackData("VER_KPIS");
            lastRow.add(backButton);
            rowsInline.add(lastRow);

            markupInline.setKeyboard(rowsInline);

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("👤 *Selecciona un usuario para ver sus KPIs:*");
            message.setParseMode("Markdown");
            message.setReplyMarkup(markupInline);

            bot.execute(message);
        } catch (Exception e) {
            logger.error("Error al mostrar selección de usuarios para KPIs", e);
            BotHelper.sendMessageToTelegram(chatId, "❌ Error al cargar la lista de usuarios.", bot);
            mostrarOpcionesKPIs(chatId, bot);
        }
    }

    /**
     * Muestra los KPIs de un usuario específico
     * 
     * @param userId ID del usuario
     * @param chatId ID del chat
     * @param bot    Bot de Telegram
     */
    public void mostrarKPIsUsuario(Long userId, Long chatId, TelegramLongPollingBot bot) {
        try {
            logger.info("Mostrando KPIs del usuario ID: " + userId);

            // Obtener datos del usuario
            ResponseEntity<Usuario> usuarioResponse = usuarioService.getUsuarioById(userId);
            if (usuarioResponse == null || !usuarioResponse.getStatusCode().is2xxSuccessful() ||
                    usuarioResponse.getBody() == null) {
                BotHelper.sendMessageToTelegram(chatId, "❌ No se pudo encontrar el usuario.", bot);
                mostrarSeleccionUsuariosParaKPIs(chatId, bot);
                return;
            }

            Usuario usuario = usuarioResponse.getBody();
            List<Tarea> tareas = tareaService.getTareasByUsuario(userId);

            if (tareas == null || tareas.isEmpty()) {
                BotHelper.sendMessageToTelegram(chatId,
                        "📊 *" + usuario.getNombre() + "* no tiene tareas asignadas, no es posible calcular KPIs.",
                        bot);
                mostrarSeleccionUsuariosParaKPIs(chatId, bot);
                return;
            }

            // Calcular KPIs
            int totalTareas = tareas.size();
            int tareasCompletadas = 0;
            int tareasPendientes = 0;
            int tareasEnProceso = 0;
            int horasEstimadasTotal = 0;
            int horasRealesTotal = 0;
            int tareasConHorasReales = 0;

            for (Tarea tarea : tareas) {
                if (tarea.getCompletado() == 1) {
                    tareasCompletadas++;
                    if (tarea.getHorasReales() != null) {
                        horasRealesTotal += tarea.getHorasReales();
                        tareasConHorasReales++;
                    }
                } else if (tarea.getEstadoID() != null && tarea.getEstadoID() == 2L) {
                    tareasEnProceso++;
                } else {
                    tareasPendientes++;
                }

                if (tarea.getHorasEstimadas() != null) {
                    horasEstimadasTotal += tarea.getHorasEstimadas();
                }
            }

            // Calcular porcentajes
            double porcentajeCompletitud = (totalTareas > 0) ? ((double) tareasCompletadas / totalTareas) * 100 : 0;
            double eficiencia = (tareasConHorasReales > 0 && horasEstimadasTotal > 0)
                    ? ((double) horasEstimadasTotal / horasRealesTotal) * 100
                    : 0;

            // Construir mensaje de KPIs
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("📊 *KPIs de ").append(usuario.getNombre()).append("*\n\n");

            // Estadísticas generales
            messageBuilder.append("🔢 *Estadísticas Generales:*\n");
            messageBuilder.append("📋 Total de tareas: ").append(totalTareas).append("\n");
            messageBuilder.append("✅ Tareas completadas: ").append(tareasCompletadas).append(" (")
                    .append(String.format("%.1f", porcentajeCompletitud)).append("%)\n");
            messageBuilder.append("🔄 Tareas en proceso: ").append(tareasEnProceso).append("\n");
            messageBuilder.append("⏳ Tareas pendientes: ").append(tareasPendientes).append("\n\n");

            // Estadísticas de tiempo
            messageBuilder.append("⏱️ *Estadísticas de Tiempo:*\n");
            messageBuilder.append("⌛ Horas estimadas totales: ").append(horasEstimadasTotal).append("\n");

            if (tareasConHorasReales > 0) {
                messageBuilder.append("⏰ Horas reales totales: ").append(horasRealesTotal).append("\n");

                // Solo mostrar eficiencia si hay datos suficientes
                if (eficiencia > 0) {
                    String emojiEficiencia = (eficiencia >= 100) ? "🟢" : (eficiencia >= 80) ? "🟡" : "🔴";
                    messageBuilder.append(emojiEficiencia).append(" Eficiencia: ")
                            .append(String.format("%.1f", eficiencia)).append("%\n");

                    if (eficiencia >= 100) {
                        messageBuilder.append(
                                "🌟 *¡Excelente eficiencia!* Tareas completadas en menos tiempo del estimado.\n");
                    } else if (eficiencia >= 80) {
                        messageBuilder.append("👍 *Buena eficiencia.* Cercano a lo estimado.\n");
                    } else {
                        messageBuilder
                                .append("⚠️ *Eficiencia por mejorar.* Las tareas tomaron más tiempo del estimado.\n");
                    }
                }
            } else {
                messageBuilder.append("⚠️ No hay tareas completadas con horas reales registradas.\n");
            }

            // Añadir una nota explicativa
            messageBuilder.append(
                    "\n💡 *Nota:* La eficiencia se calcula comparando las horas estimadas con las horas reales.\n");

            // Botones para opciones adicionales
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

            // Botón para volver a la selección de usuarios
            List<InlineKeyboardButton> row1 = new ArrayList<>();
            InlineKeyboardButton backButton = new InlineKeyboardButton();
            backButton.setText("👤 Volver a Usuarios");
            backButton.setCallbackData("VER_KPIS_USUARIOS");
            row1.add(backButton);
            rowsInline.add(row1);

            // Botón para volver a opciones de KPIs
            List<InlineKeyboardButton> row2 = new ArrayList<>();
            InlineKeyboardButton optionsButton = new InlineKeyboardButton();
            optionsButton.setText("📊 Opciones KPI");
            optionsButton.setCallbackData("VER_KPIS");
            row2.add(optionsButton);
            rowsInline.add(row2);

            // Botón para el menú principal
            List<InlineKeyboardButton> row3 = new ArrayList<>();
            InlineKeyboardButton menuButton = new InlineKeyboardButton();
            menuButton.setText("🔙 Menú Principal");
            menuButton.setCallbackData("MENU_PRINCIPAL");
            row3.add(menuButton);
            rowsInline.add(row3);

            markupInline.setKeyboard(rowsInline);

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(messageBuilder.toString());
            message.setParseMode("Markdown");
            message.setReplyMarkup(markupInline);

            bot.execute(message);

        } catch (Exception e) {
            logger.error("Error al mostrar KPIs del usuario", e);
            BotHelper.sendMessageToTelegram(chatId, "❌ Error al obtener los KPIs del usuario.", bot);
            mostrarSeleccionUsuariosParaKPIs(chatId, bot);
        }
    }

    /**
     * Muestra los KPIs de un sprint específico
     * 
     * @param sprintId ID del sprint
     * @param chatId   ID del chat
     * @param bot      Bot de Telegram
     */
    public void mostrarKPIsSprint(Long sprintId, Long chatId, TelegramLongPollingBot bot) {
        try {
            logger.info("Mostrando KPIs del sprint ID: " + sprintId);

            // Obtener datos del sprint
            Optional<Sprint> sprintOpt = sprintService.getSprintById(sprintId);
            if (sprintOpt.isEmpty()) {
                BotHelper.sendMessageToTelegram(chatId, "❌ No se pudo encontrar el sprint.", bot);
                mostrarSeleccionSprintsParaKPIs(chatId, bot);
                return;
            }

            Sprint sprint = sprintOpt.get();

            // Obtener todas las tareas del sprint
            List<Tarea> tareasSprint = tareaService.getTareasBySprint(sprintId);

            if (tareasSprint == null || tareasSprint.isEmpty()) {
                BotHelper.sendMessageToTelegram(chatId,
                        "📊 El sprint *" + sprint.getNombreSprint()
                                + "* no tiene tareas asignadas, no es posible calcular KPIs.",
                        bot);
                mostrarSeleccionSprintsParaKPIs(chatId, bot);
                return;
            }

            // Calcular KPIs
            int totalTareas = tareasSprint.size();
            int tareasCompletadas = 0;
            int tareasPendientes = 0;
            int tareasEnProceso = 0;
            int horasEstimadasTotal = 0;
            int horasRealesTotal = 0;
            int tareasConHorasReales = 0;

            // Map para agrupar tareas por usuario
            Map<Long, List<Tarea>> tareasPorUsuario = new HashMap<>();

            for (Tarea tarea : tareasSprint) {
                if (tarea.getCompletado() == 1) {
                    tareasCompletadas++;
                    if (tarea.getHorasReales() != null) {
                        horasRealesTotal += tarea.getHorasReales();
                        tareasConHorasReales++;
                    }
                } else if (tarea.getEstadoID() != null && tarea.getEstadoID() == 2L) {
                    tareasEnProceso++;
                } else {
                    tareasPendientes++;
                }

                if (tarea.getHorasEstimadas() != null) {
                    horasEstimadasTotal += tarea.getHorasEstimadas();
                }

                // Agrupar tareas por usuario
                if (tarea.getUsuarioID() != null) {
                    tareasPorUsuario.computeIfAbsent(tarea.getUsuarioID(), k -> new ArrayList<>()).add(tarea);
                }
            }

            // Calcular porcentajes
            double porcentajeCompletitud = (totalTareas > 0) ? ((double) tareasCompletadas / totalTareas) * 100 : 0;
            double eficiencia = (tareasConHorasReales > 0 && horasEstimadasTotal > 0)
                    ? ((double) horasEstimadasTotal / horasRealesTotal) * 100
                    : 0;

            // Construir mensaje de KPIs
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("📊 *KPIs del Sprint: ").append(sprint.getNombreSprint()).append("*\n\n");

            // Fechas del sprint
            if (sprint.getStartDate() != null) {
                String fechaInicio = new java.text.SimpleDateFormat("yyyy-MM-dd").format(sprint.getStartDate());
                messageBuilder.append("🗓️ Fecha inicio: ").append(fechaInicio).append("\n");
            }

            if (sprint.getEndDate() != null) {
                String fechaFin = new java.text.SimpleDateFormat("yyyy-MM-dd").format(sprint.getEndDate());
                messageBuilder.append("🗓️ Fecha fin: ").append(fechaFin).append("\n\n");
            }

            // Estadísticas generales
            messageBuilder.append("🔢 *Estadísticas Generales:*\n");
            messageBuilder.append("📋 Total de tareas: ").append(totalTareas).append("\n");
            messageBuilder.append("✅ Tareas completadas: ").append(tareasCompletadas).append(" (")
                    .append(String.format("%.1f", porcentajeCompletitud)).append("%)\n");
            messageBuilder.append("🔄 Tareas en proceso: ").append(tareasEnProceso).append("\n");
            messageBuilder.append("⏳ Tareas pendientes: ").append(tareasPendientes).append("\n\n");

            // Estadísticas de tiempo
            messageBuilder.append("⏱️ *Estadísticas de Tiempo:*\n");
            messageBuilder.append("⌛ Horas estimadas totales: ").append(horasEstimadasTotal).append("\n");

            if (tareasConHorasReales > 0) {
                messageBuilder.append("⏰ Horas reales totales: ").append(horasRealesTotal).append("\n");

                // Solo mostrar eficiencia si hay datos suficientes
                if (eficiencia > 0) {
                    String emojiEficiencia = (eficiencia >= 100) ? "🟢" : (eficiencia >= 80) ? "🟡" : "🔴";
                    messageBuilder.append(emojiEficiencia).append(" Eficiencia global: ")
                            .append(String.format("%.1f", eficiencia)).append("%\n\n");
                }
            } else {
                messageBuilder.append("⚠️ No hay tareas completadas con horas reales registradas.\n\n");
            }

            // KPIs por usuario
            if (!tareasPorUsuario.isEmpty()) {
                messageBuilder.append("👥 *Participación por Usuario:*\n");

                for (Map.Entry<Long, List<Tarea>> entry : tareasPorUsuario.entrySet()) {
                    Long userId = entry.getKey();
                    List<Tarea> tareasUsuario = entry.getValue();

                    // Obtener información del usuario
                    String nombreUsuario = "Usuario ID: " + userId;
                    try {
                        ResponseEntity<Usuario> usuarioResponse = usuarioService.getUsuarioById(userId);
                        if (usuarioResponse != null && usuarioResponse.getStatusCode().is2xxSuccessful()
                                && usuarioResponse.getBody() != null) {
                            nombreUsuario = usuarioResponse.getBody().getNombre();
                        }
                    } catch (Exception e) {
                        logger.warn("No se pudo obtener información del usuario ID: " + userId);
                    }

                    int tareasUsuarioCompletadas = (int) tareasUsuario.stream()
                            .filter(t -> t.getCompletado() == 1)
                            .count();

                    double porcentajeContribucion = ((double) tareasUsuario.size() / totalTareas) * 100;
                    double porcentajeCompletitudUsuario = (tareasUsuario.size() > 0)
                            ? ((double) tareasUsuarioCompletadas / tareasUsuario.size()) * 100
                            : 0;

                    messageBuilder.append("👤 *").append(nombreUsuario).append(":* ");
                    messageBuilder.append(tareasUsuario.size()).append(" tareas (")
                            .append(String.format("%.1f", porcentajeContribucion)).append("% del total)\n");
                    messageBuilder.append("✅ Completadas: ").append(tareasUsuarioCompletadas)
                            .append(" (").append(String.format("%.1f", porcentajeCompletitudUsuario)).append("%)\n\n");
                }
            }

            // Añadir una nota explicativa
            messageBuilder
                    .append("💡 *Nota:* La eficiencia se calcula comparando las horas estimadas con las horas reales.");

            // Botones para opciones adicionales
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

            // Botón para volver a la selección de sprints
            List<InlineKeyboardButton> row1 = new ArrayList<>();
            InlineKeyboardButton backButton = new InlineKeyboardButton();
            backButton.setText("🏃 Volver a Sprints");
            backButton.setCallbackData("VER_KPIS_SPRINTS");
            row1.add(backButton);
            rowsInline.add(row1);

            // Botón para volver a opciones de KPIs
            List<InlineKeyboardButton> row2 = new ArrayList<>();
            InlineKeyboardButton optionsButton = new InlineKeyboardButton();
            optionsButton.setText("📊 Opciones KPI");
            optionsButton.setCallbackData("VER_KPIS");
            row2.add(optionsButton);
            rowsInline.add(row2);

            // Botón para el menú principal
            List<InlineKeyboardButton> row3 = new ArrayList<>();
            InlineKeyboardButton menuButton = new InlineKeyboardButton();
            menuButton.setText("🔙 Menú Principal");
            menuButton.setCallbackData("MENU_PRINCIPAL");
            row3.add(menuButton);
            rowsInline.add(row3);

            markupInline.setKeyboard(rowsInline);

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(messageBuilder.toString());
            message.setParseMode("Markdown");
            message.setReplyMarkup(markupInline);

            bot.execute(message);

        } catch (Exception e) {
            logger.error("Error al mostrar KPIs del sprint", e);
            BotHelper.sendMessageToTelegram(chatId, "❌ Error al obtener los KPIs del sprint.", bot);
            mostrarSeleccionSprintsParaKPIs(chatId, bot);
        }
    }

    /**
     * Muestra la selección de sprints para ver KPIs
     * 
     * @param chatId ID del chat
     * @param bot    Bot de Telegram
     */
    public void mostrarSeleccionSprintsParaKPIs(Long chatId, TelegramLongPollingBot bot) {
        try {
            logger.info("Mostrando selección de sprints para ver KPIs");
            List<Sprint> sprints = sprintService.getAllSprints();

            if (sprints == null || sprints.isEmpty()) {
                BotHelper.sendMessageToTelegram(chatId, "❌ No hay sprints registrados en el sistema.", bot);
                mostrarOpcionesKPIs(chatId, bot);
                return;
            }

            // Crear teclado inline
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

            // Agregar cada sprint como un botón
            for (Sprint sprint : sprints) {
                List<InlineKeyboardButton> row = new ArrayList<>();
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(sprint.getNombreSprint());
                button.setCallbackData("VER_KPIS_SPRINT_" + sprint.getSprintID());
                row.add(button);
                rowsInline.add(row);
            }

            // Botón para volver a las opciones de KPIs
            List<InlineKeyboardButton> lastRow = new ArrayList<>();
            InlineKeyboardButton backButton = new InlineKeyboardButton();
            backButton.setText("🔙 Volver a Opciones");
            backButton.setCallbackData("VER_KPIS");
            lastRow.add(backButton);
            rowsInline.add(lastRow);

            markupInline.setKeyboard(rowsInline);

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("🏃 *Selecciona un sprint para ver sus KPIs:*");
            message.setParseMode("Markdown");
            message.setReplyMarkup(markupInline);

            bot.execute(message);
        } catch (Exception e) {
            logger.error("Error al mostrar selección de sprints para KPIs", e);
            BotHelper.sendMessageToTelegram(chatId, "❌ Error al cargar la lista de sprints.", bot);
            mostrarOpcionesKPIs(chatId, bot);
        }
    }

    /**
     * Muestra el menú inicial de opciones para los KPIs
     * 
     * @param chatId ID del chat
     * @param bot    Bot de Telegram
     */
    private void mostrarOpcionesKPIs(Long chatId, TelegramLongPollingBot bot) {
        try {
            logger.info("Mostrando opciones de KPIs");

            // Crear teclado inline
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

            // Opción para ver KPIs por Usuario
            List<InlineKeyboardButton> row1 = new ArrayList<>();
            InlineKeyboardButton userButton = new InlineKeyboardButton();
            userButton.setText("👤 Por Usuario");
            userButton.setCallbackData("VER_KPIS_USUARIOS");
            row1.add(userButton);
            rowsInline.add(row1);

            // Opción para ver KPIs por Sprint
            List<InlineKeyboardButton> row2 = new ArrayList<>();
            InlineKeyboardButton sprintButton = new InlineKeyboardButton();
            sprintButton.setText("🏃 Por Sprint");
            sprintButton.setCallbackData("VER_KPIS_SPRINTS");
            row2.add(sprintButton);
            rowsInline.add(row2);

            // Botón para volver al menú principal
            List<InlineKeyboardButton> row3 = new ArrayList<>();
            InlineKeyboardButton menuButton = new InlineKeyboardButton();
            menuButton.setText("🔙 Volver al Menú");
            menuButton.setCallbackData("MENU_PRINCIPAL");
            row3.add(menuButton);
            rowsInline.add(row3);

            markupInline.setKeyboard(rowsInline);

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("📊 *Selecciona el tipo de KPIs que deseas ver:*");
            message.setParseMode("Markdown");
            message.setReplyMarkup(markupInline);

            bot.execute(message);
        } catch (Exception e) {
            logger.error("Error al mostrar opciones de KPIs", e);
            BotHelper.sendMessageToTelegram(chatId, "❌ Error al cargar opciones de KPI.", bot);
            MenuBotHelper.showMainMenu(chatId, bot);
        }
    }

    /**
     * Muestra el menú para cambiar el estado de las tareas del usuario
     * 
     * @param chatId     ID del chat
     * @param telegramId ID de Telegram del usuario
     * @param bot        Bot de Telegram
     */
    public void showTaskStateChangeMenu(Long chatId, Long telegramId, TelegramLongPollingBot bot) {
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
                BotHelper.sendMessageToTelegram(chatId, "No tienes tareas pendientes para cambiar estado.", bot);
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
                button.setText("🔄 " + tarea.getTareaNombre());
                button.setCallbackData("CAMBIAR_ESTADO_TAREA_" + tarea.getTareaID());
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
            message.setText("Selecciona la tarea cuyo estado deseas cambiar:");
            message.setReplyMarkup(markupInline);

            bot.execute(message);
        } catch (Exception e) {
            logger.error("Error al mostrar menú de cambiar estado de tareas", e);
            BotHelper.sendMessageToTelegram(chatId, "❌ Error al cargar tus tareas.", bot);
            MenuBotHelper.showMainMenu(chatId, bot);
        }
    }

    /**
     * Muestra las opciones de estados disponibles para una tarea
     * 
     * @param tareaId ID de la tarea
     * @param chatId  ID del chat
     * @param bot     Bot de Telegram
     */
    public void showTaskStateOptions(Long tareaId, Long chatId, TelegramLongPollingBot bot) {
        try {
            ResponseEntity<Tarea> tareaResponse = tareaService.getTareaById(tareaId);
            if (tareaResponse == null || !tareaResponse.getStatusCode().is2xxSuccessful()
                    || tareaResponse.getBody() == null) {
                BotHelper.sendMessageToTelegram(chatId, "❌ No se encontró la tarea.", bot);
                MenuBotHelper.showMainMenu(chatId, bot);
                return;
            }

            Tarea tarea = tareaResponse.getBody();

            // Crear teclado inline
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

            // Opciones de estado
            String[] estados = { "PENDIENTE", "EN_PROCESO", "COMPLETADA" };
            for (String estado : estados) {
                // No mostrar el estado actual como opción
                if ((estado.equals("PENDIENTE") && tarea.getEstadoID() == 1L) ||
                        (estado.equals("EN_PROCESO") && tarea.getEstadoID() == 2L) ||
                        (estado.equals("COMPLETADA") && tarea.getEstadoID() == 3L)) {
                    continue;
                }

                List<InlineKeyboardButton> row = new ArrayList<>();
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(estado.replace("_", " "));
                button.setCallbackData("ACTUALIZAR_ESTADO_" + estado + "_TAREA_" + tareaId);
                row.add(button);
                rowsInline.add(row);
            }

            // Botón para volver
            List<InlineKeyboardButton> lastRow = new ArrayList<>();
            InlineKeyboardButton backButton = new InlineKeyboardButton();
            backButton.setText("⬅️ Volver");
            backButton.setCallbackData("MIS_TAREAS");
            lastRow.add(backButton);
            rowsInline.add(lastRow);

            markupInline.setKeyboard(rowsInline);

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("Tarea: *" + tarea.getTareaNombre() + "*\n" +
                    "Estado actual: *" + getEstadoNombre(tarea.getEstadoID()) + "*\n\n" +
                    "Selecciona el nuevo estado:");
            message.setParseMode("Markdown");
            message.setReplyMarkup(markupInline);

            bot.execute(message);
        } catch (Exception e) {
            logger.error("Error al mostrar opciones de estado", e);
            BotHelper.sendMessageToTelegram(chatId, "❌ Error al cargar opciones de estado.", bot);
            MenuBotHelper.showMainMenu(chatId, bot);
        }
    }

    /**
     * Actualiza el estado de una tarea
     * 
     * @param tareaId       ID de la tarea
     * @param nuevoEstadoId ID del nuevo estado
     * @param chatId        ID del chat
     * @param telegramId    ID de Telegram del usuario
     * @param bot           Bot de Telegram
     */
    public void updateTaskState(Long tareaId, Long nuevoEstadoId, Long chatId, Long telegramId,
            TelegramLongPollingBot bot) {
        try {
            ResponseEntity<Tarea> tareaResponse = tareaService.getTareaById(tareaId);
            if (tareaResponse == null || !tareaResponse.getStatusCode().is2xxSuccessful()
                    || tareaResponse.getBody() == null) {
                BotHelper.sendMessageToTelegram(chatId, "❌ No se encontró la tarea.", bot);
                MenuBotHelper.showMainMenu(chatId, bot);
                return;
            }

            Tarea tarea = tareaResponse.getBody();

            // Si el nuevo estado es "COMPLETADA", también actualizamos el campo completado
            if (nuevoEstadoId == 3L) {
                tarea.setCompletado(1);
                // Para tareas completadas, solicitamos las horas reales
                BotHelper.sendMessageToTelegram(chatId,
                        "⏱️ Por favor, ingresa las horas reales que tomó completar la tarea:", bot);
                completionTasks.put(chatId, tarea);
                awaitingHoursReal.put(chatId, true);
                return;
            } else {
                // Actualizamos solo el estado
                tarea.setEstadoID(nuevoEstadoId);
                tareaService.updateTarea(tarea.getTareaID(), tarea);

                BotHelper.sendMessageToTelegram(chatId,
                        "✅ Estado actualizado exitosamente a *" + getEstadoNombre(nuevoEstadoId) + "*", bot);

                // Volver a mostrar las tareas del usuario
                showUserTasks(chatId, telegramId, bot);
            }
        } catch (Exception e) {
            logger.error("Error al actualizar estado de tarea", e);
            BotHelper.sendMessageToTelegram(chatId, "❌ Error al actualizar estado.", bot);
            MenuBotHelper.showMainMenu(chatId, bot);
        }
    }

    /**
     * Obtiene el nombre del estado a partir de su ID
     * 
     * @param estadoId ID del estado
     * @return Nombre del estado
     */
    private String getEstadoNombre(Long estadoId) {
        switch (estadoId.intValue()) {
            case 1:
                return "PENDIENTE";
            case 2:
                return "EN PROCESO";
            case 3:
                return "COMPLETADA";
            default:
                return "DESCONOCIDO";
        }
    }
}
