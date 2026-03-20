package ecommerce.platform.user.dto;

import ecommerce.platform.user.entity.Member;

public record MemberQueryResponse(String username) {
    public static MemberQueryResponse of(Member member) {
        return new MemberQueryResponse(member.getUserName().toString());
    }
}
