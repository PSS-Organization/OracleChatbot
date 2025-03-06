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

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/all")
    public List<Usuario> getAllUsuarios() {
        return usuarioService.getAllUsuarios();
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String correo = credentials.get("email");  // Get email from request
        String contrasena = credentials.get("password"); // Get password from request

        Optional<Usuario> usuario = (Optional<Usuario>) usuarioService.authenticate(correo, contrasena);

        if (usuario.isPresent()) {
            // ✅ If login is successful, return user data
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("usuario", usuario.get()); // ⚠️ Exclude password if necessary

            return ResponseEntity.ok(response);
        } else {
            // ❌ If login fails, return 401 Unauthorized
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Invalid email or password"));
        }
    }

}