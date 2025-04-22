package faang.school.projectservice.dto.project;

import lombok.*;

@Setter
@Getter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEvent {
    Long authorId;
    Long projectId;
}