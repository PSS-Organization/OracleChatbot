package com.springboot.MyTodoList.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.springboot.MyTodoList.model.Usuario;
import com.springboot.MyTodoList.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    //private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }


    // Obtener usuario por ID
    public ResponseEntity<Usuario> getUsuarioById(Long id) {
        Optional<Usuario> userData = usuarioRepository.findById(id);
        if (userData.isPresent()) {
            Usuario usuario = userData.get();
            // Verifica que el ID esté presente
            System.out.println("Usuario ID: " + usuario.getUsuarioID()); // Para debugging
            return new ResponseEntity<>(usuario, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Crear nuevo usuario
    public Usuario createUsuario(Usuario usuario) {
        // Verificar si el usuario ya existe en la base de datos
        if (usuarioRepository.findByCorreo(usuario.getCorreo()).isPresent()) {
            throw new RuntimeException("El usuario con correo " + usuario.getCorreo() + " ya existe.");

            // Encriptar la contraseña antes de guardarla por el momento no
            //usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
            // Guardar el nuevo usuario en la base de datos
        }

        // Encriptar la contraseña antes de guardarla por el momento no
        //usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        // Guardar el nuevo usuario en la base de datos
        return usuarioRepository.save(usuario);
    }

    // Actualizar usuario
    public Usuario updateUsuario(Long id, Usuario usuario) {
        Optional<Usuario> usuarioData = usuarioRepository.findById(id);
        if (usuarioData.isPresent()) {
            Usuario usuarioExistente = usuarioData.get();
            usuarioExistente.setNombre(usuario.getNombre());
            usuarioExistente.setCorreo(usuario.getCorreo());
            usuarioExistente.setTelefono(usuario.getTelefono());
            usuarioExistente.setContrasena(usuario.getContrasena());
            usuarioExistente.setRolUsuario(usuario.getRolUsuario());
            usuarioExistente.setEsAdmin(usuario.getEsAdmin());
            usuarioExistente.setEquipoID(usuario.getEquipoID());
            // if (usuario.getTelegramID() != null) {
            //     usuarioExistente.setTelegramID(usuario.getTelegramID());
            // }
            // return usuarioRepository.save(usuarioExistente);
        }
        return null;
    }

    // Eliminar usuario
    public boolean deleteUsuario(Long id) {
        try {
            usuarioRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Autenticación
    public Optional<Usuario> authenticate(String correo, String contrasena) {
        Optional<Usuario> usuario = usuarioRepository.findByCorreo(correo);
        if (usuario.isPresent() && usuario.get().getContrasena().equals(contrasena)) {
            return usuario;
        }
        return Optional.empty();
    }

    

    // Actualizar el telegram_id de un usuario
    public Usuario updateTelegramID(Long usuarioId, Long telegramID) {
        Optional<Usuario> usuarioData = usuarioRepository.findById(usuarioId);
        if (usuarioData.isPresent()) {
            Usuario usuarioExistente = usuarioData.get();
            usuarioExistente.setTelegramID(telegramID);
            return usuarioRepository.save(usuarioExistente);
        }
        return null;
    }

        // Buscar usuario por teléfono

    public Optional<Usuario> getUsuarioByTelegramId(Long telegramId) {
        return usuarioRepository.findByTelegramID(telegramId);
    }
    
}
