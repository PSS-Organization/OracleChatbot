import com.springboot.MyTodoList.controller.bot.TareaBotController;
import com.springboot.MyTodoList.model.Tarea;
import com.springboot.MyTodoList.model.Usuario;
import com.springboot.MyTodoList.service.TareaService;
import com.springboot.MyTodoList.service.UsuarioService;
import com.springboot.MyTodoList.service.SprintService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TareaBotControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private TareaService tareaService;

    @Mock
    private SprintService sprintService;

    @Mock
    private TelegramLongPollingBot bot; // ✅ Ahora correctamente definido

    private TareaBotController tareaBotController;

    private final Long chatId = 1234L;
    private final Long telegramId = 5678L;

    @BeforeEach
    void setUp() {
        tareaBotController = new TareaBotController(tareaService, usuarioService, sprintService);
    }

    @Test
    void testShowUserTasksSendsExpectedMessage() throws Exception {
        // 🔹 Simular un usuario existente
        Usuario usuario = new Usuario();
        usuario.setUsuarioID(1L);
        usuario.setNombre("Diego");

        when(usuarioService.getUsuarioByTelegramId(telegramId)).thenReturn(Optional.of(usuario));

        // 🔹 Simular una tarea que será retornada
        Tarea tarea = new Tarea();
        tarea.setTareaNombre("Demo");
        tarea.setDescripcion("Preparar presentación final");
        tarea.setFechaEntrega(OffsetDateTime.parse("2025-04-30T10:00:00Z"));
        tarea.setCompletado(0);

        when(tareaService.getTareasByUsuario(usuario.getUsuarioID())).thenReturn(Arrays.asList(tarea));

        // 🔹 Ejecutar el método a probar
        tareaBotController.showUserTasks(chatId, telegramId, bot);

        // 🔹 Verificar que se haya enviado un mensaje al chat
        verify(bot, atLeastOnce()).execute(argThat((SendMessage msg) ->
            msg.getChatId().equals(chatId.toString()) &&
            msg.getText().contains("Demo") &&
            msg.getText().contains("Preparar presentación final")
        ));
    }
}
