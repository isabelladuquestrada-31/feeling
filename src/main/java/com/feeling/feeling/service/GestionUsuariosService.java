package com.feeling.feeling.service;

import com.feeling.feeling.model.*;
import com.feeling.feeling.repository.SuspensionRepository;
import com.feeling.feeling.repository.UsuarioRepository;
import com.feeling.feeling.repository.IVoluntarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Lógica de la gestión de usuarios del administrador (RF-17):
 * listar usuarios, suspender y reactivar voluntarios.
 *
 * @author Isabella Duque
 * @version 1.0
 */
@Service
public class GestionUsuariosService {

    private final UsuarioRepository usuarioRepository;
    private final IVoluntarioRepository voluntarioRepository;
    private final SuspensionRepository suspensionRepository;

    /**
     * Crea el servicio con los repositorios que necesita.
     *
     * @param usuarioRepository acceso a los usuarios
     * @param voluntarioRepository acceso a los voluntarios
     * @param suspensionRepository acceso a las suspensiones
     */
    public GestionUsuariosService(UsuarioRepository usuarioRepository,
                                  IVoluntarioRepository voluntarioRepository,
                                  SuspensionRepository suspensionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.voluntarioRepository = voluntarioRepository;
        this.suspensionRepository = suspensionRepository;
    }

    /**
     * Obtiene todos los usuarios registrados en la plataforma.
     *
     * @return lista de usuarios
     */
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    /**
     * Obtiene todos los voluntarios con su estado actual.
     *
     * @return lista de voluntarios
     */
    public List<Voluntario> listarVoluntarios() {
        return voluntarioRepository.findAll();
    }

    /**
     * Suspende a un voluntario: registra la suspensión y cambia su estado
     *
     * @param voluntarioId  id del voluntario a suspender
     * @param administrador administrador que aplica la suspensión
     * @param motivo razón de la suspensión
     * @param fechaFin fecha en que termina la suspensión
     * @return la suspensión registrada
     * @throws IllegalArgumentException si el voluntario no existe, el motivo está vacío o la fecha de fin no es posterior a hoy
     * @throws IllegalStateException    si el voluntario ya está suspendido
     */
    @Transactional
    public Suspension suspenderVoluntario(Integer voluntarioId, Administrador administrador,
                                          String motivo, LocalDate fechaFin) {
        Voluntario voluntario = voluntarioRepository.findById(voluntarioId)
                .orElseThrow(() -> new IllegalArgumentException("El voluntario no existe."));

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el motivo de la suspensión.");
        }
        if (fechaFin == null || !fechaFin.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a hoy.");
        }
        if (voluntario.getEstado() == EstadoVoluntario.SUSPENDIDO) {
            throw new IllegalStateException("El voluntario ya está suspendido.");
        }

        voluntario.setEstado(EstadoVoluntario.SUSPENDIDO);
        voluntarioRepository.save(voluntario);

        return suspensionRepository.save(new Suspension(voluntario, administrador, motivo, fechaFin));
    }

    /**
     * Reactiva a un voluntario suspendido, devolviéndolo a {@link EstadoVoluntario#ACTIVO}.
     * Las suspensiones anteriores se conservan como historial.
     *
     * @param voluntarioId id del voluntario a reactivar
     * @throws IllegalArgumentException si el voluntario no existe
     * @throws IllegalStateException    si el voluntario no está suspendido
     */
    @Transactional
    public void reactivarVoluntario(Integer voluntarioId) {
        Voluntario voluntario = voluntarioRepository.findById(voluntarioId)
                .orElseThrow(() -> new IllegalArgumentException("El voluntario no existe."));

        if (voluntario.getEstado() != EstadoVoluntario.SUSPENDIDO) {
            throw new IllegalStateException("El voluntario no está suspendido.");
        }

        voluntario.setEstado(EstadoVoluntario.ACTIVO);
        voluntarioRepository.save(voluntario);
    }
}