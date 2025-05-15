
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.springboot.MyTodoList.controller.UsuarioController;
import com.springboot.MyTodoList.model.Usuario;
import com.springboot.MyTodoList.security.JwtService;
import com.springboot.MyTodoList.service.UsuarioService;

/*
🧪 ¿Qué validamos?

1. Operaciones CRUD:
   - Obtener todos los usuarios
   - Obtener usuario por ID
   - Crear nuevo usuario
   - Actualizar usuario
   - Eliminar usuario

2. Operaciones adicionales:
   - Login de usuario
   - Registro de usuario (signup)
   - Verificación de admin
 */
@ExtendWith(MockitoExtension.class)
public class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UsuarioController usuarioController;

    private Usuario usuarioEjemplo;
    private final Long usuarioId = 1L;

    @BeforeEach
    void setUp() {
        usuarioEjemplo = new Usuario();
        usuarioEjemplo.setUsuarioID(usuarioId);
        usuarioEjemplo.setNombre("Usuario Prueba");
        usuarioEjemplo.setCorreo("usuario@test.com");
        usuarioEjemplo.setContrasena("password123");
        usuarioEjemplo.setTelegramID(12345L);
        usuarioEjemplo.setRolUsuario("USER");
        usuarioEjemplo.setEsAdmin(false);
    }

    @Test
    void testGetAllUsuarios() {
        // System.out.println("▶️ Iniciando prueba: Obtener todos los usuarios");

        // Configuramos el mock para devolver una lista de usuarios
        List<Usuario> listaUsuarios = Arrays.asList(usuarioEjemplo);
        when(usuarioService.getAllUsuarios()).thenReturn(listaUsuarios);

        // Ejecutamos el método a probar
        List<Usuario> resultado = usuarioController.getAllUsuarios();

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(usuarioEjemplo.getUsuarioID(), resultado.get(0).getUsuarioID());
        assertEquals(usuarioEjemplo.getNombre(), resultado.get(0).getNombre());

        verify(usuarioService, times(1)).getAllUsuarios();
        // System.out.println("✅ Prueba exitosa: Se obtuvieron todos los usuarios correctamente");
    }

    @Test
    void testGetUsuarioByIdExitoso() {
        // System.out.println("▶️ Iniciando prueba: Obtener usuario por ID");

        // Configuramos el mock
        when(usuarioService.getUsuarioById(usuarioId)).thenReturn(ResponseEntity.ok(usuarioEjemplo));

        // Ejecutamos el método
        ResponseEntity<?> response = usuarioController.getUsuarioById(usuarioId);

        // Verificaciones
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals(usuarioEjemplo, body.get("usuario"));

        verify(usuarioService, times(1)).getUsuarioById(usuarioId);
        // System.out.println("✅ Prueba exitosa: Se obtuvo el usuario por ID correctamente");
    }

    @Test
    void testGetUsuarioByIdNoEncontrado() {
        // System.out.println("▶️ Iniciando prueba: Obtener usuario por ID no existente");

        // Configuramos el mock para simular usuario no encontrado
        when(usuarioService.getUsuarioById(999L)).thenReturn(ResponseEntity.notFound().build());

        // Ejecutamos el método
        ResponseEntity<?> response = usuarioController.getUsuarioById(999L);

        // Verificaciones
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(usuarioService, times(1)).getUsuarioById(999L);
        // System.out.println("✅ Prueba exitosa: Se manejó correctamente el caso de usuario no encontrado");
    }

    @Test
    void testCreateUsuarioExitoso() {
        // System.out.println("▶️ Iniciando prueba: Crear nuevo usuario");

        // Configuramos el mock
        when(usuarioService.createUsuario(any(Usuario.class))).thenReturn(usuarioEjemplo);

        // Ejecutamos el método
        ResponseEntity<?> response = usuarioController.createUsuario(usuarioEjemplo);

        // Verificaciones
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) body.get("success"));
        assertEquals("Usuario creado exitosamente", body.get("message"));
        assertEquals(usuarioEjemplo, body.get("usuario"));

        verify(usuarioService, times(1)).createUsuario(any(Usuario.class));
        // System.out.println("✅ Prueba exitosa: Se creó el usuario correctamente");
    }

    @Test
    void testCreateUsuarioError() {
        // System.out.println("▶️ Iniciando prueba: Error al crear usuario");

        // Configuramos el mock para lanzar excepción
        when(usuarioService.createUsuario(any(Usuario.class)))
                .thenThrow(new RuntimeException("El correo ya está registrado"));

        // Ejecutamos el método
        ResponseEntity<?> response = usuarioController.createUsuario(usuarioEjemplo);

        // Verificaciones
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) body.get("success"));
        assertEquals("El correo ya está registrado", body.get("message"));

        verify(usuarioService, times(1)).createUsuario(any(Usuario.class));
        // System.out.println("✅ Prueba exitosa: Se manejó correctamente el error al crear usuario");
    }

    @Test
    void testIsAdmin() {
        // System.out.println("▶️ Iniciando prueba: Verificar si usuario es admin");

        // Configuramos el mock
        when(usuarioService.isUsuarioAdmin(usuarioId)).thenReturn(true);

        // Ejecutamos el método
        ResponseEntity<Map<String, Object>> response = usuarioController.isAdmin(usuarioId);

        // Verificaciones
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(usuarioId, body.get("usuarioId"));
        assertTrue((Boolean) body.get("isAdmin"));

        verify(usuarioService, times(1)).isUsuarioAdmin(usuarioId);
        // System.out.println("✅ Prueba exitosa: Se verificó correctamente si el usuario es admin");
    }

    @Test
    void testUpdateUsuarioExitoso() {
        // System.out.println("▶️ Iniciando prueba: Actualizar usuario");

        // Preparamos un usuario actualizado
        Usuario usuarioActualizado = new Usuario();
        usuarioActualizado.setUsuarioID(usuarioId);
        usuarioActualizado.setNombre("Nombre Actualizado");
        usuarioActualizado.setCorreo("actualizado@test.com");

        // Configuramos el mock
        when(usuarioService.updateUsuario(eq(usuarioId), any(Usuario.class))).thenReturn(usuarioActualizado);

        // Ejecutamos el método
        ResponseEntity<?> response = usuarioController.updateUsuario(usuarioId, usuarioActualizado);

        // Verificaciones
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) body.get("success"));
        assertEquals("Usuario actualizado exitosamente", body.get("message"));
        assertEquals(usuarioActualizado, body.get("usuario"));

        verify(usuarioService, times(1)).updateUsuario(eq(usuarioId), any(Usuario.class));
        // System.out.println("✅ Prueba exitosa: Se actualizó el usuario correctamente");
    }

    @Test
    void testUpdateUsuarioNoEncontrado() {
        // System.out.println("▶️ Iniciando prueba: Actualizar usuario no existente");

        // Configuramos el mock para simular usuario no encontrado
        when(usuarioService.updateUsuario(eq(999L), any(Usuario.class))).thenReturn(null);

        // Ejecutamos el método
        ResponseEntity<?> response = usuarioController.updateUsuario(999L, usuarioEjemplo);

        // Verificaciones
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(usuarioService, times(1)).updateUsuario(eq(999L), any(Usuario.class));
        // System.out.println("✅ Prueba exitosa: Se manejó correctamente el caso de actualizar usuario no existente");
    }

    @Test
    void testDeleteUsuarioExitoso() {
        // System.out.println("▶️ Iniciando prueba: Eliminar usuario");

        // Configuramos el mock
        when(usuarioService.deleteUsuario(usuarioId)).thenReturn(true);

        // Ejecutamos el método
        ResponseEntity<Map<String, Object>> response = usuarioController.deleteUsuario(usuarioId);

        // Verificaciones
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertTrue((Boolean) body.get("success"));
        assertEquals("Usuario eliminado exitosamente", body.get("message"));

        verify(usuarioService, times(1)).deleteUsuario(usuarioId);
        // System.out.println("✅ Prueba exitosa: Se eliminó el usuario correctamente");
    }

    @Test
    void testDeleteUsuarioNoEncontrado() {
        // System.out.println("▶️ Iniciando prueba: Eliminar usuario no existente");

        // Configuramos el mock para simular usuario no encontrado
        when(usuarioService.deleteUsuario(999L)).thenReturn(false);

        // Ejecutamos el método
        ResponseEntity<Map<String, Object>> response = usuarioController.deleteUsuario(999L);

        // Verificaciones
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertFalse((Boolean) body.get("success"));
        assertEquals("No se encontró el usuario", body.get("message"));

        verify(usuarioService, times(1)).deleteUsuario(999L);
        // System.out.println("✅ Prueba exitosa: Se manejó correctamente el caso de eliminar usuario no existente");
    }

    @Test
    void testLoginExitoso() {
        // System.out.println("▶️ Iniciando prueba: Login de usuario");

        // Preparamos datos de entrada
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", "usuario@test.com");
        credentials.put("password", "password123");

        // Configuramos los mocks
        when(usuarioService.authenticate("usuario@test.com", "password123")).thenReturn(Optional.of(usuarioEjemplo));
        when(jwtService.generateToken(anyString(), anyLong())).thenReturn("token-jwt-generado");

        // Ejecutamos el método
        ResponseEntity<?> response = usuarioController.login(credentials);

        // Verificaciones
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) body.get("success"));
        assertEquals("Login successful", body.get("message"));
        assertEquals(usuarioEjemplo, body.get("usuario"));
        assertEquals("token-jwt-generado", body.get("token"));

        verify(usuarioService, times(1)).authenticate("usuario@test.com", "password123");
        verify(jwtService, times(1)).generateToken(eq(usuarioEjemplo.getCorreo()), eq(usuarioEjemplo.getUsuarioID()));
        // System.out.println("✅ Prueba exitosa: Se realizó el login correctamente");
    }

    @Test
    void testLoginCredencialesInvalidas() {
        // System.out.println("▶️ Iniciando prueba: Login con credenciales inválidas");

        // Preparamos datos de entrada
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", "usuario@test.com");
        credentials.put("password", "passwordIncorrecto");

        // Configuramos los mocks
        when(usuarioService.authenticate("usuario@test.com", "passwordIncorrecto")).thenReturn(Optional.empty());

        // Ejecutamos el método
        ResponseEntity<?> response = usuarioController.login(credentials);

        // Verificaciones
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) body.get("success"));
        assertEquals("Invalid email or password", body.get("message"));

        verify(usuarioService, times(1)).authenticate("usuario@test.com", "passwordIncorrecto");
        verify(jwtService, never()).generateToken(anyString(), anyLong());
        // System.out.println("✅ Prueba exitosa: Se manejó correctamente el caso de credenciales inválidas");
    }

    @Test
    void testSignupExitoso() {
        // System.out.println("▶️ Iniciando prueba: Registro de usuario (signup)");

        // Configuramos los mocks
        when(usuarioService.createUsuario(any(Usuario.class))).thenReturn(usuarioEjemplo);
        when(jwtService.generateToken(anyString(), anyLong())).thenReturn("token-jwt-generado");

        // Ejecutamos el método
        ResponseEntity<?> response = usuarioController.signup(usuarioEjemplo);

        // Verificaciones
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) body.get("success"));
        assertEquals("User registered successfully", body.get("message"));
        assertEquals(usuarioEjemplo, body.get("usuario"));
        assertEquals("token-jwt-generado", body.get("token"));

        verify(usuarioService, times(1)).createUsuario(any(Usuario.class));
        verify(jwtService, times(1)).generateToken(eq(usuarioEjemplo.getCorreo()), eq(usuarioEjemplo.getUsuarioID()));
        // System.out.println("✅ Prueba exitosa: Se registró el usuario correctamente");
    }

    @Test
    void testSignupError() {
        // System.out.println("▶️ Iniciando prueba: Error en registro de usuario");

        // Configuramos el mock para lanzar excepción
        when(usuarioService.createUsuario(any(Usuario.class)))
                .thenThrow(new RuntimeException("El correo ya está registrado"));

        // Ejecutamos el método
        ResponseEntity<?> response = usuarioController.signup(usuarioEjemplo);

        // Verificaciones
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) body.get("success"));
        assertEquals("El correo ya está registrado", body.get("message"));

        verify(usuarioService, times(1)).createUsuario(any(Usuario.class));
        verify(jwtService, never()).generateToken(anyString(), anyLong());
        // System.out.println("✅ Prueba exitosa: Se manejó correctamente el error en registro");
    }
}
