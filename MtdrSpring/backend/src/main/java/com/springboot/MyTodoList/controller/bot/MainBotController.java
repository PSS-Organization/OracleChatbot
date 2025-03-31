
package com.springboot.MyTodoList.controller.bot;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.springboot.MyTodoList.util.BotCommands;
import com.springboot.MyTodoList.util.BotLabels;

public class MainBotController extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(MainBotController.class);

    private final String botName;
    private final TareaBotController tareaBotController;
    private final UsuarioBotController usuarioBotController;
    private final SprintBotController sprintBotController;

    public MainBotController(String botToken,
                              String botName,
                              TareaBotController tareaBotController,
                              UsuarioBotController usuarioBotController,
                              SprintBotController sprintBotController) {
        super(botToken);
        this.botName = botName;
        this.tareaBotController = tareaBotController;
        this.usuarioBotController = usuarioBotController;
        this.sprintBotController = sprintBotController;
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

        logger.info("Mensaje recibido: " + messageText);

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
            usuarioBotController.handleMessage(messageText, chatId, this);
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
