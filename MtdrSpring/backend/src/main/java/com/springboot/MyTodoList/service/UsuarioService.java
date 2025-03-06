package com.springboot.MyTodoList.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springboot.MyTodoList.model.Usuario;
import com.springboot.MyTodoList.repository.UsuarioRepository;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Optional<Usuario> authenticate(String correo, String contrasena) {
        Optional<Usuario> usuario = usuarioRepository.findByCorreo(correo);
        
        // Compare passwords directly (since they are stored in plain text)
        if (usuario.isPresent() && usuario.get().getContrasena().equals(contrasena)) {
            return usuario; // User found, return user
        }
        return Optional.empty(); // User not found, return empty
    }

    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }
}