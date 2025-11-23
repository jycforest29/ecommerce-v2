package ecommerce.platform.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserJoinRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String email,
        @NotBlank String phoneNumber
) {
}