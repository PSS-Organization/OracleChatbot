package com.springboot.MyTodoList.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springboot.MyTodoList.model.Usuario;
import com.springboot.MyTodoList.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    //private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Optional<Usuario> authenticate(String correo, String contrasena) {
        Optional<Usuario> usuario = usuarioRepository.findByCorreo(correo);

        // Cambiar si se necesita encriptar la contraseña, por el momento no esta encriptada
        if (usuario.isPresent() && usuario.get().getContrasena().equals(contrasena)) {
            return usuario; // User found, return user
        }
        return Optional.empty();
    }

    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario createUsuario(Usuario usuario) {
        // Verificar si el usuario ya existe en la base de datos
        if (usuarioRepository.findByCorreo(usuario.getCorreo()).isPresent()) {
            throw new RuntimeException("El usuario con correo " + usuario.getCorreo() + " ya existe.");
        }

        // Encriptar la contraseña antes de guardarla por el momento no
        //usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));

        // Guardar el nuevo usuario en la base de datos
        return usuarioRepository.save(usuario);
    }
}