package clyvasync.Clyvasync.event.auth;


import clyvasync.Clyvasync.modules.auth.entity.User;

public record UserRegisteredEvent(User user) {
}
