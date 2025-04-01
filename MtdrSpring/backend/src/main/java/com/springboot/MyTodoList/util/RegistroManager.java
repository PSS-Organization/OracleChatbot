package com.springboot.MyTodoList.util;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import com.springboot.MyTodoList.controller.bot.MenuBotHelper;
import com.springboot.MyTodoList.model.Usuario;
import com.springboot.MyTodoList.service.UsuarioService;

@Component
public class RegistroManager {

    @Autowired
    private UsuarioService usuarioService;

    private static UsuarioService usuarioServiceStatic;

    @PostConstruct
    private void init() {
        usuarioServiceStatic = usuarioService;
    }

    private static Map<Long, RegistroState> registroStates = new HashMap<>();
    private static Map<Long, DatosRegistro> datosRegistros = new HashMap<>();

    public static void iniciarRegistro(Long chatId, Long telegramId) {
        registroStates.put(chatId, RegistroState.NOMBRE);
        datosRegistros.put(chatId, new DatosRegistro(telegramId));
    }

    public static boolean estaEnRegistro(Long chatId) {
        return registroStates.containsKey(chatId);
    }

    public static void procesarRegistro(Long chatId, String messageText, Long telegramId, TelegramLongPollingBot bot) {
        DatosRegistro datos = datosRegistros.get(chatId);
        RegistroState estado = registroStates.get(chatId);

        switch (estado) {
            case NOMBRE:
                datos.setNombre(messageText);
                registroStates.put(chatId, RegistroState.TELEFONO);
                BotHelper.sendMessageToTelegram(chatId, "📱 Por favor, ingresa tu número de teléfono:", bot);
                break;

            case TELEFONO:
                datos.setTelefono(messageText);
                registroStates.put(chatId, RegistroState.CORREO);
                BotHelper.sendMessageToTelegram(chatId, "📧 Por favor, ingresa tu correo electrónico:", bot);
                break;

            case CORREO:
                if (!validarCorreo(messageText)) {
                    BotHelper.sendMessageToTelegram(chatId, "❌ Correo inválido. Por favor, ingresa un correo válido:", bot);
                    return;
                }
                datos.setCorreo(messageText);
                registroStates.put(chatId, RegistroState.CONTRASENA);
                BotHelper.sendMessageToTelegram(chatId, "🔑 Por favor, ingresa una contraseña:", bot);
                break;

            case CONTRASENA:
                datos.setContrasena(messageText);
                completarRegistro(chatId, datos, bot);
                break;
        }
    }

    private static boolean validarCorreo(String correo) {
        return correo.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private static void completarRegistro(Long chatId, DatosRegistro datos, TelegramLongPollingBot bot) {
        try {
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setNombre(datos.getNombre());
            nuevoUsuario.setTelefono(datos.getTelefono());
            nuevoUsuario.setCorreo(datos.getCorreo());
            nuevoUsuario.setContrasena(datos.getContrasena());
            nuevoUsuario.setTelegramID(datos.getTelegramId());
            nuevoUsuario.setRolUsuario("USER");
            nuevoUsuario.setEsAdmin(false);

            usuarioServiceStatic.createUsuario(nuevoUsuario);

            BotHelper.sendMessageToTelegram(chatId,
                    "✅ ¡Registro completado exitosamente!\n"
                    + "Bienvenido " + datos.getNombre(), bot);

            registroStates.remove(chatId);
            datosRegistros.remove(chatId);
            MenuBotHelper.showMainMenu(chatId, bot);
        } catch (Exception e) {
            BotHelper.sendMessageToTelegram(chatId,
                    "❌ Error al crear usuario. Por favor, intenta nuevamente.", bot);
        }
    }
}

enum RegistroState {
    NOMBRE, TELEFONO, CORREO, CONTRASENA
}

class DatosRegistro {

    private Long telegramId;
    private String nombre;
    private String telefono;
    private String correo;
    private String contrasena;

    public DatosRegistro(Long telegramId) {
        this.telegramId = telegramId;
    }

    public Long getTelegramId() {
        return telegramId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
