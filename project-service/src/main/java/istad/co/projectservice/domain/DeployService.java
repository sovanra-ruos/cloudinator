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
    private String name;
    private String gitUrl;
    private String branch;
    @Column(unique = true)
    private String subdomain;

    @ManyToOne
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;
}
