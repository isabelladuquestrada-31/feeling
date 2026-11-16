package com.feeling.feeling.repository;

import com.feeling.feeling.dto.OpcionFiltro;
import com.feeling.feeling.dto.VoluntarioResumen;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Clase que representa las consultas de la busqueda de voluntarios (RF-07),
 * hechas directamente con SQL.
 *
 * @author Daniel Bladón
 */
@Repository
public class BusquedaVoluntariosRepository {

    /**
     * Busca voluntarios activos, que no estén suspendidos, filtrando por materia y tipo de apoyo.
     * Si un filtro llega vacío (null), no se aplica.
     */
    private static final String SQL_BUSCAR = """
            SELECT v.id,
                   COALESCE(p.nombre_preferido, u.nombre_usuario) AS nombre,
                   e.programa_academico AS programa,
                   (SELECT GROUP_CONCAT(m.nombre ORDER BY m.nombre SEPARATOR '|')
                      FROM voluntario_materia vm
                      JOIN materia m ON m.codigo = vm.materia_codigo
                     WHERE vm.voluntario_id = v.id) AS materias,
                   (SELECT GROUP_CONCAT(vt.tipo_apoyo ORDER BY vt.tipo_apoyo SEPARATOR '|')
                      FROM voluntario_tipo_apoyo vt
                     WHERE vt.voluntario_id = v.id) AS tipos,
                   (SELECT GROUP_CONCAT(DISTINCT d.dia SEPARATOR '|')
                      FROM disponibilidad d
                     WHERE d.voluntario_id = v.id) AS dias
              FROM voluntario v
              JOIN estudiante e ON e.usuario_id = v.estudiante_id
              JOIN usuario u ON u.id = e.usuario_id
              LEFT JOIN perfil p ON p.usuario_id = u.id
             WHERE v.estado = 'ACTIVO'
               AND u.activo = TRUE
               AND NOT EXISTS (SELECT 1 FROM suspension s
                                WHERE s.voluntario_id = v.id
                                  AND (s.fecha_fin IS NULL OR s.fecha_fin > NOW()))
               AND (? IS NULL OR EXISTS (SELECT 1 FROM voluntario_materia vm2
                                          WHERE vm2.voluntario_id = v.id AND vm2.materia_codigo = ?))
               AND (? IS NULL OR EXISTS (SELECT 1 FROM voluntario_tipo_apoyo vt2
                                          WHERE vt2.voluntario_id = v.id AND vt2.tipo_apoyo = ?))
             ORDER BY nombre
            """;
    /**
     * Herramienta de Spring para ejecutar consultas SQL.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Crea el repositorio con el JdbcTemplate que entrega Spring.
     *
     * @param jdbcTemplate herramienta para ejecutar SQL.
     */
    public BusquedaVoluntariosRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * RF-07: busca voluntarios activos según los filtros.
     *
     * @param materiaCodigo código de la materia, o null para no filtrar
     * @param tipoApoyo     tipo de apoyo, o null para no filtrar
     * @return voluntarios que cumplen los filtros ordenados por nombre
     */
    public List<VoluntarioResumen> buscar(String materiaCodigo, String tipoApoyo) {
        return jdbcTemplate.query(SQL_BUSCAR, (rs, fila) -> new VoluntarioResumen(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("programa"),
                        separar(rs.getString("materias")),
                        separar(rs.getString("tipos")),
                        separar(rs.getString("dias"))),
                materiaCodigo, materiaCodigo, tipoApoyo, tipoApoyo);
    }

    /**
     * Devuelve todas las materias para el filtro de busqueda.
     *
     * @return materias ordenadas por nombre
     */
    public List<OpcionFiltro> materias() {
        return jdbcTemplate.query("SELECT codigo, nombre FROM materia ORDER BY nombre",
                (rs, fila) -> new OpcionFiltro(rs.getString("codigo"), rs.getString("nombre")));
    }

    /**
     * Devuelve los tipos de apoyo que ofrece al menos un voluntario.
     *
     * @return códigos de los tipos de apoyo
     */
    public List<String> tiposApoyo() {
        return jdbcTemplate.queryForList(
                "SELECT DISTINCT tipo_apoyo FROM voluntario_tipo_apoyo ORDER BY tipo_apoyo", String.class);
    }

    /**
     * Convierte un texto separado por "|" en una lista.
     *
     * @param texto texto con los valores separados por "|"
     * @return lista de valores; vacía si el texto es null
     */
    private static List<String> separar(String texto) {
        if (texto == null || texto.isBlank()) {
            return List.of();
        }
        return List.of(texto.split("\\|"));
    }
}
