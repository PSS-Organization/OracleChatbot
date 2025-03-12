package com.springboot.MyTodoList.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "USUARIOS")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USUARIOID") // Asegúrate que coincida exactamente con el nombre en la BD
    private Long usuarioID;

    @Column(name = "NOMBRE", nullable = false)
    private String nombre;

    @Column(name = "CORREO")
    private String correo;

    @Column(name = "TELEFONO")
    private String telefono;

    @Column(name = "CONTRASENA")
    private String contrasena;

    @Column(name = "ROLUSUARIO")
    private String rolUsuario;

    @Column(name = "ESADMIN", nullable = false)
    private Boolean esAdmin;

    @Column(name = "EQUIPOID")
    private Long equipoID;

    public Usuario() {
    }

    public Usuario(Long usuarioID, String nombre, String correo, String telefono, String contrasena, String rolUsuario,
            Boolean esAdmin, Long equipoID) {
        this.usuarioID = usuarioID;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.contrasena = contrasena;
        this.rolUsuario = rolUsuario;
        this.esAdmin = esAdmin;
        this.equipoID = equipoID;
    }

    public Long getUsuarioID() {
        return usuarioID;
    }

    public void setUsuarioID(Long usuarioID) {
        this.usuarioID = usuarioID;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getRolUsuario() {
        return rolUsuario;
    }

    public void setRolUsuario(String rolUsuario) {
        this.rolUsuario = rolUsuario;
    }

    public Boolean getEsAdmin() {
        return esAdmin;
    }

    public void setEsAdmin(Boolean esAdmin) {
        this.esAdmin = esAdmin;
    }

    public Long getEquipoID() {
        return equipoID;
    }

    public void setEquipoID(Long equipoID) {
        this.equipoID = equipoID;
    }

    @Override
    public String toString() {
        return "Usuario{"
                + "usuarioID=" + usuarioID
                + ", nombre='" + nombre + '\''
                + ", correo='" + correo + '\''
                + ", telefono='" + telefono + '\''
                + ", contrasena='" + contrasena + '\''
                + ", rolUsuario='" + rolUsuario + '\''
                + ", esAdmin=" + esAdmin
                + ", equipoID=" + equipoID
                + '}';
    }
}
