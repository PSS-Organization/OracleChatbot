package com.springboot.MyTodoList.controller.bot;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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

        // Rest of your existing message handling code...
        logger.info("Mensaje recibido: " + messageText);

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
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(
                "👋 Hola, para usar este bot necesitas compartir tu número de teléfono para verificar tu acceso.");

        // Crear teclado con botón para compartir contacto
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        KeyboardButton button = new KeyboardButton("Compartir mi número");
        button.setRequestContact(true);
        row.add(button);

        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Error al solicitar el número de teléfono: " + e.getMessage());
        }
    }

    private void handleRegistrationProcess(String messageText, Long chatId, Long telegramId) {
        RegistroManager.procesarRegistro(chatId, messageText, telegramId, this);
    }
}
