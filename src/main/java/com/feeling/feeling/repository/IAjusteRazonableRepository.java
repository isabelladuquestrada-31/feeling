package com.feeling.feeling.repository;

import com.feeling.feeling.model.AjusteRazonable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Consulta y guarda los ajustes razonables.
 *
 * @author Yeymi
 */
public interface IAjusteRazonableRepository extends JpaRepository<AjusteRazonable, Integer> {

    /**
     * @param solicitudId id de la solicitud
     * @return los ajustes de esa solicitud; vacía si no tiene ninguno
     */
    List<AjusteRazonable> findBySolicitudId(Integer solicitudId);
}