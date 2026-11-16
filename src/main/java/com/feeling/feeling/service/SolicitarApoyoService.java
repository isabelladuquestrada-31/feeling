package com.feeling.feeling.service;

import com.feeling.feeling.dto.HorarioLibre;
import com.feeling.feeling.model.DiaSemana;
import com.feeling.feeling.model.Disponibilidad;
import com.feeling.feeling.model.EstadoSolicitud;
import com.feeling.feeling.model.EstadoVoluntario;
import com.feeling.feeling.model.Solicitud;
import com.feeling.feeling.model.Voluntario;
import com.feeling.feeling.repository.DisponibilidadRepository;
import com.feeling.feeling.repository.IVoluntarioRepository;
import com.feeling.feeling.repository.SolicitudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Prepara y valida el formulario "Solicitar apoyo" (RF-08, SCRUM-81):
 * calcula los horarios libres del voluntario a partir de su disponibilidad
 * y crea la solicitud con el servicio de solicitudes (SCRUM-80).
 *
 * @author Daniel Blandón
 * @version 1.0
 */
@Service
public class SolicitarApoyoService {

    /** Cuántos días hacia adelante se ofrecen horarios. */
    public static final int DIAS_DE_HORARIOS = 14;

    /** Modalidades que se pueden elegir. */
    public static final List<String> MODALIDADES = List.of("PRESENCIAL", "VIRTUAL");

    /** Largo máximo del tema (igual a la columna de la base de datos). */
    public static final int LARGO_MAXIMO_TEMA = 150;

    private static final List<EstadoSolicitud> ESTADOS_QUE_OCUPAN =
            List.of(EstadoSolicitud.PENDIENTE, EstadoSolicitud.ACEPTADA);

    private final IVoluntarioRepository voluntarioRepository;
    private final DisponibilidadRepository disponibilidadRepository;
    private final SolicitudRepository solicitudRepository;
    private final SolicitudService solicitudService;

    /**
     * Crea el servicio con lo que necesita.
     *
     * @param voluntarioRepository     repositorio de voluntarios
     * @param disponibilidadRepository repositorio de la disponibilidad
     * @param solicitudRepository      repositorio de solicitudes
     * @param solicitudService         servicio que registra la solicitud
     */
    public SolicitarApoyoService(IVoluntarioRepository voluntarioRepository,
                                 DisponibilidadRepository disponibilidadRepository,
                                 SolicitudRepository solicitudRepository,
                                 SolicitudService solicitudService) {
        this.voluntarioRepository = voluntarioRepository;
        this.disponibilidadRepository = disponibilidadRepository;
        this.solicitudRepository = solicitudRepository;
        this.solicitudService = solicitudService;
    }

    /**
     * Busca un voluntario activo con sus materias y tipos de apoyo cargados.
     *
     * @param voluntarioId id del voluntario
     * @return el voluntario, o vacío si no existe o está suspendido
     */
    @Transactional(readOnly = true)
    public Optional<Voluntario> voluntarioActivo(Integer voluntarioId) {
        if (voluntarioId == null) {
            return Optional.empty();
        }
        return voluntarioRepository.findById(voluntarioId)
                .filter(v -> v.getEstado() == EstadoVoluntario.ACTIVO)
                .map(v -> {
                    v.getMaterias().size();
                    v.getTiposApoyo().size();
                    v.getEstudiante().getProgramaAcademico();
                    return v;
                });
    }

