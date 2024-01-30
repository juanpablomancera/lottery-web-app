package lottery.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import lottery.user.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
