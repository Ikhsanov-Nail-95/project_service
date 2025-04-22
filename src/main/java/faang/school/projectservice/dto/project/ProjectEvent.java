package faang.school.projectservice.dto.project;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEvent {
    Long authorId;
    Long projectId;
}