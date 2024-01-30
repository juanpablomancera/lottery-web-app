package lottery.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import lottery.pool.Pool;

public interface PoolRepository extends JpaRepository<Pool, Long> {
}
