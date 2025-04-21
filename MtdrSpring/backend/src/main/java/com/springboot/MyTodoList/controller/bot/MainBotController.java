package com.springboot.MyTodoList.controller.bot;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.springboot.MyTodoList.model.Usuario;
import com.springboot.MyTodoList.service.UsuarioService;
import com.springboot.MyTodoList.util.BotCommands;
import com.springboot.MyTodoList.util.BotHelper;
import com.springboot.MyTodoList.util.BotLabels;
import com.springboot.MyTodoList.util.RegistroManager;
import com.springboot.MyTodoList.util.TareaCreationManager;

import java.util.ArrayList;
import java.util.List;

public class MainBotController extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(MainBotController.class);

    private final String botName;
    private final TareaBotController tareaBotController;
    private final UsuarioBotController usuarioBotController;
    private final SprintBotController sprintBotController;
    private final UsuarioService usuarioService;

    public MainBotController(String botToken,
            String botName,
            TareaBotController tareaBotController,
            UsuarioBotController usuarioBotController,
            SprintBotController sprintBotController,
            UsuarioService usuarioService) {
        super(botToken);
        this.botName = botName;
        this.tareaBotController = tareaBotController;
        this.usuarioBotController = usuarioBotController;
        this.sprintBotController = sprintBotController;
        this.usuarioService = usuarioService;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Handle callback queries (from inline buttons)
        if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
            return;
        }

        if (update.hasMessage()) {
            Long chatId = update.getMessage().getChatId();
            Long telegramId = update.getMessage().getFrom().getId();

            // Handle contact sharing
            if (update.getMessage().hasContact()) {
                handleContactShared(update.getMessage().getContact(), chatId, telegramId);
                return;
            }

            // Handle text messages
            if (update.getMessage().hasText()) {
                String messageText = update.getMessage().getText();
                handleTextMessage(messageText, chatId, telegramId);
            }
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        String callbackData = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();
        Long telegramId = callbackQuery.getFrom().getId();

        logger.info("Callback recibido: " + callbackData); // Log the callback data

        // Acknowledge the callback query
        try {
            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(callbackQuery.getId());
            execute(answer);
        } catch (TelegramApiException e) {
            logger.error("Error al responder callback query", e);
        }

        // Process callbacks for viewing tasks by sprint - check this FIRST
        if (callbackData.startsWith("SPRINT_VER_")) {
            try {
                logger.info("Recibido callback para ver tareas de un sprint específico: " + callbackData);
                Long sprintId = Long.parseLong(callbackData.substring("SPRINT_VER_".length()));

                // Asegurarse de que no hay una tarea en creación activa antes de mostrar tareas
                // de sprint
                if (TareaCreationManager.isInCreationProcess(chatId)) {
                    logger.warn("Había una tarea en creación activa, limpiando estado antes de mostrar sprint");
                    TareaCreationManager.clearState(chatId);
                }

                // Ahora es seguro mostrar las tareas del sprint
                sprintBotController.mostrarTareasPorSprint(sprintId, chatId, this);
            } catch (NumberFormatException e) {
                logger.error("Error al parsear ID de sprint: " + e.getMessage());
                BotHelper.sendMessageToTelegram(chatId, "❌ Error al obtener tareas del sprint.", this);
                MenuBotHelper.showMainMenu(chatId, this);
            }
            return;
        }

        // Process callbacks for task creation
        if (callbackData.startsWith("USUARIO_") ||
                (callbackData.startsWith("SPRINT_") && !callbackData.startsWith("SPRINT_VER_")) ||
                callbackData.startsWith("HORAS_") ||
                callbackData.startsWith("FECHA_") ||
                callbackData.equals("FECHA_MANUAL") ||
                callbackData.startsWith("PRIORIDAD_") ||
                callbackData.startsWith("CAL_") ||
                callbackData.equals("CAL_NONE")) {
            tareaBotController.handleCreationCallback(callbackData, chatId, this);
            return;
        }

        // Process callbacks for viewing tasks by user
        if (callbackData.startsWith("VER_TAREAS_USER_")) {
            try {
                Long userId = Long.parseLong(callbackData.substring("VER_TAREAS_USER_".length()));
                tareaBotController.mostrarTareasUsuario(userId, chatId, this);
                return;
            } catch (NumberFormatException e) {
                logger.error("Error al procesar ID de usuario: " + e.getMessage());
                BotHelper.sendMessageToTelegram(chatId, "❌ Error al mostrar tareas del usuario.", this);
                MenuBotHelper.showMainMenu(chatId, this);
                return;
            }
        }

        // Process callbacks for viewing KPIs by user
        if (callbackData.startsWith("VER_KPIS_USER_")) {
            try {
                Long userId = Long.parseLong(callbackData.substring("VER_KPIS_USER_".length()));
                tareaBotController.mostrarKPIsUsuario(userId, chatId, this);
                return;
            } catch (NumberFormatException e) {
                logger.error("Error al procesar ID de usuario para KPIs: " + e.getMessage());
                BotHelper.sendMessageToTelegram(chatId, "❌ Error al mostrar KPIs del usuario.", this);
                MenuBotHelper.showMainMenu(chatId, this);
                return;
            }
        }

        // Process callbacks for viewing KPIs by sprint
        if (callbackData.startsWith("VER_KPIS_SPRINT_")) {
            try {
                Long sprintId = Long.parseLong(callbackData.substring("VER_KPIS_SPRINT_".length()));
                tareaBotController.mostrarKPIsSprint(sprintId, chatId, this);
                return;
            } catch (NumberFormatException e) {
                logger.error("Error al procesar ID de sprint para KPIs: " + e.getMessage());
                BotHelper.sendMessageToTelegram(chatId, "❌ Error al mostrar KPIs del sprint.", this);
                MenuBotHelper.showMainMenu(chatId, this);
                return;
            }
        }

        // Process the callback data for task completion
        if (callbackData.startsWith("COMPLETAR_TAREA_")) {
            // Extract task ID from callback data
            try {
                Long tareaId = Long.parseLong(callbackData.substring("COMPLETAR_TAREA_".length()));
                handleTaskCompletionById(tareaId, chatId, telegramId);
            } catch (NumberFormatException e) {
                logger.error("Error al parsear ID de tarea: " + e.getMessage());
                BotHelper.sendMessageToTelegram(chatId, "❌ Error al completar la tarea.", this);
            }
            return;
        }

        switch (callbackData) {
            case "MENU_PRINCIPAL":
                MenuBotHelper.showMainMenu(chatId, this);
                break;

            // --------------------------------------------------------------------------------------------------------------------------------
            case "VER_TAREAS":
                tareaBotController.handleMessage("👥 Ver Tareas", chatId, telegramId, this);
                break;
            case "NUEVA_TAREA":
                tareaBotController.handleMessage("➕ Nueva Tarea", chatId, telegramId, this);
                break;
            case "MIS_TAREAS":
                tareaBotController.handleMessage("👤 Mis Tareas", chatId, telegramId, this);
                break;
            case "VER_POR_ESTADO":
                tareaBotController.handleMessage("📊 Ver por Estado", chatId, telegramId, this);
                break;
            case "VER_POR_SPRINT":
                sprintBotController.handleMessage("🏃 Ver por Sprint", chatId, this);
                break;
            case "VER_KPIS":
                tareaBotController.handleMessage("📊 KPIs", chatId, telegramId, this);
                break;
            case "VER_KPIS_USUARIOS":
                tareaBotController.mostrarSeleccionUsuariosParaKPIs(chatId, this);
                break;
            case "VER_KPIS_SPRINTS":
                tareaBotController.mostrarSeleccionSprintsParaKPIs(chatId, this);
                break;
            case "COMPLETAR_TAREAS":
                tareaBotController.handleMessage("✅ Completar Tareas", chatId, telegramId, this);
                break;
            case "HISTORIAL_COMPLETADAS":
                tareaBotController.handleMessage("📋 Historial Completadas", chatId, telegramId, this);
                break;
            case "ESTADO_PENDIENTE":
                tareaBotController.handleMessage("ESTADO_PENDIENTE", chatId, telegramId, this);
                break;
            case "ESTADO_EN_PROCESO":
                tareaBotController.handleMessage("ESTADO_EN_PROCESO", chatId, telegramId, this);
                break;
            case "ESTADO_COMPLETADA":
                tareaBotController.handleMessage("ESTADO_COMPLETADA", chatId, telegramId, this);
                break;
            default:
                // Handle other callback data
                logger.info("Callback no reconocido: " + callbackData);
                break;
        }
    }

    private void handleTaskCompletionById(Long tareaId, Long chatId, Long telegramId) {
        BotHelper.sendMessageToTelegram(chatId,
                "⏱️ Por favor, ingresa las horas reales que tomó completar la tarea:", this);

        // Store the task ID and set the state
        tareaBotController.setTaskForCompletion(tareaId, chatId);
    }

    private void handleContactShared(Contact contact, Long chatId, Long telegramId) {
        String phoneNumber = contact.getPhoneNumber();
        Optional<Usuario> usuarioOpt = usuarioService.getUsuarioByTelefono(phoneNumber);

        if (usuarioOpt.isPresent()) {
            // Usuario existe, actualizar TelegramID
            Usuario usuario = usuarioOpt.get();
            usuario.setTelegramID(telegramId);
            usuarioService.updateUsuario(usuario.getUsuarioID(), usuario);
            BotHelper.sendMessageToTelegram(chatId,
                    "✅ ¡Hola " + usuario.getNombre() + "! Te he registrado con telegramID en nuestra base de datos.",
                    this);
            MenuBotHelper.showMainMenu(chatId, this);
        } else {
            // Usuario no existe en la base de datos
            BotHelper.sendMessageToTelegram(chatId,
                    "❌ Lo siento, tu número no está registrado en nuestra base de datos. No tienes acceso.", this);
        }
    }

    private void handleTextMessage(String messageText, Long chatId, Long telegramId) {
        // Check if user exists by telegramID
        Optional<Usuario> usuarioOpt = usuarioService.getUsuarioByTelegramId(telegramId);

        if (usuarioOpt.isEmpty()) {
            // Usuario no tiene telegramID registrado, solicitar número de teléfono
            requestPhoneNumber(chatId);
            return;
        }

        // Existing user flow continues here...
        if (messageText.equals(BotCommands.START_COMMAND.getCommand())
                || messageText.equals(BotLabels.SHOW_MAIN_SCREEN.getLabel())) {
            MenuBotHelper.showMainMenu(chatId, this);
            return;
        }

        // Comando especial de diagnóstico (solo para desarrollo)
        if (messageText.equals("/debug_tasks")) {
            logger.info("Comando de diagnóstico recibido de chatId: " + chatId);
            tareaBotController.debugTareas(chatId, this);
            return;
        }

        // 🔒 Paso 1: Verificar si el usuario está en proceso de enviar su teléfono
        if (VinculacionManager.estaEsperandoTelefono(chatId)) {
            String telefono = messageText.trim();
            System.out.println("☎️ Número recibido desde Telegram: " + telefono);
            System.out.println("🧠 Buscando usuario con número: " + telefono);

            Optional<Usuario> usuarioRegistrado = usuarioService.getUsuarioByTelefono(telefono);

            if (usuarioRegistrado.isPresent()) {
                Usuario usuario = usuarioRegistrado.get();

                usuario.setTelegramID(telegramId);
                usuarioService.updateUsuario(usuario.getUsuarioID(), usuario);

                VinculacionManager.finalizarProceso(chatId);

                BotHelper.sendMessageToTelegram(chatId,
                        "✅ Tu cuenta ha sido vinculada exitosamente. ¡Ya puedes usar el bot!", this);
                MenuBotHelper.showMainMenu(chatId, this);
            } else {
                BotHelper.sendMessageToTelegram(chatId, "❌ No encontramos un usuario con ese número. No tienes acceso.",
                        this);
            }
            return;
        }

        Usuario usuario = usuarioOpt.get(); // ✅ este usuario ya está vinculado

        // 🧠 Delegar lógica a los subcontroladores
        if (tareaBotController.canHandle(messageText, telegramId)) {
            tareaBotController.handleMessage(messageText, chatId, telegramId, this); // Add telegramId parameter
            return;
        }

        if (usuarioBotController.canHandle(messageText)) {
            usuarioBotController.handleMessage(messageText, chatId, telegramId, this); // ✅ ahora recibe telegramId
            return;
        }

        if (sprintBotController.canHandle(messageText)) {
            sprintBotController.handleMessage(messageText, chatId, this);
            return;
        }

        // ❓ Si no se reconoce el mensaje
        tareaBotController.handleFallback(messageText, chatId, this);
    }

    private void requestPhoneNumber(Long chatId) {
        try {
            // First message with explanation
            SendMessage welcomeMessage = new SendMessage();
            welcomeMessage.setChatId(chatId.toString());
            welcomeMessage.setText("👋 *¡Bienvenido al Gestor de Tareas!*\n\n" +
                    "Para poder usar este bot, necesitamos verificar tu acceso. " +
                    "Por favor, comparte tu número de teléfono usando el botón de abajo.");
            welcomeMessage.setParseMode("Markdown");
            execute(welcomeMessage);

            // Second message with contact request button
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText("📱 Presiona el botón para compartir tu contacto:");

            // Crear teclado con botón para compartir contacto
            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> keyboard = new ArrayList<>();
            KeyboardRow row = new KeyboardRow();

            KeyboardButton button = new KeyboardButton("📞 Compartir mi número");
            button.setRequestContact(true);
            row.add(button);

            keyboard.add(row);
            keyboardMarkup.setKeyboard(keyboard);
            keyboardMarkup.setResizeKeyboard(true);
            keyboardMarkup.setOneTimeKeyboard(true);

            message.setReplyMarkup(keyboardMarkup);
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Error al solicitar el número de teléfono: " + e.getMessage());
        }
    }

    private void handleRegistrationProcess(String messageText, Long chatId, Long telegramId) {
        RegistroManager.procesarRegistro(chatId, messageText, telegramId, this);
    }
}
