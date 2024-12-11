package istad.co.projectservice.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "micro_service")
@Getter
@Setter
@NoArgsConstructor
public class MicroService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String uuid;
    @
    Column(nullable = false, unique = true)
    private String name;
    private String namespace;
    private String git;
    private String branch;

    @ManyToOne
    @JoinColumn(name = "sub_workspace_id")
    private SubWorkspace subWorkspace;

}
