package com.feeling.feeling.model;

import jakarta.persistence.*;

/**
 * Preferencias de accesibilidad del usuario para ver la plataforma.
 * Cada usuario tiene una sola configuración, que usa el mismo id del usuario.
 *
 * @author Isabella Duque
 * @version 1.0
 */
@Entity
@Table(name = "preferencias_accesibilidad")
public class PreferenciasAccesibilidad {

    /** Identificador del usuario dueño. Es la llave primaria de esta tabla. */
    @Id
    @Column(name = "usuario_id")
    private Integer usuarioId;

    /** Usuario dueño de las preferencias. Su id se usa como id de esta tabla. */
    @OneToOne
    @MapsId
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    /** Indica si la página se muestra con letra grande. */
    private boolean letraGrande = false;

    /** Indica si la página se muestra con alto contraste. */
    private boolean altoContraste = false;

    /**
     * Devuelve el id del usuario dueño de las preferencias.
     *
     * @return el id del usuario
     */
    public Integer getUsuarioId() {
        return usuarioId;
    }

    /**
     * Devuelve el usuario dueño de las preferencias.
     *
     * @return el usuario dueño
     */
    public Usuario getUsuario() {
        return usuario;
    }

    /**
     * Asigna el usuario dueño de las preferencias. Las preferencias toman el mismo id del usuario.
     *
     * @param usuario usuario dueño de las preferencias
     */
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    /**
     * Indica si la letra grande está activada.
     *
     * @return true si está activada, false si no
     */
    public boolean isLetraGrande() {
        return letraGrande;
    }

    /**
     * Activa o desactiva la letra grande.
     *
     * @param letraGrande true para activarla, false para desactivarla
     */
    public void setLetraGrande(boolean letraGrande) {
        this.letraGrande = letraGrande;
    }

    /**
     * Indica si el alto contraste está activado.
     *
     * @return true si está activado, false si no
     */
    public boolean isAltoContraste() {
        return altoContraste;
    }

    /**
     * Activa o desactiva el alto contraste.
     *
     * @param altoContraste true para activarlo, false para desactivarlo
     */
    public void setAltoContraste(boolean altoContraste) {
        this.altoContraste = altoContraste;
    }
}