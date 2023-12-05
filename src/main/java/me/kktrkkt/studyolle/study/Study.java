package me.kktrkkt.studyolle.study;

import lombok.*;
import me.kktrkkt.studyolle.infra.entity.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity
@Getter @Setter
@Builder @AllArgsConstructor @NoArgsConstructor
public class Study extends BaseEntity<Study> {

    @Column(unique = true)
    private String url;

    private String title;

    private String bio;

    @Lob
    private String explanation;
}
