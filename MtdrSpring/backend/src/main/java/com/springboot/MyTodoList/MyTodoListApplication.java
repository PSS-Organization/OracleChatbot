package com.springboot.MyTodoList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import com.springboot.MyTodoList.controller.bot.MainBotController;
import com.springboot.MyTodoList.controller.bot.SprintBotController;
import com.springboot.MyTodoList.controller.bot.TareaBotController;
import com.springboot.MyTodoList.controller.bot.UsuarioBotController;
import com.springboot.MyTodoList.service.SprintService;
import com.springboot.MyTodoList.service.TareaService;
import com.springboot.MyTodoList.service.UsuarioService;

@SpringBootApplication
public class MyTodoListApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MyTodoListApplication.class);

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private SprintService sprintService;

    @Autowired
    private TareaService tareaService;

    @Value("${telegram.bot.token}")
    private String telegramBotToken;

    @Value("${telegram.bot.name}")
    private String botName;

    public static void main(String[] args) {
        SpringApplication.run(MyTodoListApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            // Crear instancias de los subcontroladores
            TareaBotController tareaBot = new TareaBotController(tareaService, usuarioService, sprintService);
            UsuarioBotController usuarioBot = new UsuarioBotController(tareaService, usuarioService);
            SprintBotController sprintBot = new SprintBotController(sprintService, tareaService, usuarioService);

            // Registrar el controlador principal con todos los sub-controladores
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new MainBotController(
                    telegramBotToken,
                    botName,
                    tareaBot,
                    usuarioBot,
                    sprintBot,
                    usuarioService));

            logger.info("✅ Bot registrado correctamente en Telegram.");
        } catch (TelegramApiException e) {
            logger.error("❌ Error al registrar el bot", e);
        }
    }
}
