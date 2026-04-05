package clyvasync.Clyvasync.event.auth;

import clyvasync.Clyvasync.entity.auth.User;

public record UserRegisteredEvent(User user) {
}
