package com.home_banking_.repository;

import com.home_banking_.dto.response.UserResponseDto;
import com.home_banking_.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {

    Optional <Users> findByEmail(String email);

    /*@Query("select new com.home_banking_.dto.response.UserResponseDto(u.id, u.name, u.surname, u.email, u.registrationDate, u.rol) from Users u")

    List<UserResponseDto> findAllAsDto();*/
}
