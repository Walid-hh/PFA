/* package com.covoituragedigitalise.user.mapper;

import com.covoituragedigitalise.user.dto.RegisterRequest;
import com.covoituragedigitalise.user.dto.UserDto;
import com.covoituragedigitalise.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    @Mapping(target = "status", expression = "java(user.getStatus().toString())")
    UserDto toDto(User user);

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "password", ignore = true)
    User toEntity(UserDto userDto);

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isVerified", ignore = true)
    @Mapping(target = "isDriver", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "totalTrips", ignore = true)
    User fromRegisterRequest(RegisterRequest request);

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "email", ignore = true)
    void updateUserFromDto(UserDto userDto, @MappingTarget User user);
} */
package com.covoituragedigitalise.user.mapper;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    // Temporairement vide pour résoudre les erreurs de compilation
    // On implémentera plus tard
}