package com.feeling.feeling.service;

import com.feeling.feeling.model.AjusteRazonable;
import com.feeling.feeling.model.TipoAjuste;
import com.feeling.feeling.repository.IAjusteRazonableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lógica de los ajustes razonables que se piden junto con una solicitud.
 *
 * @author Yeymi
 */
@Service
public class AjusteRazonableService {

    /**
     * Repositorio para guardar y consultar los ajustes.
     */
    private final IAjusteRazonableRepository ajusteRepository;

    /**
     * Crea el servicio con el repositorio que le entrega Spring.
     *
     * @param ajusteRepository repositorio de ajustes razonables
     */
    public AjusteRazonableService(IAjusteRazonableRepository ajusteRepository) {
        this.ajusteRepository = ajusteRepository;
    }

    /**
     * Revisa los ajustes ANTES de guardar la solicitud. Si el estudiante eligió
     * "Otro" tiene que describir el ajuste.
     *
     * @param tipos       ajustes marcados; puede ser null si no marcó ninguno
     * @param detalleOtro texto escrito para "Otro"
     * @throws IllegalArgumentException si eligió "Otro" sin descripción o con una muy larga
     */
    public void validar(List<TipoAjuste> tipos, String detalleOtro) {
        if (tipos == null || !tipos.contains(TipoAjuste.OTRO)) {
            return;
        }
        if (detalleOtro == null || detalleOtro.trim().isEmpty()) {
            throw new IllegalArgumentException("Describe el ajuste razonable que necesitas.");
        }
        if (detalleOtro.trim().length() > 255) {
            throw new IllegalArgumentException("La descripción del ajuste no puede pasar de 255 caracteres.");
        }
    }

    /**
     * Guarda una fila por cada ajuste marcado, ligada a la solicitud.
     *
     * @param solicitudId id de la solicitud ya guardada
     * @param tipos       ajustes marcados
     * @param detalleOtro texto escrito para "Otro"
     * @throws IllegalArgumentException si los ajustes no pasan la validación
     */
    @Transactional
    public void guardar(Integer solicitudId, List<TipoAjuste> tipos, String detalleOtro) {
        validar(tipos, detalleOtro);
        if (tipos == null) {
            return;
        }
        for (TipoAjuste tipo : tipos) {
            AjusteRazonable ajuste = new AjusteRazonable();
            ajuste.setSolicitudId(solicitudId);
            ajuste.setTipo(tipo);
            if (tipo == TipoAjuste.OTRO) {
                ajuste.setDetalle(detalleOtro.trim());
            }
            ajusteRepository.save(ajuste);
        }
    }

    /**
     * Devuelve los ajustes de una solicitud, para que el voluntario los vea al revisarla.
     *
     * @param solicitudId id de la solicitud
     * @return ajustes de la solicitud; vacía si no tiene ninguno
     */
    public List<AjusteRazonable> ajustesDeLaSolicitud(Integer solicitudId) {
        return ajusteRepository.findBySolicitudId(solicitudId);
    }
}