package com.feeling.feeling.service;


import com.feeling.feeling.dto.OpcionFiltro;
import com.feeling.feeling.dto.VoluntarioResumen;
import com.feeling.feeling.model.ProgramaAcademico;
import com.feeling.feeling.repository.BusquedaVoluntariosRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Clase que representa la lógica de búsqueda de voluntarios (RF-07): limpia los filtros
 * y convierte los códigos en textos legibles para la vista
 *
 * @author Daniel Blandón
 */
@Service
public class BusquedaVoluntariosService {

    /**
     * Orden en que se muestran los días de la semana.
     */
    private static final List<String> ORDEN_DIAS =
            List.of("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO");

    /**
     * Nombre con tilde de cada día.
     */
    private static final Map<String, String> NOMBRES_DIAS = Map.of(
            "LUNES", "Lunes", "MARTES", "Martes", "MIERCOLES", "Miércoles", "JUEVES", "Jueves",
            "VIERNES", "Viernes", "SABADO", "Sábado", "DOMINGO", "Domingo");

    /**
     * Nombre legible de cada tipo de apoyo.
     */
    private static final Map<String, String> NOMBRES_APOYO = Map.of(
            "TUTORIA", "Tutoría",
            "EXPLICACION_CONCEPTOS", "Explicación de conceptos",
            "PREPARACION_EXAMENES", "Preparación de exámenes",
            "COMPRENSION_MATERIAL", "Comprensión de material");

    /**
     * Repositorio con las consultas de la búsqueda.
     */
    private final BusquedaVoluntariosRepository repositorio;

    /**
     * Crea el servicio con el repositorio que entrega Spring.
     *
     * @param repositorio repositorio de la búsqueda
     */
    public BusquedaVoluntariosService(BusquedaVoluntariosRepository repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * RF-07: busca voluntarios activos por materia, tipo de apoyo y programa académico.
     * Los filtros vacíos se ignoran, y un código de programa que no existe también.
     *
     * @param materiaCodigo  código de la materia, puede venir vacío
     * @param tipoApoyo      tipo de apoyo, puede venir vacío
     * @param programaCodigo código del programa, por ejemplo INGENIERIA_SISTEMAS; puede venir vacío
     * @return voluntarios encontrados, con los días ordenados y los textos legibles
     */
    public List<VoluntarioResumen> buscar(String materiaCodigo, String tipoApoyo, String programaCodigo) {
        String programa = ProgramaAcademico.desdeCodigo(programaCodigo)
                .map(ProgramaAcademico::getEtiqueta)
                .orElse(null);
        return repositorio.buscar(limpiar(materiaCodigo), limpiar(tipoApoyo), programa).stream()
                .map(v -> new VoluntarioResumen(
                        v.getId(),
                        v.getNombre(),
                        v.getPrograma(),
                        v.getMaterias(),
                        v.getTiposApoyo().stream().map(this::nombreApoyo).toList(),
                        v.getDias().stream()
                                .sorted(Comparator.comparingInt(ORDEN_DIAS::indexOf))
                                .map(dia -> NOMBRES_DIAS.getOrDefault(dia, dia))
                                .toList()))
                .toList();
    }

    /**
     * Devuelve los programas académicos agrupados por facultad para el filtro.
     *
     * @return programas de la UAM agrupados por facultad
     */
    public Map<String, List<ProgramaAcademico>> programas() {
        return ProgramaAcademico.porFacultad();
    }

    /**
     * Devuelve las materias para el filtro.
     *
     * @return materias ordenadas por nombre
     */
    public List<OpcionFiltro> materias() {
        return repositorio.materias();
    }

    /**
     * Devuelve los tipos de apoyo para el filtro, con su nombre legible.
     *
     * @return tipos de apoyo disponibles
     */
    public List<OpcionFiltro> tiposApoyo() {
        return repositorio.tiposApoyo().stream()
                .map(codigo -> new OpcionFiltro(codigo, nombreApoyo(codigo)))
                .toList();
    }

    /**
     * Convierte el código de un tipo de apoyo en su nombre legible.
     *
     * @param codigo código del tipo de apoyo
     * @return nombre legible, o el mismo código si no se conoce
     */
    public String nombreApoyo(String codigo) {
        return NOMBRES_APOYO.getOrDefault(codigo, codigo);
    }

    /**
     * Convierte un filtro vacío en null para que la consulta no lo aplique.
     *
     * @param texto valor del filtro
     * @return el valor sin espacios, o null si está vacío
     */
    private String limpiar(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}
