package com.springboot.MyTodoList.controller.bot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.model.Tarea;
import com.springboot.MyTodoList.model.Usuario;
import com.springboot.MyTodoList.service.SprintService;
import com.springboot.MyTodoList.service.TareaService;
import com.springboot.MyTodoList.service.UsuarioService;
import com.springboot.MyTodoList.util.BotHelper;
import com.springboot.MyTodoList.util.BotLabels;
import com.springboot.MyTodoList.util.TareaCreationManager;

public class SprintBotController {

    private static final Logger logger = LoggerFactory.getLogger(SprintBotController.class);

    private final SprintService sprintService;
    private final TareaService tareaService;
    private final UsuarioService usuarioService;

    public SprintBotController(SprintService sprintService, TareaService tareaService, UsuarioService usuarioService) {
        this.sprintService = sprintService;
        this.tareaService = tareaService;
        this.usuarioService = usuarioService;
    }

    public boolean canHandle(String messageText) {
        return messageText.equals(BotLabels.VIEW_BY_SPRINT.getLabel()) || messageText.startsWith("Sprint ");
    }

    public void handleMessage(String messageText, Long chatId, TelegramLongPollingBot bot) {
        logger.info("SprintBotController.handleMessage recibido: '" + messageText + "' para chatId: " + chatId);

        // Siempre verificar y limpiar cualquier estado de creación de tareas pendiente
        if (TareaCreationManager.isInCreationProcess(chatId)) {
            logger.warn("Limpiando estado de creación de tarea pendiente para chatId: " + chatId);
            TareaCreationManager.clearState(chatId);
        }

        if (messageText.equals(BotLabels.VIEW_BY_SPRINT.getLabel())) {
            mostrarListaSprints(chatId, bot);
        } else if (messageText.startsWith("Sprint ")) {
            try {
                Long sprintId = Long.parseLong(messageText.split(" ")[1]);
                mostrarTareasPorSprint(sprintId, chatId, bot);
            } catch (NumberFormatException e) {
                logger.error("Error al parsear ID de sprint: " + e.getMessage());
                BotHelper.sendMessageToTelegram(chatId, "❌ Sprint inválido. Usa /start para volver al menú principal.",
                        bot);
            }
        }
    }

    private void mostrarListaSprints(Long chatId, TelegramLongPollingBot bot) {
        try {
            logger.info("Mostrando lista de sprints con botones inline");
            List<Sprint> sprints = sprintService.getAllSprints();

            if (sprints == null || sprints.isEmpty()) {
                BotHelper.sendMessageToTelegram(chatId, "No hay sprints disponibles.", bot);
                return;
            }

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("🏃 *Selecciona un Sprint para ver sus tareas:*");
            message.setParseMode("Markdown");

            // Crear teclado inline
            org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup markupInline = new org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup();
            List<List<org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton>> rowsInline = new ArrayList<>();

            // Agregar botones para cada sprint
            for (Sprint sprint : sprints) {
                List<org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton> row = new ArrayList<>();
                org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton button = new org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton();
                button.setText("Sprint " + sprint.getSprintID() + ": " + sprint.getNombreSprint());
                button.setCallbackData("SPRINT_VER_" + sprint.getSprintID());
                row.add(button);
                rowsInline.add(row);
            }

            // Agregar botón para volver al menú principal
            List<org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton> menuRow = new ArrayList<>();
            org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton menuButton = new org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton();
            menuButton.setText("🔙 Volver al Menú");
            menuButton.setCallbackData("MENU_PRINCIPAL");
            menuRow.add(menuButton);
            rowsInline.add(menuRow);

            markupInline.setKeyboard(rowsInline);
            message.setReplyMarkup(markupInline);

            bot.execute(message);
            logger.info("Lista de sprints enviada exitosamente con botones inline");
        } catch (Exception e) {
            logger.error("Error al mostrar lista de sprints con botones inline", e);
            BotHelper.sendMessageToTelegram(chatId, "❌ Error al obtener la lista de sprints.", bot);
        }
    }

