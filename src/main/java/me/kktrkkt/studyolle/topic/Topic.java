package me.kktrkkt.studyolle.topic;

import lombok.*;
import me.kktrkkt.studyolle.infra.entity.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
@Getter @Setter
@Builder @AllArgsConstructor @NoArgsConstructor
public class Topic extends BaseEntity<Topic> {

    @Column(unique = true)
    private String title;
}
