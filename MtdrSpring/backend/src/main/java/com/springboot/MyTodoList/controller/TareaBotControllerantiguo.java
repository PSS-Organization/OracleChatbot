// package com.springboot.MyTodoList.controller;

// import java.time.LocalDate;
// import java.time.OffsetDateTime;
// import java.time.ZoneOffset;
// import java.time.format.DateTimeFormatter;
// import java.time.format.DateTimeParseException;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.stream.Collectors;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.http.ResponseEntity;
// import org.telegram.telegrambots.bots.TelegramLongPollingBot;
// import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
// import org.telegram.telegrambots.meta.api.objects.Update;
// import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
// import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
// import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
// import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

// import com.springboot.MyTodoList.model.Estado;
// import com.springboot.MyTodoList.model.Sprint;
// import com.springboot.MyTodoList.model.Tarea;
// import com.springboot.MyTodoList.model.TareaCreationState;
// import com.springboot.MyTodoList.model.Usuario;
// import com.springboot.MyTodoList.service.TareaService;
// import com.springboot.MyTodoList.util.BotCommands;
// import com.springboot.MyTodoList.util.BotHelper;
// import com.springboot.MyTodoList.util.BotLabels;
// import com.springboot.MyTodoList.util.TareaCreationManager;

// public class TareaBotController extends TelegramLongPollingBot {

//     private static final Logger logger = LoggerFactory.getLogger(TareaBotController.class);
//     private TareaService tareaService;
//     private String botName;

//     public TareaBotController(String botToken, String botName, TareaService tareaService) {
//         super(botToken);
//         this.tareaService = tareaService;
//         this.botName = botName;
//         logger.info("TareaBot iniciado con nombre: " + botName);
//     }

//     @Override
//     public void onUpdateReceived(Update update) {
//         if (update.hasMessage() && update.getMessage().hasText()) {
//             String messageText = update.getMessage().getText();
//             long chatId = update.getMessage().getChatId();

//             // Estado inicial o comando de inicio
//             if (messageText.equals(BotCommands.START_COMMAND.getCommand())
//                     || messageText.equals(BotLabels.SHOW_MAIN_SCREEN.getLabel())) {
//                 showMainMenu(chatId);
//                 return;
//             }

//             // Manejo de acciones principales
//             switch (messageText) {
//                 case "📋 Ver Todas las Tareas":
//                     mostrarListaTareas(chatId, tareaService.getAllTareas(), "📋 *TODAS LAS TAREAS*");
//                     break;
//                 case "➕ Nueva Tarea":
//                     solicitarNuevaTarea(chatId);
//                     break;
//                 case "👤 Mis Tareas":
//                     handleMisTareas(chatId);
//                     break;
//                 case "🏃 Ver por Sprint":
//                     mostrarMenuFiltroSprint(chatId);
//                     break;
//                 case "📊 Ver por Estado":
//                     mostrarMenuFiltroEstado(chatId);
//                     break;
//                 case "❌ Cerrar Menú":
//                     ocultarMenu(chatId);
//                     break;
//                 default:
//                     handleDefaultActions(messageText, chatId);
//                     break;
//             }
//         }
//     }

//     private void handleDefaultActions(String messageText, long chatId) {
//         if (TareaCreationManager.isInCreationProcess(chatId)) {
//             handleTareaCreation(messageText, chatId);
//             return;
//         }

