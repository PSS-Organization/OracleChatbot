package com.springboot.MyTodoList.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springboot.MyTodoList.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);

    // Optional<Usuario> findByTelegramID(String telegramID);
    //Optional<Usuario> findByTelefono(String telefono);

    Optional<Usuario> findByTelegramID(Long telegramID);

    //Optional<Usuario>findByEsAdmin(Boolean esAdmin);

}