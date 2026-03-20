package ecommerce.platform.user.dto;

import jakarta.validation.constraints.NotBlank;

public record MemberJoinRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String email,
        @NotBlank String phoneNumber
) {
}
