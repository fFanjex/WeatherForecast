package ru.ffanjex.weatherforecast.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import ru.ffanjex.weatherforecast.dto.enums.Sex;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "clothes_example")
public class ClothesExample {

    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "sex", nullable = false)
    private Sex sex;

    @Column(name = "season", nullable = false)
    private String season;

    @Column(name = "style")
    private String style;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "title")
    private String title;
}
