package com.home_banking_.security.user;

import com.home_banking_.enums.Rol;
import com.home_banking_.enums.UserStatus;
import com.home_banking_.model.Users;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;
    private final boolean accountNonLocked;

    public static UserDetailsImpl build(Users u) {

        Rol enumRol = u.getRol();

        List<GrantedAuthority> auths =
                enumRol == null
                        ? List.of()
                        : List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_" + enumRol.name()
                        )
                );

        return new UserDetailsImpl(
                u.getId(),
                u.getEmail(),
                u.getPassword(),
                auths,
                u.getUserStatus() == UserStatus.ACTIVE,
                !u.isAccountLocked()
        );
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}