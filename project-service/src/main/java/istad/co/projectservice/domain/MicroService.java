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
    @Column(nullable = false, unique = true)

    private String uuid;
    @Column(nullable = false, length = 64)
    private String name;

    @Column(nullable = false, length = 64)
    private String branch;

    @Column(nullable = false, length = 64)
    private String namespace;

    @Column(nullable = false, length = 64)
    private String git;

    @ManyToOne
    @JoinColumn(name = "sub_workspace_id")
    private SubWorkspace subWorkspace;

}
