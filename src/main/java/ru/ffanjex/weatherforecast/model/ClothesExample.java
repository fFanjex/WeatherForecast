package ru.ffanjex.weatherforecast.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import ru.ffanjex.weatherforecast.dto.enums.Sex;

import java.util.UUID;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "clothes_example")
public class ClothesExample {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "sex", nullable = false)
    private Sex sex;

    @Column(name = "season", nullable = false)
    private String season;

    @Column(name = "item_type", nullable = false)
    private String itemType;

    @Column(name = "style")
    private String style;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "title")
    private String title;
}