//         // Manejo de acciones sobre tareas específicas
//         if (messageText.contains("-✅ Completar")) {
//             marcarTareaComoCompletada(messageText, chatId);
//         } else if (messageText.contains("-↩️ Deshacer")) {
//             desmarcarTareaComoCompletada(messageText, chatId);
//         } else if (messageText.contains("-❌ Eliminar")) {
//             eliminarTarea(messageText, chatId);
//         } else if (messageText.startsWith("Sprint ")) {
//             // Manejo de selección de sprint
//             String sprintNum = messageText.substring(7);
//             try {
//                 Long sprintId = Long.valueOf(sprintNum);
//                 List<Tarea> tareasSprint = tareaService.getTareasBySprint(sprintId);
//                 mostrarListaTareas(chatId, tareasSprint, "🏃 *TAREAS DEL SPRINT " + sprintNum + "*");
//             } catch (NumberFormatException e) {
//                 logger.error("Error al procesar número de sprint", e);
//             }
//         } else if (messageText.contains("Estado")) {
//             // Manejo de selección de estado
//             handleEstadoSelection(messageText, chatId);
//         } else {
//             // Asumimos que es el nombre de una nueva tarea
//             //crearNuevaTarea(messageText, chatId);
//         }
//     }

//     private void handleMisTareas(long chatId) {
//         // TODO: Implementar lógica para obtener el usuarioID del chat de Telegram
//         Long usuarioID = 1L; // Ejemplo, deberías obtener el ID real del usuario
//         List<Tarea> misTareas = tareaService.getTareasByUsuario(usuarioID);
//         mostrarListaTareas(chatId, misTareas, "👤 *MIS TAREAS*");
//     }

//     private void handleEstadoSelection(String messageText, long chatId) {
//         Long estadoId;
//         switch (messageText) {
//             case "🟡 Pendiente":
//                 estadoId = 1L;
//                 break;
//             case "🟠 En Progreso":
//                 estadoId = 2L;
//                 break;
//             case "🟢 Completado":
//                 estadoId = 3L;
//                 break;
//             case "🔴 Bloqueado":
//                 estadoId = 4L;
//                 break;
//             default:
//                 return;
//         }

//         List<Tarea> tareasEstado = tareaService.getTareasByEstado(estadoId);
//         mostrarListaTareas(chatId, tareasEstado, "📊 *TAREAS EN ESTADO: " + messageText + "*");
//     }

//     private void ocultarMenu(long chatId) {
//         SendMessage message = new SendMessage();
//         message.setChatId(chatId);
//         message.setText("Menú ocultado. Usa /start para volver a mostrarlo.");
//         message.setReplyMarkup(new ReplyKeyboardRemove(true));

//         try {
//             execute(message);
//         } catch (TelegramApiException e) {
//             logger.error("Error al ocultar menú", e);
//         }
//     }

//     private void showMainMenu(long chatId) {
//         SendMessage message = new SendMessage();
//         message.setChatId(chatId);
//         message.setText("🎯 Bienvenido al Gestor de Tareas\n\nSeleccione una opción:");

//         ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
//         List<KeyboardRow> keyboard = new ArrayList<>();

//         // Primera fila - Acciones principales
//         KeyboardRow row1 = new KeyboardRow();
//         row1.add("📋 Ver Todas las Tareas");
//         row1.add("➕ Nueva Tarea");
//         keyboard.add(row1);

//         // Segunda fila - Filtros
//         KeyboardRow row2 = new KeyboardRow();
//         row2.add("👤 Mis Tareas");
//         row2.add("🏃 Ver por Sprint");
//         keyboard.add(row2);

//         // Tercera fila - Estados y más opciones
//         KeyboardRow row3 = new KeyboardRow();
//         row3.add("📊 Ver por Estado");
//         row3.add("❌ Cerrar Menú");
//         keyboard.add(row3);

//         keyboardMarkup.setKeyboard(keyboard);
//         message.setReplyMarkup(keyboardMarkup);

//         try {
//             execute(message);
//         } catch (TelegramApiException e) {
//             logger.error("Error al mostrar menú principal", e);
//         }
//     }

//     private void mostrarListaTareas(long chatId) {
//         List<Tarea> tareas = tareaService.getAllTareas();
//         ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
//         List<KeyboardRow> keyboard = new ArrayList<>();

//         // Botón de menú principal
//         KeyboardRow menuRow = new KeyboardRow();
//         menuRow.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
//         keyboard.add(menuRow);

