package me.kktrkkt.studyolle.infra.mail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor @NoArgsConstructor
public class EmailMessage {

    private String to;

    private String subject;

    private String message;
}
