import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.springboot.MyTodoList.controller.TareaController;
import com.springboot.MyTodoList.model.Tarea;
import com.springboot.MyTodoList.service.TareaService;

/*

🧪 ¿Qué validamos?

Que se llame al servicio de creación si los datos están completos.

Que NO se llame al servicio si falta nombre o descripción.

Que la respuesta HTTP y el contenido (success, message, tarea) sean los esperados.
 */

@ExtendWith(MockitoExtension.class)
public class TareaControllerTest {

    @Mock
    private TareaService tareaService;

    @InjectMocks
    private TareaController tareaController;

    private Tarea tareaEjemplo;

    @BeforeEach
    void setUp() {
        tareaEjemplo = new Tarea(
            "Prueba de tarea",
            "Esta es una descripción de prueba",
            OffsetDateTime.parse("2025-04-30T10:00:00Z"),
            0,
            "MEDIA",
            5,
            0,
            1L,  // usuarioID
            1L,  // sprintID
            1L   // estadoID
        );
    }

    @Test
    void testCreateTareaExitosa() {
        when(tareaService.createTarea(any(Tarea.class))).thenReturn(tareaEjemplo);

        ResponseEntity<?> response = tareaController.createTarea(tareaEjemplo);

        assertEquals(200, response.getStatusCodeValue());
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertTrue((Boolean) body.get("success"));
        assertEquals("Tarea creada exitosamente", body.get("message"));
        assertEquals(tareaEjemplo, body.get("tarea"));

        verify(tareaService, times(1)).createTarea(any(Tarea.class));
    }

    @Test
    void testCreateTareaSinNombreODescripcion() {
        Tarea tareaInvalida = new Tarea();
        tareaInvalida.setDescripcion("Falta el nombre");

        ResponseEntity<?> response = tareaController.createTarea(tareaInvalida);

        assertEquals(400, response.getStatusCodeValue());
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertFalse((Boolean) body.get("success"));
        assertTrue(body.get("message").toString().contains("obligatorios"));

        verify(tareaService, never()).createTarea(any());
    }

    /*
        📌 ¿Qué estamos validando?
        El endpoint responde con código 200.

        Retorna una lista con tareas relacionadas al sprint especificado.

        La tarea en la lista está marcada como completada (completado == 1).

        El sprintID coincide.
     */
    @Test
    void testGetTareasCompletadasPorSprint() {
        System.out.println("▶️ Iniciando prueba: Obtener tareas completadas por sprint");
        Long sprintId = 1L;

        // Simulamos una tarea completada
        Tarea tareaCompletada = new Tarea();
        tareaCompletada.setTareaNombre("Tarea finalizada");
        tareaCompletada.setDescripcion("Ya está terminada");
        tareaCompletada.setCompletado(1);
        tareaCompletada.setSprintID(sprintId);

        when(tareaService.getTareasBySprint(sprintId)).thenReturn(List.of(tareaCompletada));

        // Ejecutamos el controlador
        ResponseEntity<List<Tarea>> response = tareaController.getTareasBySprint(sprintId);

        assertEquals(200, response.getStatusCodeValue());
        List<Tarea> tareas = response.getBody();

        assertNotNull(tareas);
        assertEquals(1, tareas.size());
        assertEquals(1, tareas.get(0).getCompletado()); // Verificamos que está completada
        assertEquals(sprintId, tareas.get(0).getSprintID());

        verify(tareaService, times(1)).getTareasBySprint(sprintId);
        System.out.println("✅ Prueba exitosa: Se obtuvieron tareas completadas del sprint correctamente");

    }

    @Test
    void testGetTareasCompletadasPorUsuarioYSprint() {
        System.out.println("▶️ Iniciando prueba: Tareas completadas por usuario en sprint");

        Long usuarioId = 1L;
        Long sprintId = 2L;

        Tarea tareaCompletada = new Tarea();
        tareaCompletada.setTareaNombre("Demo completada");
        tareaCompletada.setDescripcion("Finalizada");
        tareaCompletada.setCompletado(1);
        tareaCompletada.setUsuarioID(usuarioId);
        tareaCompletada.setSprintID(sprintId);

        when(tareaService.getTareasCompletadasByUsuarioYSprint(usuarioId, sprintId))
            .thenReturn(List.of(tareaCompletada));

        ResponseEntity<List<Tarea>> response = tareaController.getTareasCompletadasByUsuarioYSprint(usuarioId, sprintId);

        assertEquals(200, response.getStatusCodeValue());
        List<Tarea> tareas = response.getBody();

        assertNotNull(tareas);
        assertEquals(1, tareas.size());
        assertEquals(usuarioId, tareas.get(0).getUsuarioID());
        assertEquals(sprintId, tareas.get(0).getSprintID());
        assertEquals(1, tareas.get(0).getCompletado());

        System.out.println("✅ Prueba exitosa: Se obtuvieron tareas completadas del usuario por sprint correctamente");
    }



    
}
