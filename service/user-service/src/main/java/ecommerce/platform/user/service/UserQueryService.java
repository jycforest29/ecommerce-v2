package ecommerce.platform.user.service;

import ecommerce.platform.common.exception.EntityNotFoundException;
import ecommerce.platform.user.dto.UserInfoResponse;
import ecommerce.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {
    private final UserRepository userRepository;

    public UserInfoResponse getUser(String username) {
        return userRepository.findByUserName(username)
                .map(UserInfoResponse::of)
                .orElseThrow(() -> new EntityNotFoundException());
    }
}
