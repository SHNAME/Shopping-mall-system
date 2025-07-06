package com.example.demo.repository;

import com.example.demo.domain.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDataRepository extends JpaRepository<UserData,Long> {
    Optional<UserData> findById(long id);
    Optional<UserData> findByEmail(String email);

}
