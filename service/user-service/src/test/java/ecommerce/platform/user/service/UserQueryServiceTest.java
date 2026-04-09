package ecommerce.platform.user.service;

import ecommerce.platform.common.exception.EntityNotFoundException;
import ecommerce.platform.user.dto.MemberQueryResponse;
import ecommerce.platform.user.entity.Member;
import ecommerce.platform.user.repository.UserRepository;
import ecommerce.platform.user.vo.Email;
import ecommerce.platform.user.vo.Password;
import ecommerce.platform.user.vo.PhoneNumber;
import ecommerce.platform.user.vo.UserName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {

    @InjectMocks
    private UserQueryService userQueryService;

    @Mock
    private UserRepository userRepository;

    private Member createMember(Long id) {
        Member member = Member.builder()
                .userName(new UserName("user01"))
                .password(new Password("encrypted"))
                .email(new Email("test@example.com"))
                .phoneNumber(new PhoneNumber("010-1234-5678"))
                .build();
        ReflectionTestUtils.setField(member, "memberId", id);
        return member;
    }

    @Test
    @DisplayName("사용자 ID로 회원 정보를 조회한다")
    void getUserSuccess() {
        Member member = createMember(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(member));

        MemberQueryResponse response = userQueryService.getUser(1L);

        assertThat(response.username()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 예외가 발생한다")
    void getUserNotFound() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userQueryService.getUser(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}