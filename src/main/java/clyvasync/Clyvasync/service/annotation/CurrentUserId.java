package clyvasync.Clyvasync.service.annotation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)

@AuthenticationPrincipal(expression = "" +
        "#this instanceof T(org.springframework.security.oauth2.jwt.Jwt) ? claims['user_id'] : (" +
        "#this instanceof T(clyvasync.Clyvasync.security.custom.CustomUserDetails) ? id : (" +
        "#this instanceof T(org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken) ? token.claims['user_id'] : " +
        "hasProperty('claims') ? claims['user_id'] : null))")
public @interface CurrentUserId {
}