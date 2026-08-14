package com.ilanacpa.backend.user;

import com.ilanacpa.backend.audit.AuditAction;
import com.ilanacpa.backend.audit.AuditService;
import com.ilanacpa.backend.auth.dto.UserSummary;
import com.ilanacpa.backend.security.UserPrincipal;
import com.ilanacpa.backend.user.dto.CreateUserRequest;
import com.ilanacpa.backend.user.dto.CreateUserResponse;
import com.ilanacpa.backend.user.dto.UpdateUserRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;
    private final AuditService auditService;

    public AdminUserController(UserService userService, AuditService auditService) {
        this.userService = userService;
        this.auditService = auditService;
    }

    @PostMapping
    public CreateUserResponse createUser(@Valid @RequestBody CreateUserRequest request,
                                          @AuthenticationPrincipal UserPrincipal actor,
                                          HttpServletRequest httpRequest) {
        var created = userService.createUserWithTemporaryPassword(request.email(), request.fullName(), request.role());
        auditService.log(AuditAction.USER_CREATED, actor.getId(), "User", created.user().getId().toString(),
                Map.of("email", created.user().getEmail(), "role", created.user().getRole().name()),
                httpRequest.getRemoteAddr());
        return new CreateUserResponse(UserSummary.from(created.user()), created.temporaryPassword());
    }

    @PatchMapping("/{id}")
    public UserSummary updateUser(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request,
                                   @AuthenticationPrincipal UserPrincipal actor,
                                   HttpServletRequest httpRequest) {
        User updated = userService.updateRoleAndStatus(id, request.role(), request.status());
        auditService.log(AuditAction.USER_ROLE_OR_STATUS_CHANGED, actor.getId(), "User", id.toString(),
                Map.of("role", request.role().name(), "status", request.status().name()),
                httpRequest.getRemoteAddr());
        return UserSummary.from(updated);
    }
}