//         // Botón para agregar tarea
//         KeyboardRow addRow = new KeyboardRow();
//         addRow.add(BotLabels.ADD_NEW_ITEM.getLabel());
//         keyboard.add(addRow);

//         // Tareas pendientes
//         List<Tarea> tareasPendientes = tareas.stream()
//                 .filter(tarea -> tarea.getCompletado() == 0)
//                 .collect(Collectors.toList());

//         for (Tarea tarea : tareasPendientes) {
//             KeyboardRow row = new KeyboardRow();
//             row.add(tarea.getTareaNombre() + " - " + tarea.getDescripcion());
//             row.add(tarea.getTareaID() + BotLabels.DASH.getLabel() + BotLabels.DONE.getLabel());
//             keyboard.add(row);
//         }

//         // Tareas completadas
//         List<Tarea> tareasCompletadas = tareas.stream()
//                 .filter(tarea -> tarea.getCompletado() == 1)
//                 .collect(Collectors.toList());

//         for (Tarea tarea : tareasCompletadas) {
//             KeyboardRow row = new KeyboardRow();
//             row.add(tarea.getTareaNombre() + " (Completada)");
//             row.add(tarea.getTareaID() + BotLabels.DASH.getLabel() + BotLabels.UNDO.getLabel());
//             row.add(tarea.getTareaID() + BotLabels.DASH.getLabel() + BotLabels.DELETE.getLabel());
//             keyboard.add(row);
//         }

//         keyboardMarkup.setKeyboard(keyboard);

//         SendMessage message = new SendMessage();
//         message.setChatId(chatId);
//         message.setText("Lista de Tareas:");
//         message.setReplyMarkup(keyboardMarkup);

//         try {
//             execute(message);
//         } catch (TelegramApiException e) {
//             logger.error("Error al mostrar lista de tareas", e);
//         }
//     }

//     private void mostrarListaTareas(long chatId, List<Tarea> tareas, String titulo) {
//         StringBuilder messageText = new StringBuilder();
//         messageText.append(titulo + "\n\n");

//         // Tareas pendientes
//         List<Tarea> tareasPendientes = tareas.stream()
//                 .filter(tarea -> tarea.getCompletado() == 0)
//                 .collect(Collectors.toList());

//         if (!tareasPendientes.isEmpty()) {
//             messageText.append("📌 *TAREAS PENDIENTES*\n");
//             for (Tarea tarea : tareasPendientes) {
//                 messageText.append(String.format(
//                         "🔸 *%s*\n   📝 %s\n   ⏰ Prioridad: %s\n   👤 Usuario: %d\n   🏃 Sprint: %d\n\n",
//                         tarea.getTareaNombre(),
//                         tarea.getDescripcion(),
//                         tarea.getPrioridad(),
//                         tarea.getUsuarioID(),
//                         tarea.getSprintID()
//                 ));
//             }
//         }

//         // Tareas completadas
//         List<Tarea> tareasCompletadas = tareas.stream()
//                 .filter(tarea -> tarea.getCompletado() == 1)
//                 .collect(Collectors.toList());

//         if (!tareasCompletadas.isEmpty()) {
//             messageText.append("\n✅ *TAREAS COMPLETADAS*\n");
//             for (Tarea tarea : tareasCompletadas) {
//                 messageText.append(String.format(
//                         "✓ %s\n",
//                         tarea.getTareaNombre()
//                 ));
//             }
//         }

//         ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
//         List<KeyboardRow> keyboard = new ArrayList<>();

//         // Botones de navegación
//         KeyboardRow menuRow = new KeyboardRow();
//         menuRow.add("🏠 Menú Principal");
//         keyboard.add(menuRow);

//         // Botones de filtro
//         KeyboardRow filterRow = new KeyboardRow();
//         filterRow.add("👤 Mis Tareas");
//         filterRow.add("🏃 Ver por Sprint");
//         filterRow.add("📊 Ver por Estado");
//         keyboard.add(filterRow);

