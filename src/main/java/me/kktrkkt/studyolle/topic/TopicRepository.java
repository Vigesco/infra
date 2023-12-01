package me.kktrkkt.studyolle.topic;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    Optional<Topic> findByTitle(String title);

    List<Topic> findAllByTitleContains(String title);
}
