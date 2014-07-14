package com.brienwheeler.web.spring.security;

import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RestAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler
{
    public RestAuthenticationSuccessHandler() {
        super.setRedirectStrategy(new NoopRedirectStrategy());
    }

    @Override
    public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
        // can't replace NoopRedirectStrategy
    }

    private static final class NoopRedirectStrategy implements RedirectStrategy {
        @Override
        public void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url)
                throws IOException
        {
            // do nothing
        }
    }
}