    /**
     * Calcula los horarios libres del voluntario en los próximos días: cada franja
     * de su disponibilidad en cada fecha que cae en ese día de la semana, sin los
     * horarios que ya pasaron ni los que tienen una solicitud pendiente o aceptada.
     *
     * @param voluntarioId id del voluntario
     * @param ahora        fecha y hora desde la que se buscan horarios
     * @return horarios libres, del más cercano al más lejano
     */
    @Transactional(readOnly = true)
    public List<HorarioLibre> horariosLibres(Integer voluntarioId, LocalDateTime ahora) {
        List<Disponibilidad> franjas = disponibilidadRepository.findByVoluntarioId(voluntarioId);
        Set<LocalDateTime> ocupados = solicitudRepository
                .findByVoluntarioIdAndEstadoInAndFechaProgramadaAfter(voluntarioId, ESTADOS_QUE_OCUPAN, ahora)
                .stream()
                .map(Solicitud::getFechaProgramada)
                .collect(Collectors.toSet());

        List<HorarioLibre> libres = new ArrayList<>();
        LocalDate hoy = ahora.toLocalDate();
        for (int i = 0; i < DIAS_DE_HORARIOS; i++) {
            LocalDate fecha = hoy.plusDays(i);
            DiaSemana dia = DiaSemana.values()[fecha.getDayOfWeek().getValue() - 1];
            for (Disponibilidad franja : franjas) {
                if (franja.getDia() != dia) {
                    continue;
                }
                LocalDateTime inicio = fecha.atTime(franja.getHoraInicio());
                if (inicio.isAfter(ahora) && !ocupados.contains(inicio)) {
                    libres.add(new HorarioLibre(inicio, fecha.atTime(franja.getHoraFin())));
                }
            }
        }
        libres.sort(Comparator.comparing(HorarioLibre::inicio));
        return libres;
    }

    /**
     * Crea la solicitud en estado PENDIENTE después de validar el formulario.
     *
     * @param estudianteId id del estudiante que pide el apoyo
     * @param voluntarioId id del voluntario
     * @param materia      código de la materia (debe ser una que el voluntario domine)
     * @param tema         tema que quiere trabajar
     * @param tipo         tipo de apoyo (debe ser uno que el voluntario ofrezca)
     * @param modalidad    PRESENCIAL o VIRTUAL (por WhatsApp)
     * @param horario      inicio del horario elegido
     * @param ahora        fecha y hora actuales
     * @return la solicitud creada
     * @throws IllegalArgumentException si falta un campo o un dato no es válido
     * @throws IllegalStateException    si el horario ya no está disponible
     */
    @Transactional
    public Solicitud solicitar(Integer estudianteId, Integer voluntarioId, String materia, String tema,
                               String tipo, String modalidad, LocalDateTime horario, LocalDateTime ahora) {
        Voluntario voluntario = voluntarioActivo(voluntarioId)
                .orElseThrow(() -> new IllegalArgumentException("Este voluntario no está disponible."));

        if (Objects.equals(voluntario.getEstudiante().getId(), estudianteId)) {
            throw new IllegalArgumentException("No puedes solicitarte apoyo a ti mismo.");
        }
        if (vacio(materia) || vacio(tema) || vacio(tipo) || vacio(modalidad) || horario == null) {
            throw new IllegalArgumentException(
                    "Completa todos los campos: materia, tema, tipo de apoyo, horario y modalidad.");
        }
        String temaLimpio = tema.trim();
        if (temaLimpio.length() > LARGO_MAXIMO_TEMA) {
            throw new IllegalArgumentException("El tema puede tener máximo " + LARGO_MAXIMO_TEMA + " caracteres.");
        }
        boolean dominaMateria = voluntario.getMaterias().stream()
                .anyMatch(m -> m.getCodigo().equals(materia));
        if (!dominaMateria) {
            throw new IllegalArgumentException("Elige una de las materias que ofrece este voluntario.");
        }
        boolean ofreceTipo = voluntario.getTiposApoyo().stream()
                .anyMatch(t -> t.name().equals(tipo));
        if (!ofreceTipo) {
            throw new IllegalArgumentException("Elige uno de los tipos de apoyo que ofrece este voluntario.");
        }
        if (!MODALIDADES.contains(modalidad)) {
            throw new IllegalArgumentException("Elige si el acompañamiento es presencial o por WhatsApp.");
        }
        boolean horarioLibre = horariosLibres(voluntarioId, ahora).stream()
                .anyMatch(h -> h.inicio().equals(horario));
        if (!horarioLibre) {
            throw new IllegalStateException("El horario que elegiste ya no está disponible. Elige otro.");
        }

        Solicitud solicitud = new Solicitud();
        solicitud.setEstudianteId(estudianteId);
        solicitud.setVoluntarioId(voluntarioId);
        solicitud.setMateriaCodigo(materia);
        solicitud.setTema(temaLimpio);
        solicitud.setTipo(tipo);
        solicitud.setModalidad(modalidad);
        solicitud.setFechaProgramada(horario);
        return solicitudService.registrar(solicitud);
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
