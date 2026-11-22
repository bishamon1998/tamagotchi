package com.tamagotchi.miniapp;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;

/**
 * Lets API controllers take a {@link TelegramUser} parameter. The Mini App sends its launch data as
 * {@code Authorization: tma <initData>}; requests without valid data get 401.
 */
@Component
class TelegramUserResolver implements HandlerMethodArgumentResolver {

    private static final String SCHEME = "tma ";

    private final InitDataValidator validator;

    TelegramUserResolver(InitDataValidator validator) {
        this.validator = validator;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType() == TelegramUser.class;
    }

    @Override
    public TelegramUser resolveArgument(MethodParameter parameter, ModelAndViewContainer container,
                                        NativeWebRequest request, WebDataBinderFactory binderFactory) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(SCHEME)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Open the app from Telegram");
        }
        return validator.validate(header.substring(SCHEME.length()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Telegram sign-in is invalid or expired"));
    }
}
