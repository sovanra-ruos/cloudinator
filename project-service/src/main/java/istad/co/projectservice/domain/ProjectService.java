package istad.co.projectservice.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "service")
@Setter
@Getter
@NoArgsConstructor
public class ProjectService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String uuid;
    private String name;
    private String namespace;
    private String git;
    private String branch;
    private String token;

    @ManyToOne
    @JoinColumn(name = "service_type_id")
    private ServiceType serviceType;


}
