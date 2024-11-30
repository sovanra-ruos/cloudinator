package istad.co.projectservice.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "service_type")
@Getter
@Setter
@NoArgsConstructor
public class ServiceType {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;
    private String uuid;
    private String name;

}
