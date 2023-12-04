package me.kktrkkt.studyolle.zone;

import lombok.*;
import me.kktrkkt.studyolle.infra.entity.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
@Getter @Setter
@Builder @AllArgsConstructor @NoArgsConstructor
public class Zone extends BaseEntity<Zone> {

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String localNameOfCity;

    private String province;

    public String toString(){
        return this.city + "(" + this.localNameOfCity + ")" + "/" + this.province;
    }
}
