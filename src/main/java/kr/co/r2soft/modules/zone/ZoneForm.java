package kr.co.r2soft.modules.zone;

import lombok.Data;

@Data
public class ZoneForm {

    private String zoneName;

    public String getCity() {
        return this.zoneName.substring(0, zoneName.indexOf("("));
    }

    public String getProvince() {
        return this.zoneName.substring(zoneName.indexOf("/")+1);
    }
}
