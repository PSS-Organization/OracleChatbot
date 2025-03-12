package com.springboot.MyTodoList.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.MyTodoList.model.Usuario;
import com.springboot.MyTodoList.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuarios", description = "Endpoints para gestionar usuarios") // 📌 Agregar categoría en Swagger
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/all")
    @Operation(summary = "Obtener todos los usuarios", description = "Devuelve la lista de todos los usuarios en la base de datos")
    public List<Usuario> getAllUsuarios() {
        return usuarioService.getAllUsuarios();
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuario", description = "Verifica las credenciales del usuario y devuelve sus datos si son correctas.")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String correo = credentials.get("email");
        String contrasena = credentials.get("password");
        Optional<Usuario> usuario = (Optional<Usuario>) usuarioService.authenticate(correo, contrasena);

        if (usuario.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("usuario", usuario.get());

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Invalid email or password"));
        }
    }

    // 🔹 Nuevo endpoint para registrar usuarios
    @PostMapping("/signup")
    @Operation(summary = "Registrar un nuevo usuario", description = "Registra un usuario en la base de datos")

    public ResponseEntity<?> signup(@RequestBody Usuario usuario) {
        try {
            Usuario nuevoUsuario = usuarioService.createUsuario(usuario);
            return ResponseEntity.ok(Map.of("success", true, "message", "User registered successfully", "usuario", nuevoUsuario));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}