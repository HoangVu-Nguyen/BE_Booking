package clyvasync.Clyvasync.mapper.media;

import clyvasync.Clyvasync.dto.request.UploadPhotoRequest;
import clyvasync.Clyvasync.dto.response.UserPhotoResponse;
import clyvasync.Clyvasync.modules.media.entity.UserPhoto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserPhotoMapper {
    UserPhotoResponse toUserPhotoResponse(UserPhoto userPhoto);
    UserPhoto toUserPhoto(UploadPhotoRequest uploadPhotoRequest);
}
