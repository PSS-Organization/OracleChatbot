package com.springboot.MyTodoList.controller.bot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import com.springboot.MyTodoList.model.Tarea;
import com.springboot.MyTodoList.model.Usuario;
import com.springboot.MyTodoList.service.TareaService;
import com.springboot.MyTodoList.service.UsuarioService;
import com.springboot.MyTodoList.util.BotHelper;
import com.springboot.MyTodoList.util.BotLabels;

public class UsuarioBotController {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioBotController.class);
    private final TareaService tareaService;
    private final UsuarioService usuarioService;


    public UsuarioBotController(TareaService tareaService, UsuarioService usuarioService) {
        this.tareaService = tareaService;
        this.usuarioService = usuarioService;
    }

    public boolean canHandle(String messageText) {
        return messageText.equals(BotLabels.VIEW_MY_TASKS.getLabel());
    }
    
    public void handleMessage(String messageText, Long chatId, Long telegramId, TelegramLongPollingBot bot) {
        try {
            Optional<Usuario> usuarioOpt = usuarioService.getUsuarioByTelegramId(telegramId);
            if (usuarioOpt.isEmpty()) {
                BotHelper.sendMessageToTelegram(chatId, "❌ No se pudo encontrar tu cuenta. Asegúrate de estar registrado.", bot);
                return;
            }
    
            Usuario usuario = usuarioOpt.get(); // 👈 ahora sí tienes acceso a usuario
    
            List<Tarea> tareas = tareaService.getTareasByUsuario(usuario.getUsuarioID());
    
            if (tareas.isEmpty()) {
                BotHelper.sendMessageToTelegram(chatId, "No tienes tareas asignadas.", bot);
                return;
            }
    
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("👤 *MIS TAREAS ASIGNADAS:*").append("\n\n");
            for (Tarea tarea : tareas) {
                messageBuilder.append("🔸 *").append(tarea.getTareaNombre()).append("*\n")
                        .append("📝 ").append(tarea.getDescripcion()).append("\n")
                        .append("📌 Prioridad: ").append(tarea.getPrioridad()).append("\n")
                        .append("📅 Entrega: ").append(tarea.getFechaEntrega()).append("\n")
                        .append("⏱️ Horas Estimadas: ").append(tarea.getHorasEstimadas()).append("\n\n");
            }
    
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(messageBuilder.toString());
            message.setParseMode("Markdown");
    
            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> keyboard = new ArrayList<>();
            KeyboardRow row = new KeyboardRow();
            row.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
            keyboard.add(row);
            keyboardMarkup.setKeyboard(keyboard);
            message.setReplyMarkup(keyboardMarkup);
    
            bot.execute(message);
    
        } catch (Exception e) {
            logger.error("Error al recuperar tareas del usuario", e);
            BotHelper.sendMessageToTelegram(chatId, "❌ Error al obtener tus tareas.", bot);
        }
    }
    
 
    
}
