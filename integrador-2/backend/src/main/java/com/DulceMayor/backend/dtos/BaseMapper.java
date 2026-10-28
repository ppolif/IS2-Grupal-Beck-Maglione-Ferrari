package com.DulceMayor.backend.dtos;

import java.util.List;
import org.mapstruct.MappingTarget;

/**
 * Contrato genérico de mapeo entre Entidades de dominio y objetos de transferencia de datos (DTO).
 *
 * <p><b>Patrón GoF aplicado: Adapter (Estructural) / Objeto Adaptador</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> Target (interfaz objetivo) y Adapter (implementaciones MapStruct).</li>
 *   <li><b>Target:</b> Contratos {@code Req} y {@code Res} requeridos por la capa web/OpenAPI.</li>
 *   <li><b>Adaptee:</b> Entidades JPA (modelo de persistencia relacional con borrado lógico y relaciones perezosas).</li>
 *   <li><b>Rol:</b> Adapta la estructura interna y compleja del modelo de persistencia (JPA) a los tipos
 *       inmutables esperados por la capa de presentación (DTO Records), garantizando que las capas
 *       controladoras nunca interactúen con entidades de persistencia.</li>
 * </ul>
 *
 * <p><b>Patrón de diseño: DTO (Data Transfer Object)</b></p>
 * Separa los datos de entrada validados ({@code Request}) de los datos proyectados para salida ({@code Response}),
 * previniendo la exposición de datos sensibles y ataques de asignación masiva.
 *
 * @param <E>   Tipo de la entidad de dominio.
 * @param <Req> Tipo del DTO de entrada (Request).
 * @param <Res> Tipo del DTO de salida (Response).
 */
public interface BaseMapper<E, Req, Res> {

    E aEntidad(Req request);

    void actualizarEntidad(Req request, @MappingTarget E entidad);

    Res aResponse(E entidad);

    List<Res> aResponseList(List<E> entidades);
}

