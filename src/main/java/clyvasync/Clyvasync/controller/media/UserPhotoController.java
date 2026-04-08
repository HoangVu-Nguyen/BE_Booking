package clyvasync.Clyvasync.controller.media;

import clyvasync.Clyvasync.dto.response.ApiResponse;
import clyvasync.Clyvasync.dto.response.UserPhotoResponse;
import clyvasync.Clyvasync.enums.media.ImageType;
import clyvasync.Clyvasync.service.annotation.CurrentUserId;
import clyvasync.Clyvasync.service.media.IUserPhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user-photos")
@RequiredArgsConstructor
public class UserPhotoController {
    private final IUserPhotoService userPhotoService;
    @GetMapping("/avatar")
    public ApiResponse<UserPhotoResponse> getAvatar(@CurrentUserId Long userId) {
        return ApiResponse.success(userPhotoService.getCurrentPhoto(userId, ImageType.AVATAR));
    }

}