//         // Acciones para tareas
//         for (Tarea tarea : tareasPendientes) {
//             KeyboardRow row = new KeyboardRow();
//             row.add(tarea.getTareaID() + "-✅ Completar");
//             row.add(tarea.getTareaID() + "-❌ Eliminar");
//             keyboard.add(row);
//         }

//         keyboardMarkup.setKeyboard(keyboard);

//         SendMessage message = new SendMessage();
//         message.setChatId(chatId);
//         message.setText(messageText.toString());
//         message.setParseMode("Markdown");
//         message.setReplyMarkup(keyboardMarkup);

//         try {
//             execute(message);
//         } catch (TelegramApiException e) {
//             logger.error("Error al mostrar tareas", e);
//         }
//     }

//     private void mostrarMenuFiltroSprint(long chatId) {

//         List<Sprint> sprints = tareaService.getAllSprints();
//         if (sprints.isEmpty()) {
//             sendMessage(chatId, "❌ No hay sprints disponibles");
//             return;
//         }

//         SendMessage message = new SendMessage();
//         message.setChatId(chatId);
//         message.setText("🏃 Selecciona el sprint:");

//         ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
//         List<KeyboardRow> keyboard = new ArrayList<>();

//         for (Sprint sprint : sprints) {
//             KeyboardRow row = new KeyboardRow();
//             row.add("Sprint " + sprint.getSprintID());
//             keyboard.add(row);
//         }

//         keyboardMarkup.setKeyboard(keyboard);
//         message.setReplyMarkup(keyboardMarkup);

//         try {
//             execute(message);
//         } catch (TelegramApiException e) {
//             logger.error("Error al mostrar menú de sprints", e);
//         }
//     }

//     private void mostrarMenuFiltroEstado(long chatId) {
//         SendMessage message = new SendMessage();
//         message.setChatId(chatId);
//         message.setText("📊 *Seleccione un Estado:*");
//         message.setParseMode("Markdown");

//         ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
//         List<KeyboardRow> keyboard = new ArrayList<>();

//         // Botón de regreso
//         KeyboardRow menuRow = new KeyboardRow();
//         menuRow.add("🏠 Menú Principal");
//         keyboard.add(menuRow);

//         // Estados predefinidos
//         String[] estados = {"🟡 Pendiente", "🟠 En Progreso", "🟢 Completado", "🔴 Bloqueado"};
//         for (String estado : estados) {
//             KeyboardRow row = new KeyboardRow();
//             row.add(estado);
//             keyboard.add(row);
//         }

//         keyboardMarkup.setKeyboard(keyboard);
//         message.setReplyMarkup(keyboardMarkup);

//         try {
//             execute(message);
//         } catch (TelegramApiException e) {
//             logger.error("Error al mostrar menú de estados", e);
//         }
//     }

//     private void marcarTareaComoCompletada(String messageText, long chatId) {
//         try {
//             Long id = Long.valueOf(messageText.substring(0, messageText.indexOf(BotLabels.DASH.getLabel())));
//             ResponseEntity<Tarea> response = tareaService.getTareaById(id);
//             if (response.getBody() != null) {
//                 Tarea tarea = response.getBody();
//                 tarea.setCompletado(1);
//                 tareaService.updateTarea(id, tarea);
//                 BotHelper.sendMessageToTelegram(chatId, "Tarea marcada como completada", this);
//                 mostrarListaTareas(chatId);
//             }
//         } catch (Exception e) {
//             logger.error("Error al completar tarea", e);
//         }
//     }

