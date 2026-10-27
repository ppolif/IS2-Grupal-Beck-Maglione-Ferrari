package com.DulceMayor.backend.dtos;

import java.util.List;
import org.mapstruct.MappingTarget;

/**
 * Contrato genérico de mapeo entre Entidades de dominio y objetos de transferencia de datos (DTO).
 *
 * <p><b>Patrón aplicado: DTO (Data Transfer Object) + Mapper.</b></p>
 * <p><b>Por qué se aplica acá:</b> Desacopla el modelo de persistencia interna (entidades JPA,
 * banderas de borrado lógico como {@code eliminado}, relaciones perezosas y campos sensibles)
 * del contrato público de la API REST definido en {@code 04-openapi.yaml}. Al separar
 * {@code Request} (datos de entrada validados) y {@code Response} (datos de salida proyectados)
 * mediante MapStruct en tiempo de compilación, se evita la exposición accidental del modelo
 * de base de datos y vulnerabilidades de asignación masiva (Mass Assignment).</p>
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

