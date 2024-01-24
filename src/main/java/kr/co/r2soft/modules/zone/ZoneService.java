package kr.co.r2soft.modules.zone;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@Profile({"!test"})
@Service
@RequiredArgsConstructor
public class ZoneService {

    private final ZoneRepository zones;

    @PostConstruct
    public void init() throws IOException {
        if(zones.count() != 0) {
            return;
        }

        Resource resource = new ClassPathResource("list_of_cities_in_south_korea.csv");
        List<String> zoneString = Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8);

        List<Zone> zoneList = zoneString.stream().map(x->{
            String[] split = x.split(",");
            return Zone.builder().city(split[0]).localNameOfCity(split[1]).province(split[2]).build();
        }).collect(Collectors.toList());
        zones.saveAll(zoneList);
    }
}