//     private void desmarcarTareaComoCompletada(String messageText, long chatId) {
//         try {
//             Long id = Long.valueOf(messageText.substring(0, messageText.indexOf(BotLabels.DASH.getLabel())));
//             ResponseEntity<Tarea> response = tareaService.getTareaById(id);
//             if (response.getBody() != null) {
//                 Tarea tarea = response.getBody();
//                 tarea.setCompletado(0);
//                 tareaService.updateTarea(id, tarea);
//                 BotHelper.sendMessageToTelegram(chatId, "Tarea marcada como pendiente", this);
//                 mostrarListaTareas(chatId);
//             }
//         } catch (Exception e) {
//             logger.error("Error al desmarcar tarea", e);
//         }
//     }

//     private void eliminarTarea(String messageText, long chatId) {
//         try {
//             Long id = Long.valueOf(messageText.substring(0, messageText.indexOf(BotLabels.DASH.getLabel())));
//             if (tareaService.deleteTarea(id)) {
//                 BotHelper.sendMessageToTelegram(chatId, "Tarea eliminada exitosamente", this);
//                 mostrarListaTareas(chatId);
//             }
//         } catch (Exception e) {
//             logger.error("Error al eliminar tarea", e);
//         }
//     }

//     private void solicitarNuevaTarea(long chatId) {
//         TareaCreationManager.startCreation(chatId);
//         SendMessage message = new SendMessage();
//         message.setChatId(chatId);
//         message.setText("📝 Por favor, ingresa el nombre de la tarea:");
//         message.setReplyMarkup(new ReplyKeyboardRemove(true));

//         try {
//             execute(message);
//         } catch (TelegramApiException e) {
//             logger.error("Error al solicitar nueva tarea", e);
//         }
//     }

//     private void handleTareaCreation(String messageText, long chatId) {
//         TareaCreationState state = TareaCreationManager.getState(chatId);

//         switch (state.getCurrentField()) {
//             case "NOMBRE":
//                 state.getTarea().setTareaNombre(messageText);
//                 state.setCurrentField("DESCRIPCION");
//                 sendMessage(chatId, "📝 Ingresa la descripción de la tarea:");
//                 break;

//             case "DESCRIPCION":
//                 state.getTarea().setDescripcion(messageText);
//                 state.setCurrentField("PRIORIDAD");
//                 mostrarOpcionesPrioridad(chatId);
//                 break;

//             case "PRIORIDAD":
//                 if (!messageText.matches("BAJA|MEDIA|ALTA")) {
//                     sendMessage(chatId, "❌ Por favor, selecciona una prioridad válida (BAJA, MEDIA, ALTA)");
//                     return;
//                 }
//                 state.getTarea().setPrioridad(messageText);
//                 state.setCurrentField("USUARIO");
//                 mostrarOpcionesUsuarios(chatId);
//                 break;

//             case "USUARIO":
//                 try {
//                     // Validar que el mensaje tenga el formato correcto
//                     if (!messageText.contains("-")) {
//                     sendMessage(chatId, "❌ Por favor, selecciona un sprint válido de la lista");
//                     return;
//                     }
//                     Long usuarioId = Long.parseLong(messageText.split("-")[0].trim());
//                     state.getTarea().setUsuarioID(usuarioId);
//                     state.setCurrentField("SPRINT");
//                     mostrarOpcionesSprints(chatId);
//                 } catch (NumberFormatException e) {
//                     sendMessage(chatId, "❌ Por favor, selecciona un usuario válido de la lista");
//                 }
//                 break;

//             case "SPRINT":
//                 try {
//                     // Validar que el mensaje tenga el formato correcto
//                     if (!messageText.contains("-")) {
//                         sendMessage(chatId, "❌ Por favor, selecciona un sprint válido de la lista");
//                         return;
//                     }

//                     Long sprintId = Long.parseLong(messageText.split("-")[0].trim());
//                     state.getTarea().setSprintID(sprintId);
//                     state.setCurrentField("ESTADO");
//                     mostrarOpcionesEstados(chatId);
//                 } catch (NumberFormatException e) {
//                     logger.error("Error al procesar ID del sprint: " + messageText, e);
//                     sendMessage(chatId, "❌ Por favor, selecciona un sprint válido de la lista");
//                 }
//                 break;

