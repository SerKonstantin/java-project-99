package hexlet.code.mapper;

import hexlet.code.dto.label.LabelInputDTO;
import hexlet.code.dto.label.LabelDTO;
import hexlet.code.model.Label;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        uses = { ReferenceMapper.class },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class LabelMapper {
    public abstract Label map(LabelInputDTO dto);

    public abstract LabelDTO map(Label model);

    public abstract void update(LabelInputDTO dto, @MappingTarget Label model);
}
