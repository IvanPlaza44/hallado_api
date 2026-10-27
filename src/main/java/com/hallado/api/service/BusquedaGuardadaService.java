package com.hallado.api.service;

import com.hallado.api.dto.request.BusquedaGuardadaRequestDto;
import com.hallado.api.dto.response.BusquedaGuardadaResponseDto;
import com.hallado.api.entity.BusquedaGuardada;
import com.hallado.api.entity.Publicacion;
import com.hallado.api.entity.Usuario;
import com.hallado.api.enums.TipoPublicacion;
import com.hallado.api.exception.ApiException;
import com.hallado.api.repository.BusquedaGuardadaRepository;
import com.hallado.api.repository.PublicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Búsquedas guardadas = alertas: el usuario guarda los filtros de una búsqueda
 * (igual lógica de "tipo opuesto" que la búsqueda normal) y, cada vez que las lista,
 * se le indica si hay publicaciones nuevas desde la última vez que la revisó.
 */
@Service
@RequiredArgsConstructor
public class BusquedaGuardadaService {

    private final BusquedaGuardadaRepository busquedaGuardadaRepository;
    private final PublicacionRepository publicacionRepository;

    @Transactional
    public BusquedaGuardadaResponseDto crear(Usuario usuario, BusquedaGuardadaRequestDto dto) {
        BusquedaGuardada bg = BusquedaGuardada.builder()
                .usuario(usuario)
                .nombre(dto.nombre())
                .tipo(dto.tipo())
                .categoria(dto.categoria())
                .palabrasClave(dto.palabrasClave())
                .barrio(dto.barrio())
                .rangoFecha(dto.rangoFecha())
                .fechaDesde(dto.fechaDesde())
                .fechaHasta(dto.fechaHasta())
                .build();
        busquedaGuardadaRepository.save(bg);
        return aDto(bg);
    }

    public List<BusquedaGuardadaResponseDto> listar(Usuario usuario) {
        return busquedaGuardadaRepository.findByUsuario(usuario).stream().map(this::aDto).toList();
    }

    @Transactional
    public void marcarRevisada(Usuario usuario, Long id) {
        BusquedaGuardada bg = obtenerPropia(usuario, id);
        bg.setUltimaRevision(LocalDateTime.now());
        busquedaGuardadaRepository.save(bg);
    }

    @Transactional
    public void eliminar(Usuario usuario, Long id) {
        busquedaGuardadaRepository.delete(obtenerPropia(usuario, id));
    }

    private BusquedaGuardada obtenerPropia(Usuario usuario, Long id) {
        BusquedaGuardada bg = busquedaGuardadaRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("Búsqueda guardada no encontrada"));
        if (!bg.getUsuario().getId().equals(usuario.getId())) {
            throw ApiException.noAutorizado("Esta búsqueda guardada no te pertenece");
        }
        return bg;
    }

    private BusquedaGuardadaResponseDto aDto(BusquedaGuardada bg) {
        TipoPublicacion tipoAMostrar = bg.getTipo() == null ? null
                : (bg.getTipo() == TipoPublicacion.PERDIDO ? TipoPublicacion.ENCONTRADO : TipoPublicacion.PERDIDO);

        Specification<Publicacion> spec = Specification.where(PublicacionSpecifications.tipo(tipoAMostrar))
                .and(PublicacionSpecifications.categoria(bg.getCategoria()))
                .and(PublicacionSpecifications.texto(bg.getPalabrasClave()))
                .and(PublicacionSpecifications.barrio(bg.getBarrio()))
                .and(PublicacionSpecifications.publicadaDespuesDe(bg.getUltimaRevision()));

        boolean hayNovedades = publicacionRepository.count(spec) > 0;

        return new BusquedaGuardadaResponseDto(
                bg.getId(), bg.getNombre(), hayNovedades, bg.getTipo(), bg.getCategoria(), bg.getPalabrasClave(),
                bg.getBarrio(), bg.getRangoFecha(), bg.getFechaDesde(), bg.getFechaHasta()
        );
    }
}