//             case "ESTADO":
//                 try {
//                     if (!messageText.contains("-")) {
//                         sendMessage(chatId, "❌ Por favor, selecciona un estado válido de la lista");
//                         return;
//                     }
//                     Long estadoId = Long.parseLong(messageText.split("-")[0].trim());
//                     state.getTarea().setEstadoID(estadoId);
//                     state.setCurrentField("HORAS_ESTIMADAS");
//                     sendMessage(chatId, "⏱️ Ingresa las horas estimadas (número entero):");
//                 } catch (NumberFormatException e) {
//                     logger.error("Error al procesar ID del estado: " + messageText, e);
//                     sendMessage(chatId, "❌ Por favor, selecciona un estado válido de la lista");
//                 }
//                 break;

//             case "HORAS_ESTIMADAS":
//                 try {
//                     int horas = Integer.parseInt(messageText.trim());
//                     if (horas <= 0) {
//                         sendMessage(chatId, "❌ Las horas estimadas deben ser mayores a 0");
//                         return;
//                     }
//                     state.getTarea().setHorasEstimadas(horas);
//                     state.setCurrentField("FECHA_ENTREGA");
//                     sendMessage(chatId, "📅 Ingresa la fecha de entrega (formato: DD/MM/YYYY):");
//                 } catch (NumberFormatException e) {
//                     sendMessage(chatId, "❌ Por favor, ingresa solo números para las horas estimadas");
//                 }
//                 break;

//             case "FECHA_ENTREGA":
//                 try {
//                     DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//                     LocalDate fecha = LocalDate.parse(messageText.trim(), formatter);
//                     LocalDate hoy = LocalDate.now();

//                     if (fecha.isBefore(hoy)) {
//                         sendMessage(chatId, "❌ La fecha de entrega no puede ser anterior a hoy");
//                         return;
//                     }

//                     state.getTarea().setFechaEntrega(fecha.atStartOfDay().atOffset(ZoneOffset.UTC));
//                     finalizarCreacionTarea(state);
//                 } catch (DateTimeParseException e) {
//                     sendMessage(chatId, "❌ Formato de fecha inválido. Usa DD/MM/YYYY (ejemplo: 31/12/2025)");
//                 }
//                 break;
//         }
//     }

//     private void mostrarOpcionesPrioridad(long chatId) {
//         SendMessage message = new SendMessage();
//         message.setChatId(chatId);
//         message.setText("⚡ Selecciona la prioridad:");

//         ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
//         List<KeyboardRow> keyboard = new ArrayList<>();

//         String[] prioridades = {"BAJA", "MEDIA", "ALTA"};
//         for (String prioridad : prioridades) {
//             KeyboardRow row = new KeyboardRow();
//             row.add(prioridad);
//             keyboard.add(row);
//         }

//         keyboardMarkup.setKeyboard(keyboard);
//         message.setReplyMarkup(keyboardMarkup);

//         try {
//             execute(message);
//         } catch (TelegramApiException e) {
//             logger.error("Error al mostrar opciones de prioridad", e);
//         }
//     }

//     private void mostrarOpcionesUsuarios(long chatId) {
//         List<Usuario> usuarios = tareaService.getAllUsuarios();
//         SendMessage message = new SendMessage();
//         message.setChatId(chatId);
//         message.setText("👤 Selecciona el usuario asignado:");

//         ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
//         List<KeyboardRow> keyboard = new ArrayList<>();

//         for (Usuario usuario : usuarios) {
//             KeyboardRow row = new KeyboardRow();
//             row.add(usuario.getUsuarioID() + " - " + usuario.getNombre());
//             keyboard.add(row);
//         }

//         keyboardMarkup.setKeyboard(keyboard);
//         message.setReplyMarkup(keyboardMarkup);

