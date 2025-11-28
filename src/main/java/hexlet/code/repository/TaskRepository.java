package hexlet.code.repository;

import hexlet.code.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    Optional<Task> findByTitle(String title);

    boolean existsByAssigneeId(Long assigneeId);
    boolean existsByTaskStatusId(Long taskStatusId);

    @Query("SELECT COUNT(t) > 0 FROM Task t JOIN t.labels l WHERE l.id = :labelId")
    boolean existsByLabelId(@Param("labelId") Long labelId);
}
