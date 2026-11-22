package com.feeling.feeling.service;

import com.feeling.feeling.model.TipoAjuste;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AjusteRazonableServiceTest {

    /** validar() no usa el repositorio, por eso se puede pasar null. */
    private final AjusteRazonableService service = new AjusteRazonableService(null);

    @Test
    void casoDeFalloOtroSinDetalle() {
        final List<TipoAjuste> tipos = Arrays.asList(TipoAjuste.OTRO);
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() {
                service.validar(tipos, "   ");
            }
        });
    }

    @Test
    void casoDeExitoAjusteSinOtro() {
        final List<TipoAjuste> tipos = Arrays.asList(TipoAjuste.ESPACIO_ACCESIBLE);
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                service.validar(tipos, null);
            }
        });
    }

    @Test
    void casoDeExitoOtroConDetalle() {
        final List<TipoAjuste> tipos = Arrays.asList(TipoAjuste.OTRO);
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                service.validar(tipos, "Necesito material con letra grande");
            }
        });
    }
}