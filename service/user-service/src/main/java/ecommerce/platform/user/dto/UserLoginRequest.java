package ecommerce.platform.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(
        @NotBlank String userName,
        @NotBlank String password
) {
}
