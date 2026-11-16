package com.feeling.feeling.service;

import com.feeling.feeling.dto.ResumenAdmin;
import com.feeling.feeling.model.EstadoVoluntario;
import com.feeling.feeling.model.Estudiante;
import com.feeling.feeling.model.Materia;
import com.feeling.feeling.model.Perfil;
import com.feeling.feeling.repository.IMateriaRepository;
import com.feeling.feeling.repository.IPerfilRepository;
import com.feeling.feeling.repository.IVoluntarioRepository;
import com.feeling.feeling.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas de los datos de las pantallas de inicio por rol (SCRUM-53).
 *
 * @author Daniel Blandón
 */
class PanelServiceTest {

    private final IPerfilRepository perfilRepository = mock(IPerfilRepository.class);
    private final IMateriaRepository materiaRepository = mock(IMateriaRepository.class);
    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final IVoluntarioRepository voluntarioRepository = mock(IVoluntarioRepository.class);
    private final PanelService servicio =
            new PanelService(perfilRepository, materiaRepository, usuarioRepository, voluntarioRepository);

    /**
     * Crea el estudiante 3 de los datos de prueba.
     */
    private Estudiante estudiante() {
        Estudiante estudiante = new Estudiante();
        estudiante.setId(3);
        estudiante.setNombreUsuario("estudiante2");
        return estudiante;
    }

    /**
     * El saludo usa el nombre preferido del perfil.
     */
    @Test
    void saludaConElNombrePreferido() {
        Perfil perfil = new Perfil();
        perfil.setNombrePreferido(" Yuli ");
        when(perfilRepository.findById(3)).thenReturn(Optional.of(perfil));

        assertEquals("Yuli", servicio.nombreParaSaludo(estudiante()));
    }

    /**
     * Sin perfil o sin nombre preferido, el saludo usa el nombre de usuario.
     */
    @Test
    void saludaConElNombreDeUsuarioSiNoHayNombrePreferido() {
        when(perfilRepository.findById(3)).thenReturn(Optional.empty());
        assertEquals("estudiante2", servicio.nombreParaSaludo(estudiante()));

        Perfil perfilVacio = new Perfil();
        perfilVacio.setNombrePreferido("  ");
        when(perfilRepository.findById(3)).thenReturn(Optional.of(perfilVacio));
        assertEquals("estudiante2", servicio.nombreParaSaludo(estudiante()));
    }

    /**
     * Las materias se muestran por su nombre y no por su código.
     */
    @Test
    void traduceCodigosDeMateriaANombres() {
        when(materiaRepository.findAll()).thenReturn(List.of(
                new Materia("MAT101", "Cálculo Diferencial"),
                new Materia("ISW203", "Bases de Datos I")));

        Map<String, String> nombres = servicio.nombresDeMaterias();

        assertEquals("Cálculo Diferencial", nombres.get("MAT101"));
        assertEquals("Bases de Datos I", nombres.get("ISW203"));
    }

    /**
     * El resumen del administrador cuenta estudiantes y voluntarios por estado.
     */
    @Test
    void resumenDelAdministrador() {
        when(usuarioRepository.contarEstudiantes()).thenReturn(5L);
        when(voluntarioRepository.countByEstado(EstadoVoluntario.ACTIVO)).thenReturn(2L);
        when(voluntarioRepository.countByEstado(EstadoVoluntario.SUSPENDIDO)).thenReturn(1L);

        assertEquals(new ResumenAdmin(5, 2, 1), servicio.resumenAdmin());
    }
}
