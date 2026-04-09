package ecommerce.platform.common.util;

import ecommerce.platform.common.exception.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class EntityFinder {

    public static <T, V> T findEntity(JpaRepository<T, V> repository, V id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException());
    }

}
