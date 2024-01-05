package me.kktrkkt.studyolle.modules.topic;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topics;

    private final ModelMapper modelMapper;

    public List<Topic> search(String title) {
        return topics.findAllByTitleContains(title);
    }

    public Topic findOrCreateNew(Topic topic, TopicForm topicForm) {
        modelMapper.map(topicForm, topic);
        Optional<Topic> byTitle = topics.findByTitle(topic.getTitle());

        return byTitle.orElseGet(() -> topics.save(topic));
    }
}
