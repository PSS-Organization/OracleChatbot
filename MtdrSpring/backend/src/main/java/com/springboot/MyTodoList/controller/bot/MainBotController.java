
package com.springboot.MyTodoList.controller.bot;


import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.springboot.MyTodoList.model.Usuario;
import com.springboot.MyTodoList.service.UsuarioService;
import com.springboot.MyTodoList.util.BotCommands;
import com.springboot.MyTodoList.util.BotHelper;
import com.springboot.MyTodoList.util.BotLabels;
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
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        String messageText = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        Long telegramId = update.getMessage().getFrom().getId(); // 👈 este


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

            BotHelper.sendMessageToTelegram(chatId, "✅ Tu cuenta ha sido vinculada exitosamente. ¡Ya puedes usar el bot!", this);
            MenuBotHelper.showMainMenu(chatId, this);
        } else {
            BotHelper.sendMessageToTelegram(chatId, "❌ No encontramos un usuario con ese número. Intenta nuevamente o contacta a soporte.", this);
        }
        return;
    }

    //🔍 Paso 2: Buscar usuario por telegramId
        Optional<Usuario> usuarioOpt = usuarioService.getUsuarioByTelegramId(telegramId);
        if (usuarioOpt.isEmpty()) {
            BotHelper.sendMessageToTelegram(chatId, "👋 Hola, aún no estás vinculado. Por favor, envía tu número de teléfono para identificarte.", this);
            VinculacionManager.iniciarProceso(chatId); // 👈 aquí lo agregas
            return;
        }

        Usuario usuario = usuarioOpt.get(); // ✅ este usuario ya está vinculado

        // 🟢 Comando de inicio
        if (messageText.equals(BotCommands.START_COMMAND.getCommand()) ||
            messageText.equals(BotLabels.SHOW_MAIN_SCREEN.getLabel())) {
            MenuBotHelper.showMainMenu(chatId, this);
            return;
        }

        // 🧠 Delegar lógica a los subcontroladores
        if (tareaBotController.canHandle(messageText)) {
            tareaBotController.handleMessage(messageText, chatId, this);
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
}