//         try {
//             execute(message);
//         } catch (TelegramApiException e) {
//             logger.error("Error al mostrar opciones de usuarios", e);
//         }
//     }

//     private void mostrarOpcionesSprints(long chatId) {
//         List<Sprint> sprints = tareaService.getAllSprints();
//         if (sprints.isEmpty()) {
//             sendMessage(chatId, "❌ No hay sprints disponibles");
//             return;
//         }

//         SendMessage message = new SendMessage();
//         message.setChatId(chatId);
//         message.setText("🏃 Selecciona el sprint:");

//         ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
//         List<KeyboardRow> keyboard = new ArrayList<>();

//         // Agregar cada sprint como un botón
//         for (Sprint sprint : sprints) {
//             KeyboardRow row = new KeyboardRow();
//             String buttonText = sprint.getSprintID() + " - Sprint " + sprint.getNombreSprint();
//             row.add(buttonText);
//             keyboard.add(row);
//         }

//         // Agregar botón para cancelar
//         KeyboardRow cancelRow = new KeyboardRow();
//         cancelRow.add("❌ Cancelar");
//         keyboard.add(cancelRow);

//         keyboardMarkup.setKeyboard(keyboard);
//         keyboardMarkup.setResizeKeyboard(true);
//         keyboardMarkup.setOneTimeKeyboard(true);
//         message.setReplyMarkup(keyboardMarkup);

//         try {
//             execute(message);
//         } catch (TelegramApiException e) {
//             logger.error("Error al mostrar opciones de sprints", e);
//         }
//     }

//     private void mostrarOpcionesEstados(long chatId) {
//         List<Estado> estados = tareaService.getAllEstados();
//         SendMessage message = new SendMessage();
//         message.setChatId(chatId);
//         message.setText("📊 Selecciona el estado inicial:");

//         ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
//         List<KeyboardRow> keyboard = new ArrayList<>();

//         for (Estado estado : estados) {
//             KeyboardRow row = new KeyboardRow();
//             row.add(estado.getId() + " - " + estado.getNombre());
//             keyboard.add(row);
//         }

//         keyboardMarkup.setKeyboard(keyboard);
//         message.setReplyMarkup(keyboardMarkup);

//         try {
//             execute(message);
//         } catch (TelegramApiException e) {
//             logger.error("Error al mostrar opciones de estados", e);
//         }
//     }

//     private void finalizarCreacionTarea(TareaCreationState state) {
//         try {
//             Tarea nuevaTarea = state.getTarea();
//             nuevaTarea.setFechaCreacion(OffsetDateTime.now());
//             nuevaTarea = tareaService.createTarea(nuevaTarea);

//             sendMessage(state.getChatId(), "✅ Tarea creada exitosamente!\n"
//                     + "📋 Nombre: " + nuevaTarea.getTareaNombre() + "\n"
//                     + "📝 Descripción: " + nuevaTarea.getDescripcion() + "\n"
//                     + "⚡ Prioridad: " + nuevaTarea.getPrioridad() + "\n"
//                     + "⏱️ Horas estimadas: " + nuevaTarea.getHorasEstimadas() + "\n"
//                     + "📅 Fecha entrega: " + nuevaTarea.getFechaEntrega().toLocalDate());

//             showMainMenu(state.getChatId());
//         } catch (Exception e) {
//             logger.error("Error al crear tarea", e);
//             sendMessage(state.getChatId(), "❌ Error al crear la tarea: " + e.getMessage());
//         } finally {
//             TareaCreationManager.clearState(state.getChatId());
//         }
//     }

//     private void sendMessage(long chatId, String text) {
//         SendMessage message = new SendMessage();
//         message.setChatId(chatId);
//         message.setText(text);
//         try {
//             execute(message);
//         } catch (TelegramApiException e) {
//             logger.error("Error al enviar mensaje", e);
//         }
//     }

//     @Override
//     public String getBotUsername() {
//         return botName;
//     }
// }
