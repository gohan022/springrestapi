package com.gohan.springrestapi.security;

import com.gohan.springrestapi.entities.Session;
import com.gohan.springrestapi.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class SessionService {
    private final SessionRepository sessionRepository;
    private final Logger logger = LoggerFactory.getLogger(SessionService.class);

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public boolean isValid(HttpServletRequest request, String prevAccessToken, String newAccessToken, User user) {
        final String ipAddress = request.getRemoteAddr();
        Session session = this.sessionRepository.findByIpAddressAndPayloadAndUser(ipAddress, prevAccessToken, user).orElse(null);
        if(session != null) {
            session.setPayload(newAccessToken);
            this.sessionRepository.save(session);
            return true;
        }

        return false;
    }

    public void updateSession(HttpServletRequest request, String newAccessToken, User user) {
        final String ipAddress = request.getRemoteAddr();
        Session session = this.sessionRepository.findByIpAddressAndUser(ipAddress, user).orElse(null);

        if(session != null) {
            session.setPayload(newAccessToken);
            this.sessionRepository.save(session);
        } else {
            final String userAgent = request.getHeader("User-Agent");
            Session newSession = new Session(ipAddress, userAgent, newAccessToken, user);
            this.sessionRepository.save(newSession);
        }
    }

    public void clearSession(HttpServletRequest request, User user) {
        final String ipAddress = request.getRemoteAddr();
        this.sessionRepository.findByIpAddressAndUser(ipAddress, user).ifPresent(this.sessionRepository::delete);
    }
}