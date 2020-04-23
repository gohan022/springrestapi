package com.gohan.springrestapi.security;

import com.gohan.springrestapi.entities.Session;
import com.gohan.springrestapi.entities.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SessionRepository extends CrudRepository<Session, Long> {
    Optional<Session> findByIpAddressAndPayloadAndUser(String ipAddress, String payload, User user);

    Optional<Session> findByIpAddressAndUser(String ipAddress, User user);
}
