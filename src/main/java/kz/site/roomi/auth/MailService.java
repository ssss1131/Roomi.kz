package kz.site.roomi.auth;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import kz.site.roomi.config.AppMailProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

	private static final Logger log = LoggerFactory.getLogger(MailService.class);

	private final ObjectProvider<JavaMailSender> mailSender;
	private final AppMailProperties properties;

	public MailService(ObjectProvider<JavaMailSender> mailSender, AppMailProperties properties) {
		this.mailSender = mailSender;
		this.properties = properties;
	}

	public void sendConfirmation(String email, String token) {
		String link = buildLink(token);
		if (!properties.enabled()) {
			log.info("Mail disabled. Confirmation link for {}: {}", email, link);
			return;
		}
		JavaMailSender sender = mailSender.getIfAvailable();
		if (sender == null) {
			log.warn("JavaMailSender is not configured. Confirmation link for {}: {}", email, link);
			return;
		}
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(properties.from());
			message.setTo(email);
			message.setSubject("Roomi.kz: confirm your email");
			message.setText("""
					Welcome to Roomi.kz!

					Please confirm your email by opening the link below:
					%s

					The link is valid for 24 hours. If you did not register, just ignore this email.
					""".formatted(link));
			sender.send(message);
			log.info("Confirmation email sent to {}", email);
		}
		catch (Exception ex) {
			log.error("Failed to send confirmation email to {}", email, ex);
		}
	}

	private String buildLink(String token) {
		String base = properties.confirmUrl();
		String separator = base.contains("?") ? "&" : "?";
		return base + separator + "token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
	}

}
