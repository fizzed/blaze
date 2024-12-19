package com.fizzed.blaze.postoffice;

import com.fizzed.blaze.Config;
import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.Action;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.core.VerbosityMixin;
import com.fizzed.blaze.util.*;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class Mail extends Action<Mail.Result,Integer> implements VerbosityMixin<Mail> {

    static private final Map<String,Session> SESSIONS = new ConcurrentHashMap<>();

    static public class Result extends com.fizzed.blaze.core.Result<Mail,Integer,Result> {

        Result(Mail action, Integer value) {
            super(action, value);
        }

    }

    private final VerboseLogger log;
    private String smtpHost;
    private Integer smtpPort;
    private Boolean smtpAuth;
    private Boolean smtpStartTls;
    private Boolean smtpSsl;
    private Boolean smtpSslInsecure;
    private String smtpUsername;
    private String smtpPassword;

    private String from;
    private List<String> to;
    private List<String> cc;
    private List<String> bcc;
    private String subject;
    private String textBody;
    private String htmlBody;

    public Mail(Context context) {
        super(context);
        this.log = new VerboseLogger(this);

        // initialize the host, port, etc. from config
        final Config config = context.config();
        this.smtpHost = config.value("postoffice.smtp.host").orNull();
        this.smtpPort = config.value("postoffice.smtp.port", Integer.class).orNull();
        this.smtpAuth = config.value("postoffice.smtp.auth", Boolean.class).orNull();
        this.smtpStartTls = config.value("postoffice.smtp.start_tls", Boolean.class).orNull();
        this.smtpSsl = config.value("postoffice.smtp.ssl", Boolean.class).orNull();
        this.smtpSslInsecure = config.value("postoffice.smtp.ssl_insecure", Boolean.class).orNull();
        this.smtpUsername = config.value("postoffice.smtp.username").orNull();
        this.smtpPassword = config.value("postoffice.smtp.password").orNull();
    }

    public VerboseLogger getVerboseLogger() {
        return this.log;
    }

    public Mail smtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
        return this;
    }

    public Mail smtpPort(Integer smtpPort) {
        this.smtpPort = smtpPort;
        return this;
    }

    public Mail smtpAuth(Boolean smtpAuth) {
        this.smtpAuth = smtpAuth;
        return this;
    }

    public Mail smtpStartTls(Boolean smtpStartTls) {
        this.smtpStartTls = smtpStartTls;
        return this;
    }

    public Mail smtpSsl(Boolean smtpSsl) {
        this.smtpSsl = smtpSsl;
        return this;
    }

    public Mail smtpSslInsecure(Boolean smtpSslInsecure) {
        this.smtpSslInsecure = smtpSslInsecure;
        return this;
    }

    public Mail smtpUsername(String smtpUsername) {
        this.smtpUsername = smtpUsername;
        return this;
    }

    public Mail smtpPassword(String smtpPassword) {
        this.smtpPassword = smtpPassword;
        return this;
    }

    public Mail from(String from) {
        this.from = from;
        return this;
    }

    public Mail to(String... to) {
        return this.to(asList(to));
    }

    public Mail to(List<String> to) {
        this.to = to;
        return this;
    }

    public Mail addTo(String to) {
        if (this.to == null) {
            this.to = new ArrayList<>();
        }
        this.to.add(to);
        return this;
    }

    public Mail cc(String... cc) {
        return this.cc(asList(cc));
    }

    public Mail cc(List<String> cc) {
        this.cc = cc;
        return this;
    }

    public Mail addCc(String cc) {
        if (this.cc == null) {
            this.cc = new ArrayList<>();
        }
        this.cc.add(cc);
        return this;
    }

    public Mail bcc(String... bcc) {
        return this.bcc(asList(bcc));
    }

    public Mail bcc(List<String> bcc) {
        this.bcc = bcc;
        return this;
    }

    public Mail addBcc(String bcc) {
        if (this.bcc == null) {
            this.bcc = new ArrayList<>();
        }
        this.bcc.add(bcc);
        return this;
    }

    public Mail subject(String subject) {
        this.subject = subject;
        return this;
    }

    public Mail textBody(String textBody) {
        this.textBody = textBody;
        return this;
    }

    public Mail htmlBody(String htmlBody) {
        this.htmlBody = htmlBody;
        return this;
    }

    @Override
    protected Result doRun() throws BlazeException {
        final Properties properties = new Properties();
        if (this.smtpHost != null) {
            properties.put("mail.smtp.host", this.smtpHost);
        }
        if (this.smtpPort != null) {
            properties.put("mail.smtp.port", this.smtpPort);
        }
        if (this.smtpAuth != null) {
            properties.put("mail.smtp.auth", this.smtpAuth);
        }
        if (this.smtpStartTls != null) {
            properties.put("mail.smtp.starttls.enable", this.smtpStartTls);
        }
        if (this.smtpSsl != null) {
            properties.put("mail.smtp.ssl.enable", this.smtpSsl);
        }
        if (this.smtpSslInsecure != null) {
            // if insecure set we do NOT want to check the server identity
            properties.put("mail.smtp.ssl.checkserveridentity", !this.smtpSslInsecure);
        }

        // why on earth are these "infinite" by default?
        properties.put("mail.smtp.connectiontimeout", 60000);
        properties.put("mail.smtp.timeout", 60000);
        properties.put("mail.smtp.writetimeout", 60000);

        final Timer timer = new Timer();
        log.verbose("Sending mail: to={}, subject={}", this.to, this.subject);

        // get or create new session
        final String sessionIdentifier = this.buildSessionIdentifier(properties, this.smtpUsername, this.smtpPassword);
        Session session = SESSIONS.get(sessionIdentifier);
        if (session == null) {
            if (this.smtpUsername != null && this.smtpPassword != null) {
                session = Session.getInstance(properties, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(smtpUsername, smtpPassword);
                    }
                });
            } else {
                session = Session.getInstance(properties);
            }
            SESSIONS.put(sessionIdentifier, session);
            log.verbose("Using new mail session: {}", session);
        } else {
            log.verbose("Using cached mail session: {}", session);
        }

        if (log.isDebug()) {
            session.setDebug(true);
        }

        final Message message = new MimeMessage(session);

        if (this.from != null) {
            try {
                message.setFrom(new InternetAddress(this.from));
            } catch (MessagingException e) {
                throw new BlazeException("Failed setting FROM address from value '" + this.from + "': " + e.getMessage(), e);
            }
        }

        this.setRecipients(this.to, message, Message.RecipientType.TO);
        this.setRecipients(this.cc, message, Message.RecipientType.CC);
        this.setRecipients(this.bcc, message, Message.RecipientType.BCC);

        try {
            message.setSubject(this.subject);
        } catch (MessagingException e) {
            throw new BlazeException("Failed setting subject from value '" + this.subject + "': " + e.getMessage(), e);
        }

        // you need text or html or both
        if (this.textBody == null && this.htmlBody == null) {
            throw new BlazeException("A text and/or html body are required");
        }

        final Multipart multipart = new MimeMultipart();

        if (this.textBody != null) {
            try {
                MimeBodyPart mimeBodyPart = new MimeBodyPart();
                mimeBodyPart.setContent(this.textBody, "text/plain; charset=utf-8");
                multipart.addBodyPart(mimeBodyPart);
            } catch (MessagingException e) {
                throw new BlazeException("Failed setting text body: " + e.getMessage(), e);
            }
        }

        if (this.htmlBody != null) {
            try {
                MimeBodyPart mimeBodyPart = new MimeBodyPart();
                mimeBodyPart.setContent(this.htmlBody, "text/html; charset=utf-8");
                multipart.addBodyPart(mimeBodyPart);
            } catch (MessagingException e) {
                throw new BlazeException("Failed setting html body: " + e.getMessage(), e);
            }
        }

        try {
            message.setContent(multipart);
        } catch (MessagingException e) {
            throw new BlazeException("Failed setting multipart message: " + e.getMessage(), e);
        }

        try {
            Transport.send(message);
            log.verbose("Successfully sent mail: to={}, subject={} (in {})", this.to, this.subject, timer);
        } catch (MessagingException e) {
            throw new BlazeException("Failed to send mail: to="
                + this.to + ", subject=" + this.subject + ", error=" + e.getMessage () + " (in " + timer + ")", e);
        }

        return new Result(this, 0);
    }

    private void setRecipients(List<String> addresses, Message message, Message.RecipientType recipientType) {
        if (addresses != null && !addresses.isEmpty()) {
            for (final String a : addresses) {
                try {
                    message.addRecipient(recipientType, new InternetAddress(a));
                } catch (MessagingException e) {
                    throw new BlazeException("Failed setting " + recipientType + " address from value '" + a + "': " + e.getMessage(), e);
                }
            }
        }
    }

    private String buildSessionIdentifier(Properties props, String username, String password) {
        StringBuilder identifier = new StringBuilder();
        List<Object> sortedKeys = props.keySet().stream()
            .sorted()
            .collect(Collectors.toList());

        for (Object sortedKey : sortedKeys) {
            Object value = props.get(sortedKey);
            if (identifier.length() > 0) {
                identifier.append(";");
            }
            identifier.append(sortedKey).append("=").append(value);
        }

        if (username != null && password != null) {
            if (identifier.length() > 0) {
                identifier.append(";");
            }
            identifier.append("username=").append(username).append(";").append("password_hash=").append(password.hashCode());
        }

        return identifier.toString();
    }

}