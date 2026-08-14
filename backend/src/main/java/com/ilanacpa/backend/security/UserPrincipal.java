package com.ilanacpa.backend.security;

import com.ilanacpa.backend.user.Role;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class UserPrincipal extends User {

    private final UUID id;
    private final Role role;

    public UserPrincipal(UUID id, String email, Role role) {
        super(email, "", authorities(role));
        this.id = id;
        this.role = role;
    }

    private static List<GrantedAuthority> authorities(Role role) {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    public UUID getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }
}
