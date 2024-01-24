package kr.co.r2soft.modules.zone;

import lombok.*;
import kr.co.r2soft.infra.entity.BaseEntity;

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
        return String.format("%s(%s)/%s", this.city, this.localNameOfCity, this.province);
    }
}
