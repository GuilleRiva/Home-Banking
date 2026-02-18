package com.home_banking_.security.user;

import com.home_banking_.enums.Rol;
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

    public static UserDetailsImpl build(Users u) {

        Rol enumRol = u.getRol();

        String roleName= (enumRol != null) ? enumRol.name() :  null;

        List<GrantedAuthority> auths =
                (roleName == null)
                ? List.of()
                        : List.of(new SimpleGrantedAuthority(
                                roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName
                ));

        return new UserDetailsImpl(
                u.getId(),
                u.getEmail(),
                u.getPassword(),
                auths,
                true
        );
    }

    @Override public String getUsername(){ return email;}

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
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
