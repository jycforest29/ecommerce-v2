package ecommerce.platform.user.dto;

import ecommerce.platform.user.entity.UserEntity;

public record UserInfoResponse (String username) {
    public static UserInfoResponse of(UserEntity userEntity) {
        UserInfoResponse userInfoResponse = new UserInfoResponse(userEntity.getUserName().toString());
        return userInfoResponse;
    }
}