    public void mostrarTareasPorSprint(Long sprintId, Long chatId, TelegramLongPollingBot bot) {
        try {
            logger.info("Mostrando tareas para el Sprint ID: " + sprintId);
            List<Tarea> tareas = tareaService.getTareasBySprint(sprintId);

            if (tareas == null || tareas.isEmpty()) {
                logger.info("No hay tareas para el Sprint ID: " + sprintId);
                BotHelper.sendMessageToTelegram(chatId, "No hay tareas en este sprint.", bot);

                // Añadir botón para volver a la lista de sprints
                SendMessage backMessage = new SendMessage();
                backMessage.setChatId(chatId);
                backMessage.setText("Selecciona una opción:");

                org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup markupInline = new org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup();
                List<List<org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton>> rowsInline = new ArrayList<>();

                List<org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton> row1 = new ArrayList<>();
                org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton sprintsButton = new org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton();
                sprintsButton.setText("🏃 Ver otros Sprints");
                sprintsButton.setCallbackData("VER_POR_SPRINT");
                row1.add(sprintsButton);
                rowsInline.add(row1);

                List<org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton> row2 = new ArrayList<>();
                org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton menuButton = new org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton();
                menuButton.setText("🔙 Volver al Menú");
                menuButton.setCallbackData("MENU_PRINCIPAL");
                row2.add(menuButton);
                rowsInline.add(row2);

                markupInline.setKeyboard(rowsInline);
                backMessage.setReplyMarkup(markupInline);

                bot.execute(backMessage);
                return;
            }

            // Intentar obtener el nombre del sprint
            String nombreSprint = "Sprint " + sprintId;
            try {
                Optional<Sprint> sprintOpt = sprintService.getSprintById(sprintId);
                if (sprintOpt.isPresent()) {
                    nombreSprint = sprintOpt.get().getNombreSprint();
                }
            } catch (Exception e) {
                logger.warn("No se pudo obtener el nombre del sprint", e);
            }

            StringBuilder messageText = new StringBuilder();
            messageText.append("🏃 *TAREAS EN " + nombreSprint + "*\n\n");

            for (Tarea tarea : tareas) {
                String estado = "";
                if (tarea.getEstadoID() != null) {
                    switch (tarea.getEstadoID().intValue()) {
                        case 1:
                            estado = "⏳ PENDIENTE";
                            break;
                        case 2:
                            estado = "🔄 EN PROCESO";
                            break;
                        case 3:
                            estado = "✅ COMPLETADA";
                            break;
                        default:
                            estado = "⏳ PENDIENTE";
                            break;
                    }
                }

                messageText.append("🔸 *").append(tarea.getTareaNombre()).append("*\n")
                        .append("📝 ").append(tarea.getDescripcion()).append("\n");

                // Añadir información del usuario asignado
                if (tarea.getUsuarioID() != null) {
                    try {
                        var usuarioResponse = usuarioService.getUsuarioById(tarea.getUsuarioID());
                        if (usuarioResponse != null && usuarioResponse.getStatusCode().is2xxSuccessful()
                                && usuarioResponse.getBody() != null) {
                            Usuario usuario = usuarioResponse.getBody();
                            messageText.append("👤 Asignada a: ").append(usuario.getNombre()).append("\n");
                        } else {
                            messageText.append("👤 Asignada a: Usuario desconocido\n");
                        }
                    } catch (Exception e) {
                        logger.warn(
                                "No se pudo obtener la información del usuario para la tarea: " + tarea.getTareaID(),
                                e);
                        messageText.append("👤 Asignada a: No disponible\n");
                    }
                } else {
                    messageText.append("👤 Asignada a: Sin asignar\n");
                }

                if (tarea.getFechaEntrega() != null) {
                    messageText.append("📅 Fecha entrega: ").append(
                            tarea.getFechaEntrega().toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                            .append("\n");
                }

                messageText.append("⏱️ Horas estimadas: ").append(tarea.getHorasEstimadas()).append("\n");

                // Mostrar horas reales si están disponibles
                if (tarea.getHorasReales() != null && tarea.getHorasReales() > 0) {
                    messageText.append("⏱️ Horas reales: ").append(tarea.getHorasReales()).append("\n");
                }

                // Mostrar prioridad si está disponible
                if (tarea.getPrioridad() != null && !tarea.getPrioridad().isEmpty()) {
                    String prioridadIcon = "📌 ";
                    switch (tarea.getPrioridad()) {
                        case "ALTA":
                            prioridadIcon = "⬆️ ";
                            break;
                        case "MEDIA":
                            prioridadIcon = "↔️ ";
                            break;
                        case "BAJA":
                            prioridadIcon = "⬇️ ";
                            break;
                    }
                    messageText.append(prioridadIcon).append("Prioridad: ").append(tarea.getPrioridad()).append("\n");
                }

                messageText.append("Estado: ").append(estado).append("\n\n");
            }

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(messageText.toString());
            message.setParseMode("Markdown");

            // Añadir botones para navegación
            org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup markupInline = new org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup();
            List<List<org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton>> rowsInline = new ArrayList<>();

            List<org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton> row1 = new ArrayList<>();
            org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton sprintsButton = new org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton();
            sprintsButton.setText("🏃 Ver otros Sprints");
            sprintsButton.setCallbackData("VER_POR_SPRINT");
            row1.add(sprintsButton);
            rowsInline.add(row1);

            List<org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton> row2 = new ArrayList<>();
            org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton menuButton = new org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton();
            menuButton.setText("🔙 Volver al Menú");
            menuButton.setCallbackData("MENU_PRINCIPAL");
            row2.add(menuButton);
            rowsInline.add(row2);

            markupInline.setKeyboard(rowsInline);
            message.setReplyMarkup(markupInline);

            bot.execute(message);
            logger.info("Tareas del Sprint " + sprintId + " mostradas correctamente");
        } catch (Exception e) {
            logger.error("Error al mostrar tareas por sprint", e);
            BotHelper.sendMessageToTelegram(chatId, "❌ Error al mostrar tareas del sprint.", bot);
        }
    }
}
