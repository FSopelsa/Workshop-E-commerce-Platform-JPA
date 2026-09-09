package se.lexicon.ecommerce.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user_profiles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nickname;

    @Column(name = "phone_number", nullable = false, length = 100)
    private String phoneNumber;

    @Column(length = 500)
    private String bio;

    public UserProfile(String nickname, String phoneNumber, String bio) {
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.bio = bio;
    }
}
