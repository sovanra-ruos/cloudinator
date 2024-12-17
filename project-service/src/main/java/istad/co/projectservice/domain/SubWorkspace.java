package istad.co.projectservice.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sub_workspace")
@Getter
@Setter
@NoArgsConstructor
public class SubWorkspace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String uuid;
    @Column(unique = true,nullable = true)
    private String name;
    private String type;

    @ManyToOne
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

}
