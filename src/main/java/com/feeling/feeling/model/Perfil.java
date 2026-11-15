package com.feeling.feeling.model;

import jakarta.persistence.*;

/**
 * Datos personales que el estudiante muestra en la plataforma.
 * Cada usuario tiene un solo perfil, que usa el mismo id del usuario.
 *
 * @author Isabella Duque
 * @version 1.0
 */
@Entity
@Table(name = "perfil")
public class Perfil {

    /** Identificador del usuario dueño. Es la llave primaria de esta tabla. */
    @Id
    @Column(name = "usuario_id")
    private Integer usuarioId;

    /** Usuario dueño del perfil. Su id se usa como id de esta tabla. */
    @OneToOne
    @MapsId
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    /** Nombre con el que el estudiante quiere que lo llamen. */
    private String nombrePreferido;

    /** Pronombres del estudiante: él, ella, elle o prefiero no decir. */
    private String pronombres;

    /** Número de WhatsApp. Solo se comparte cuando una solicitud es aceptada. */
    private String whatsapp;

    /**
     * Devuelve el id del usuario dueño del perfil.
     *
     * @return el id del usuario
     */
    public Integer getUsuarioId() {
        return usuarioId;
    }

    /**
     * Devuelve el usuario dueño del perfil.
     *
     * @return el usuario dueño
     */
    public Usuario getUsuario() {
        return usuario;
    }

    /**
     * Asigna el usuario dueño del perfil. El perfil toma el mismo id del usuario.
     *
     * @param usuario usuario dueño del perfil
     */
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    /**
     * Devuelve el nombre con el que el estudiante quiere que lo llamen.
     *
     * @return el nombre preferido, o null si no lo ha escrito
     */
    public String getNombrePreferido() {
        return nombrePreferido;
    }

    /**
     * Cambia el nombre con el que el estudiante quiere que lo llamen.
     *
     * @param nombrePreferido nuevo nombre preferido
     */
    public void setNombrePreferido(String nombrePreferido) {
        this.nombrePreferido = nombrePreferido;
    }

    /**
     * Devuelve los pronombres del estudiante.
     *
     * @return los pronombres, o null si no los ha elegido
     */
    public String getPronombres() {
        return pronombres;
    }

    /**
     * Cambia los pronombres del estudiante.
     *
     * @param pronombres él, ella, elle o prefiero no decir
     */
    public void setPronombres(String pronombres) {
        this.pronombres = pronombres;
    }

    /**
     * Devuelve el número de WhatsApp del estudiante.
     *
     * @return el número de WhatsApp
     */
    public String getWhatsapp() {
        return whatsapp;
    }

    /**
     * Cambia el número de WhatsApp del estudiante.
     *
     * @param whatsapp nuevo número de WhatsApp
     */
    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }
}