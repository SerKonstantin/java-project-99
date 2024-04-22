package hexlet.code.app.mapper;

import hexlet.code.app.dto.user.UserCreateDTO;
import hexlet.code.app.dto.user.UserDTO;
import hexlet.code.app.dto.user.UserUpdateDTO;
import hexlet.code.app.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        uses = { JsonNullableMapper.class },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class UserMapper {

    @Mapping(target = "encryptedPassword", source = "password")
    public abstract User map(UserCreateDTO dto);

    // Fields are matching, explicit mapping is not required
    public abstract UserDTO map(User model);

    @Mapping(target = "encryptedPassword", source = "password")
    public abstract void update(UserUpdateDTO dto, @MappingTarget User model);
}
