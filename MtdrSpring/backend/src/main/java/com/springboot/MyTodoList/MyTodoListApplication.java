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

import com.springboot.MyTodoList.controller.TareaBotController;
import com.springboot.MyTodoList.service.TareaService;

@SpringBootApplication
public class MyTodoListApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MyTodoListApplication.class);

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
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new TareaBotController(telegramBotToken, botName, tareaService));
            logger.info("Bot de Tareas registrado exitosamente!");
        } catch (TelegramApiException e) {
            logger.error("Error al registrar el bot", e);
        }
    }
}
