package istad.co.projectservice.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "deploy_service")
@Getter
@Setter
@NoArgsConstructor
public class DeployService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String uuid;
    @Column(unique = true, nullable = false)
    private String name;
    @Column(nullable = false)
    private String gitUrl;
    @Column(nullable = false)
    private String branch;
    @Column(unique = true, nullable = false)
    private String subdomain;

    private Boolean status;

    private String type;

    @ManyToOne
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;
}
