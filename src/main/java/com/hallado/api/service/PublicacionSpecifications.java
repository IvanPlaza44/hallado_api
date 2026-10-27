package com.hallado.api.service;

import com.hallado.api.entity.Publicacion;
import com.hallado.api.enums.CategoriaItem;
import com.hallado.api.enums.EstadoPublicacion;
import com.hallado.api.enums.TipoPublicacion;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class PublicacionSpecifications {

    private PublicacionSpecifications() {}

    public static Specification<Publicacion> estado(EstadoPublicacion estado) {
        return (root, query, cb) -> estado == null ? null : cb.equal(root.get("estado"), estado);
    }

    public static Specification<Publicacion> tipo(TipoPublicacion tipo) {
        return (root, query, cb) -> tipo == null ? null : cb.equal(root.get("tipo"), tipo);
    }

    public static Specification<Publicacion> categoria(CategoriaItem categoria) {
        return (root, query, cb) -> categoria == null ? null : cb.equal(root.get("categoria"), categoria);
    }

    public static Specification<Publicacion> barrio(String barrio) {
        return (root, query, cb) -> (barrio == null || barrio.isBlank())
                ? null
                : cb.equal(cb.lower(root.get("barrio")), barrio.toLowerCase());
    }

    public static Specification<Publicacion> texto(String palabrasClave) {
        return (root, query, cb) -> {
            if (palabrasClave == null || palabrasClave.isBlank()) return null;
            String like = "%" + palabrasClave.toLowerCase() + "%";
            Predicate porTitulo = cb.like(cb.lower(root.get("titulo")), like);
            Predicate porDescripcion = cb.like(cb.lower(root.get("descripcion")), like);
            Predicate porPalabrasClave = cb.like(cb.lower(root.get("palabrasClave")), like);
            return cb.or(porTitulo, porDescripcion, porPalabrasClave);
        };
    }

    public static Specification<Publicacion> fechaSucesoDesde(LocalDate desde) {
        return (root, query, cb) -> desde == null ? null : cb.greaterThanOrEqualTo(root.get("fechaSuceso"), desde);
    }

    public static Specification<Publicacion> fechaSucesoHasta(LocalDate hasta) {
        return (root, query, cb) -> hasta == null ? null : cb.lessThanOrEqualTo(root.get("fechaSuceso"), hasta);
    }

    public static Specification<Publicacion> publicadaDespuesDe(java.time.LocalDateTime momento) {
        return (root, query, cb) -> momento == null ? null : cb.greaterThan(root.get("fechaPublicacion"), momento);
    }
}
