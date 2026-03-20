package ecommerce.platform.user.entity;

import ecommerce.platform.user.vo.Email;
import ecommerce.platform.user.vo.Password;
import ecommerce.platform.user.vo.PhoneNumber;
import ecommerce.platform.user.vo.UserName;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Embedded
    private UserName userName;

    @Embedded
    private Password password;

    @Embedded
    private Email email;

    @Embedded
    private PhoneNumber phoneNumber;

    @Enumerated(EnumType.STRING)
    private MemberStatus memberStatus;

    @Column(nullable = false)
    private Instant joinedAt;

    @Builder
    public Member(UserName userName, Password password, Email email, PhoneNumber phoneNumber) {
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.memberStatus = MemberStatus.ACTIVE;
        this.joinedAt = Instant.now();
    }
}