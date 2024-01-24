package kr.co.r2soft.modules.topic;

import lombok.*;
import kr.co.r2soft.infra.entity.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
@Getter @Setter
@Builder @AllArgsConstructor @NoArgsConstructor
public class Topic extends BaseEntity<Topic> {

    @Column(unique = true)
    private String title;
}
