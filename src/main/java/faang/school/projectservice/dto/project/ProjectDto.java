package faang.school.projectservice.dto.project;

import faang.school.projectservice.model.ProjectStatus;
import faang.school.projectservice.model.ProjectVisibility;
import lombok.Data;



@Data
public class ProjectDto {

    private long id;
    private String name;
    private String description;
    private long ownerId;
    private ProjectStatus status;
    private ProjectVisibility visibility;

}
