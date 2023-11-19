package me.kktrkkt.studyolle.account;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class ProfileUpdateForm {

    @Size(max = 35, message = "Bio length should be less than 35 characters")
    private String bio;

    @Size(max = 50, message = "Url length should be less than 50 characters")
    private String url;

    @Size(max = 50, message = "Url length should be less than 50 characters")
    private String occupation;

    @Size(max = 50, message = "Location length should be less than 50 characters")
    private String location;

    private String profileImage;

    public ProfileUpdateForm(Account account) {
        this.bio = account.getBio();
        this.url = account.getUrl();
        this.occupation = account.getOccupation();
        this.location = account.getLocation();
        this.profileImage = account.getProfileImage();
    }
}
