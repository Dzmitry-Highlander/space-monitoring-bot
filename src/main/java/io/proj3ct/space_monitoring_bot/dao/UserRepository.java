package io.proj3ct.space_monitoring_bot.dao;

import io.proj3ct.space_monitoring_bot.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
}
