package io.apicurio.hub.api.security;


import io.apicurio.studio.shared.beans.StudioConfigAuth;
import io.apicurio.studio.shared.beans.StudioConfigAuthType;
import io.apicurio.studio.shared.beans.User;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipal;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * This is a simple filter that extracts authentication information from the
 * security context
 *
 * @author carnalca@redhat.com
 */
public class QuarkusAuthenticationFilter implements Filter {

    @Inject
    private ISecurityContext security;

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override public void init(FilterConfig filterConfig) throws ServletException {

    }

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        JWTCallerPrincipal principal = (JWTCallerPrincipal) httpReq.getUserPrincipal();

        if (principal != null) {
            HttpSession httpSession = httpReq.getSession();

            // Fabricate a User object from information in the access token and store it in the request.
            User user = new User();
            user.setEmail(principal.getClaim("email"));
            user.setLogin(principal.getClaim("preferred_username"));
            user.setName(principal.getClaim("name"));
            ((SecurityContext) security).setUser(user);
            ((SecurityContext) security).setToken(principal.getRawToken());

            chain.doFilter(request, response);
        }
    }

    @Override public void destroy() { }
}
