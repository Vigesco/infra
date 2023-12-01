package me.kktrkkt.studyolle.topic;

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

    public Topic findOrCreateNew(Topic topic, Object update) {
        modelMapper.map(update, topic);
        Optional<Topic> byTitle = topics.findByTitle(topic.getTitle());

        if(byTitle.isEmpty()){
            return topics.save(topic);
        }

        return byTitle.get();
    }
}
