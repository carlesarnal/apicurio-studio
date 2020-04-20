package io.apicurio.studio.fe.servlet.filters;

import io.apicurio.studio.fe.servlet.config.RequestAttributeKeys;
import io.apicurio.studio.shared.beans.StudioConfigAuth;
import io.apicurio.studio.shared.beans.StudioConfigAuthType;
import io.apicurio.studio.shared.beans.User;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipal;

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

            // Set the token as a string in the request (as an attribute) for later use.
            StudioConfigAuth auth = new StudioConfigAuth();
            auth.setType(StudioConfigAuthType.token);
            auth.setLogoutUrl(((HttpServletRequest) request).getContextPath() + "/logout");
            auth.setToken(principal.getRawToken());
            //auth.setTokenRefreshPeriod(expirationToRefreshPeriod(principal.getExpirationTime()));
            httpSession.setAttribute(RequestAttributeKeys.AUTH_KEY, auth);

            // Fabricate a User object from information in the access token and store it in the request.
            User user = new User();
            user.setEmail(principal.getClaim("email"));
            user.setLogin(principal.getClaim("preferred_username"));
            user.setName(principal.getClaim("name"));
            httpSession.setAttribute(RequestAttributeKeys.USER_KEY, user);

            chain.doFilter(request, response);
        }
    }

    /**
     * Converts the token expiration time (in seconds) into a refresh period.  The
     * refresh period is simply the # of seconds to wait until a refresh is needed.
     *
     * @param expiration
     */
    private int expirationToRefreshPeriod(int expiration) {
        int nowInSeconds = org.keycloak.common.util.Time.currentTime();
        int expiresInSeconds = expiration;

        if (expiresInSeconds <= nowInSeconds) {
            return 1;
        } else {
            return expiresInSeconds - nowInSeconds;
        }
    }

    @Override public void destroy() {

    }
}
