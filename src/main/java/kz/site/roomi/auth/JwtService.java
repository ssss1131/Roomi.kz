package kz.site.roomi.auth;

import java.time.Instant;

import kz.site.roomi.auth.dto.TokenResponse;
import kz.site.roomi.config.JwtConfig;
import kz.site.roomi.config.JwtProperties;
import kz.site.roomi.user.User;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	private final JwtEncoder encoder;
	private final JwtProperties properties;

	public JwtService(JwtEncoder encoder, JwtProperties properties) {
		this.encoder = encoder;
		this.properties = properties;
	}

	public TokenResponse issue(User user) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(JwtConfig.ISSUER)
				.subject(String.valueOf(user.getId()))
				.issuedAt(now)
				.expiresAt(now.plus(properties.ttl()))
				.claim("email", user.getEmail())
				.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
		return TokenResponse.bearer(token, properties.ttl().toSeconds());
	}

}
