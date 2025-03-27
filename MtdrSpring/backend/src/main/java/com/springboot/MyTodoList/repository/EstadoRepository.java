package com.springboot.MyTodoList.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springboot.MyTodoList.model.Estado;

@Repository
public interface EstadoRepository extends JpaRepository<Estado, Long> {
}
