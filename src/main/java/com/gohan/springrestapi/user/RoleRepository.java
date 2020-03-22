package com.gohan.springrestapi.user;

import com.gohan.springrestapi.entities.Role;
import org.springframework.data.repository.CrudRepository;

public interface RoleRepository extends CrudRepository<Role, Long> {
}
