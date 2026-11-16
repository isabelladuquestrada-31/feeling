package com.feeling.feeling.repository;

import com.feeling.feeling.model.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio para guardar y consultar los perfiles
 *
 * @author Isabella Duque Estrada
 * @version 1.0
 */

public interface IPerfilRepository extends JpaRepository<Perfil, Integer> {
}
