package com.feeling.feeling.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
/**
 * Representa a una persona registrada en la plataforma Feeling.
 * Es una clase abstracta: cada usuario es un Estudiante o un Administrador.
 * Se guarda en la tabla "usuario", y cada tipo de usuario tiene además su propia tabla.
 *
 * @author Isabella Duque
 */
@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Usuario {

    /** Identificador único del usuario, generado por la base de datos. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    /** Nombre con el que el usuario inicia sesión. No se puede repetir. */
    protected String nombreUsuario;

    /** Contraseña encriptada con BCrypt. Nunca se guarda la contraseña real. */
    protected String contrasenaHash;

    /** Indica si el usuario puede usar la plataforma. El administrador puede desactivarlo. */
    protected boolean activo = true;

    /** Fecha y hora en que el usuario se registró. */
    protected LocalDateTime fechaRegistro;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public void setContrasenaHash(String contrasenaHash) {
        this.contrasenaHash = contrasenaHash;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}