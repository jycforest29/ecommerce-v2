package ecommerce.platform.common.util;

import ecommerce.platform.common.exception.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class EntityFinderTest {

    @Mock
    private JpaRepository<String, Long> repository;

    @Test
    @DisplayName("ID로 엔티티를 찾으면 반환한다")
    void findEntity() {
        given(repository.findById(1L)).willReturn(Optional.of("found"));

        String result = EntityFinder.findEntity(repository, 1L);

        assertThat(result).isEqualTo("found");
    }

    @Test
    @DisplayName("ID로 엔티티를 찾지 못하면 EntityNotFoundException 발생")
    void findEntityNotFound() {
        given(repository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> EntityFinder.findEntity(repository, 999L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}