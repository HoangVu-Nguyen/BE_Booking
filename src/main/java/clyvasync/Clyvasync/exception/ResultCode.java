package clyvasync.Clyvasync.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ResultCode {
    SUCCESS(1000, "Success", HttpStatus.OK),
    FAILED(9000, "Operation failed", HttpStatus.INTERNAL_SERVER_ERROR),

    // --- 1xxx: Authentication & Authorization ---
    UNAUTHENTICATED(1001, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(1002, "You do not have permission", HttpStatus.FORBIDDEN),
    TOKEN_EXPIRED(1003, "Token has expired", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(1004, "Invalid token", HttpStatus.UNAUTHORIZED),
    LOGIN_FAILED(1005, "Invalid email or password", HttpStatus.UNAUTHORIZED),
    USER_NOT_ACTIVE(1006, "Account has not been activated", HttpStatus.FORBIDDEN),
    USER_ALREADY_ACTIVE(1007, "Account has already been activated", HttpStatus.BAD_REQUEST),
    LOGOUT_SUCCESS(1008, "Logged out successfully", HttpStatus.OK),
    LOGOUT_FAILED(1009, "Logout failed", HttpStatus.INTERNAL_SERVER_ERROR),

    ACCOUNT_TEMPORARILY_LOCKED(1010, "Account is temporarily locked due to multiple failed login attempts. Please try again later.", HttpStatus.FORBIDDEN),
    DEVICE_NOT_FOUND(1011, "The requested device session could not be found", HttpStatus.NOT_FOUND),
    // Lỗi yêu cầu Captcha
    // --- 2xxx: User & Profile Management (Success & Specific) ---
    REGISTER_SUCCESS(2010, "Registration successful. Please verify your email to activate your account.", HttpStatus.CREATED),
    ACTIVATION_SUCCESS(2011, "Account activated successfully. You can now log in.", HttpStatus.OK),
    PASSWORD_RESET_SUCCESS(2012, "Password reset successfully", HttpStatus.OK),
    PASSWORD_CHANGED_SUCCESS(2013, "Password changed successfully", HttpStatus.OK),
    EMAIL_VERIFICATION_SENT(2014, "Verification email has been sent successfully", HttpStatus.OK),

    // --- 11xx: Verification (OTP / Captcha) ---
    OTP_INVALID(1101, "Invalid verification code", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED(1102, "Verification code has expired", HttpStatus.BAD_REQUEST),
    OTP_VALID(1103, "Verification code is valid", HttpStatus.OK), // <--- THÊM MÃ NÀY
    CAPTCHA_REQUIRED(1104, "Captcha verification is required", HttpStatus.BAD_REQUEST),
    CAPTCHA_INVALID(1105, "Invalid captcha", HttpStatus.BAD_REQUEST),
    EMAIL_IS_SPAM(1106, "Email address is suspected of spam", HttpStatus.BAD_REQUEST),
    PLEASE_WAIT_BEFORE_RESENDING(1107, "Too many requests. Please wait 60 seconds before requesting a new code.", HttpStatus.TOO_MANY_REQUESTS),

    // --- 2xxx: User & Profile Management ---
    USER_NOT_FOUND(2001, "User not found", HttpStatus.NOT_FOUND),
    USER_EXISTED(2002, "User already exists", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_USED(2003, "Email is already in use", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(2004, "Username must be at least 3 characters", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(2005, "Password does not meet length requirements", HttpStatus.BAD_REQUEST),
    PASSWORD_TOO_WEAK(2006, "Password is too weak", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_MATCH(2007, "Password confirmation does not match", HttpStatus.BAD_REQUEST),
    CURRENT_PASSWORD_INCORRECT(2008, "Current password is incorrect", HttpStatus.BAD_REQUEST),
    NEW_PASSWORD_SAME_AS_OLD(2009, "New password cannot be the same as old password", HttpStatus.BAD_REQUEST),

    // --- 3xxx: Post & Content ---
    POST_NOT_FOUND(3001, "Post not found", HttpStatus.NOT_FOUND),
    CANNOT_DELETE_POST(3002, "You do not have permission to delete this post", HttpStatus.FORBIDDEN),
    CANNOT_EDIT_POST(3003, "You do not have permission to edit this post", HttpStatus.FORBIDDEN),
    CONTENT_TOO_LONG(3004, "Content exceeds maximum length", HttpStatus.BAD_REQUEST),

    // --- 4xxx: Messaging & Chat ---
    MESSAGE_NOT_FOUND(4001, "Message not found", HttpStatus.NOT_FOUND),
    MESSAGE_NOT_OWNER(4002, "You are not the owner of this message", HttpStatus.FORBIDDEN),
    MESSAGE_RECALL_EXPIRED(4003, "Message recall time has expired", HttpStatus.BAD_REQUEST),
    MESSAGE_ALREADY_RECALLED(4004, "Message has already been recalled", HttpStatus.BAD_REQUEST),
    CANNOT_SEND_TO_SELF(4005, "You cannot send message to yourself", HttpStatus.BAD_REQUEST),
    MESSAGE_ACCESS_DENIED(4006, "Access denied to this conversation", HttpStatus.FORBIDDEN),

    // --- 5xxx: Social & Relationships ---
    ALREADY_FRIENDS(5001, "You are already friends", HttpStatus.BAD_REQUEST),
    FRIEND_REQUEST_NOT_FOUND(5002, "Friend request not found", HttpStatus.NOT_FOUND),
    USER_BLOCKED(5003, "User is blocked", HttpStatus.BAD_REQUEST),
    CANNOT_PERFORM_ON_SELF(5004, "You cannot perform this action on yourself", HttpStatus.BAD_REQUEST),
    BLOCK_NOT_FOUND(5005, "Block relationship not found", HttpStatus.NOT_FOUND),
    BLOCK_SUCCESS(5010, "User blocked successfully", HttpStatus.OK),
    UNBLOCK_SUCCESS(5011, "User unblocked successfully", HttpStatus.OK),
    CANNOT_BLOCK_ADMIN(5006, "You cannot block an administrator", HttpStatus.FORBIDDEN),
    ALREADY_BLOCKED(5007, "You have already blocked this user", HttpStatus.BAD_REQUEST),
    BLOCKED_BY_USER(5008, "You have been blocked by this user", HttpStatus.FORBIDDEN),
    INTERACTION_RESTRICTED(5009, "Interaction restricted due to block settings", HttpStatus.FORBIDDEN),

    // --- 6xxx: Media & Infrastructure ---
    FILE_TOO_LARGE(6001, "File size exceeds limit", HttpStatus.BAD_REQUEST),
    UNSUPPORTED_FILE_TYPE(6002, "Unsupported file type", HttpStatus.BAD_REQUEST),
    UPLOAD_FAILED(6003, "File upload failed", HttpStatus.INTERNAL_SERVER_ERROR),
    ROLE_NOT_FOUND(6004, "Default role not found", HttpStatus.INTERNAL_SERVER_ERROR),
    RINGTONE_NOT_FOUND(6005, "Default ringtone not found", HttpStatus.INTERNAL_SERVER_ERROR),
    // --- 7xxx: Comments & Interactions ---
    COMMENT_NOT_FOUND(7001, "Comment not found", HttpStatus.NOT_FOUND),
    COMMENT_NOT_OWNER(7002, "You are not the owner of this comment", HttpStatus.FORBIDDEN),
    CANNOT_EDIT_COMMENT(7003, "You do not have permission to edit this comment", HttpStatus.FORBIDDEN),
    CANNOT_DELETE_COMMENT(7004, "You do not have permission to delete this comment", HttpStatus.FORBIDDEN),
    COMMENT_CONTENT_TOO_LONG(7005, "Comment content exceeds maximum length", HttpStatus.BAD_REQUEST),
    INVALID_REACTION_TYPE(7006, "Invalid reaction type", HttpStatus.BAD_REQUEST),
    REACTION_NOT_FOUND(7007, "Reaction not found", HttpStatus.NOT_FOUND),
    NOTIFICATION_NOT_FOUND(7008, "Notification not found", HttpStatus.NOT_FOUND),
    CANNOT_ACCESS_NOTIFICATION(7009, "You do not have permission to access this notification", HttpStatus.FORBIDDEN),
    // --- 8xxx: Validation Errors (Dùng cho @Valid) ---
    INVALID_EMAIL(8001, "Invalid email format", HttpStatus.BAD_REQUEST),
    EMAIL_REQUIRED(8002, "Email is required", HttpStatus.BAD_REQUEST),
    PASSWORD_REQUIRED(8003, "Password is required", HttpStatus.BAD_REQUEST),
    PASSWORD_MIN_8_CHARACTERS(8004, "Password must be at least 8 characters", HttpStatus.BAD_REQUEST),
    USERNAME_REQUIRED(8005, "Username is required", HttpStatus.BAD_REQUEST),
    FIELD_REQUIRED(8006, "This field is required", HttpStatus.BAD_REQUEST),
    INVALID_INPUT(8007, "Invalid input data", HttpStatus.BAD_REQUEST),
    USERNAME_LENGTH_INVALID(8008, "Username length is invalid (3-50 chars)", HttpStatus.BAD_REQUEST),
    CONFIRM_PASSWORD_REQUIRED(8009, "Confirm password is required", HttpStatus.BAD_REQUEST),
    BIRTHDATE_MUST_BE_IN_PAST(8010, "Birthdate must be in the past", HttpStatus.BAD_REQUEST),

    // --- 9xxx: Global / System ---
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REQUEST_FORMAT(9001, "Invalid request format or malformed JSON", HttpStatus.BAD_REQUEST),
    INVALID_KEY(9002, "Invalid message key", HttpStatus.BAD_REQUEST),
    UPDATE_FAILED(9003, "Update operation failed", HttpStatus.INTERNAL_SERVER_ERROR),
    DELETE_FAILED(9004, "Delete operation failed", HttpStatus.INTERNAL_SERVER_ERROR),
    CREATE_FAILED(9005, "Create operation failed", HttpStatus.INTERNAL_SERVER_ERROR),
    DATABASE_ERROR(9006, "Database connection or execution error", HttpStatus.SERVICE_UNAVAILABLE),
    INTERNAL_SERVER_ERROR(9007, "Internal server error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    // --- 10xxx: Reels & Short Videos ---
    REEL_NOT_FOUND(10001, "Reel not found", HttpStatus.NOT_FOUND),
    REEL_UPLOAD_FAILED(10002, "Reel video upload failed", HttpStatus.INTERNAL_SERVER_ERROR),
    REEL_PROCESSING(10003, "Reel is still being processed", HttpStatus.ACCEPTED),
    REEL_FILE_INVALID(10004, "Invalid reel file format or duration", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE_REEL(10005, "You do not have permission to delete this reel", HttpStatus.FORBIDDEN),
    REEL_AI_TRAINING_FAILED(10006, "Failed to trigger AI training for this reel", HttpStatus.SERVICE_UNAVAILABLE),
    REEL_METADATA_INVALID(10007, "Reel metadata is missing or invalid", HttpStatus.BAD_REQUEST),
    // --- 11xxx: Stories (Short-lived Content) ---
    STORY_NOT_FOUND(11001, "Story not found", HttpStatus.NOT_FOUND),
    STORY_EXPIRED(11002, "This story has expired and is no longer available", HttpStatus.GONE),
    STORY_UPLOAD_FAILED(11003, "Failed to upload story media", HttpStatus.INTERNAL_SERVER_ERROR),
    STORY_TOO_MANY_ITEMS(11004, "You have reached the maximum number of stories for today", HttpStatus.BAD_REQUEST),
    CANNOT_VIEW_STORY(11005, "You do not have permission to view this story", HttpStatus.FORBIDDEN),
    STORY_MEDIA_UNSUPPORTED(11006, "Unsupported media format for story", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    STORY_REACTION_FAILED(11007, "Could not process reaction to this story", HttpStatus.BAD_REQUEST),
    // --- 12xxx: Groups & Communities ---
    GROUP_NOT_FOUND(12001, "Group not found", HttpStatus.NOT_FOUND),
    GROUP_EXISTED(12002, "Group name already exists", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE_GROUP(12003, "You do not have permission to delete this group", HttpStatus.FORBIDDEN),
    CANNOT_EDIT_GROUP(12004, "You do not have permission to edit group settings", HttpStatus.FORBIDDEN),
    NOT_A_MEMBER(12005, "You are not a member of this group", HttpStatus.FORBIDDEN),
    ALREADY_A_MEMBER(12006, "You are already a member of this group", HttpStatus.BAD_REQUEST),
    GROUP_INVITATION_NOT_FOUND(12007, "Group invitation not found", HttpStatus.NOT_FOUND),
    ALREADY_INVITED(12008, "User has already been invited to this group", HttpStatus.BAD_REQUEST),
    MEMBER_NOT_FOUND(12009, "Member not found in this group", HttpStatus.NOT_FOUND),
    GROUP_ACCESS_DENIED(12010, "This group is private. You must be a member to view content", HttpStatus.FORBIDDEN),
    CANNOT_LEAVE_GROUP_CREATOR(12011, "Creator cannot leave the group. Please transfer ownership first", HttpStatus.BAD_REQUEST),
    GROUP_MAX_MEMBERS_REACHED(12012, "Group has reached the maximum number of members", HttpStatus.BAD_REQUEST),
    INVITATION_EXPIRED(12013, "Group invitation has expired", HttpStatus.BAD_REQUEST),
    NOT_AN_ADMIN(12014, "You must be an administrator to perform this action", HttpStatus.FORBIDDEN),
    CANNOT_INVITE_SELF(12015, "You cannot invite yourself to a group", HttpStatus.BAD_REQUEST),
    // --- 12xxx: Groups & Communities ---
    INVITATION_ALREADY_PROCESSED(12016, "This invitation has already been accepted or rejected", HttpStatus.GONE),
    INVITATION_NOT_FOUND(12017, "No invitation found for this group", HttpStatus.NOT_FOUND),
    NOT_AN_INVITEE(12018, "You are not in the invitee list for this group", HttpStatus.FORBIDDEN),
    ALREADY_A_MEMBER_REQUEST_CLEARED(12020, "User is already a member; the pending request has been cleared", HttpStatus.BAD_REQUEST),
    JOIN_REQUEST_NOT_FOUND(12019, "The join request does not exist or was previously cancelled", HttpStatus.NOT_FOUND),

    MEMBER_REMOVAL_FAILED(12021, "Failed to process group departure", HttpStatus.INTERNAL_SERVER_ERROR),

    // --- 13xxx: Authorization & Permission System (SpiceDB Integration) ---
    PERMISSION_DENIED(13001, "You do not have the required permission to perform this action", HttpStatus.FORBIDDEN),
    UNAUTHORIZED_ACCESS(13002, "Unauthorized access to this resource", HttpStatus.UNAUTHORIZED),
    RESOURCE_OWNER_REQUIRED(13003, "Only the owner of this resource can perform this action", HttpStatus.FORBIDDEN),
    SPICEDB_CONNECTION_ERROR(13004, "Security service connection failed", HttpStatus.SERVICE_UNAVAILABLE),
    SPICEDB_WRITE_ERROR(13005, "Failed to synchronize permissions", HttpStatus.INTERNAL_SERVER_ERROR),
    SPICEDB_SCHEMA_VIOLATION(13006, "Permission configuration mismatch", HttpStatus.INTERNAL_SERVER_ERROR),
    ROLE_INSUFFICIENT(13007, "Your current role does not have enough authority", HttpStatus.FORBIDDEN),
    GROUP_MEMBER_REQUIRED(13008, "This action requires group membership", HttpStatus.FORBIDDEN),
    FRIENDSHIP_REQUIRED(13009, "Only friends can interact with this content", HttpStatus.FORBIDDEN),
    SPICEDB_READ_ERROR(13011, "Không thể kiểm tra quyền hạn lúc này", HttpStatus.INTERNAL_SERVER_ERROR),
    PERMISSION_REVC_FAILED(13012, "Thu hồi quyền hạn thất bại", HttpStatus.INTERNAL_SERVER_ERROR),

    PERMISSION_SYNC_PENDING(13010, "Your permissions are being updated, please try again in a moment", HttpStatus.ACCEPTED);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ResultCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
