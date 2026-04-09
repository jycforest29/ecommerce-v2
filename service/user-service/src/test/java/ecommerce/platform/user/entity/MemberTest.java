package ecommerce.platform.user.entity;

import ecommerce.platform.user.vo.Email;
import ecommerce.platform.user.vo.Password;
import ecommerce.platform.user.vo.PhoneNumber;
import ecommerce.platform.user.vo.UserName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberTest {

    @Test
    @DisplayName("빌더로 회원을 생성하면 ACTIVE 상태와 joinedAt이 설정된다")
    void createMemberWithDefaults() {
        Member member = Member.builder()
                .userName(new UserName("user01"))
                .password(new Password("encryptedPw"))
                .email(new Email("test@example.com"))
                .phoneNumber(new PhoneNumber("010-1234-5678"))
                .build();

        assertThat(member.getUserName().userName()).isEqualTo("user01");
        assertThat(member.getPassword().password()).isEqualTo("encryptedPw");
        assertThat(member.getEmail().email()).isEqualTo("test@example.com");
        assertThat(member.getPhoneNumber().phoneNumber()).isEqualTo("010-1234-5678");
        assertThat(member.getMemberStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(member.getJoinedAt()).isNotNull();
    }
}