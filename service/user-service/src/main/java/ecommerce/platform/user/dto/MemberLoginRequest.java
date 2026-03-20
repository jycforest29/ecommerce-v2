package ecommerce.platform.user.dto;

import jakarta.validation.constraints.NotBlank;

public record MemberLoginRequest(
        @NotBlank String userName,
        @NotBlank String password
) {
}
