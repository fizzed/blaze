package com.fizzed.blaze.postoffice;

import com.fizzed.blaze.Config;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.internal.ConfigHelper;
import com.fizzed.blaze.internal.ContextImpl;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.Message;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;

public class MailTest {

    private GreenMail greenMail;
    private Config config;
    private ContextImpl context;


    @Before
    public void before() throws Exception {
        this.greenMail = new GreenMail(ServerSetup.SMTP
            .dynamicPort());
        this.greenMail.withConfiguration(new GreenMailConfiguration()
            );
        this.greenMail.start();
        this.config = ConfigHelper.create(null);
        this.context = spy(new ContextImpl(null, null, Paths.get("blaze.java"), config));
    }

    @After
    public void after() throws Exception {
        this.greenMail.stop();
    }

    @Test
    public void noTextOrHtmlBody() throws Exception {
        try {
            new Mail(this.context)
                .smtpHost("localhost")
                .smtpPort(this.greenMail.getSmtp().getPort())
                .from("from@localhost")
                .to("henry@example.com")
                .subject("Test Email")
                .run();
            fail();
        } catch (BlazeException e) {
            // expected
        }
    }

    @Test
    public void textMail() throws Exception {
        new Mail(this.context)
            .smtpHost("localhost")
            .smtpPort(this.greenMail.getSmtp().getPort())
            .from("from@localhost")
            .to("henry@example.com")
            .subject("Test Email")
            .textBody("Hello World!")
            .run();

        MimeMessage[] receivedMessages = this.greenMail.getReceivedMessages();

        assertThat(receivedMessages.length, is(1));

        MimeMessage receivedMessage = receivedMessages[0];
        assertThat(receivedMessage.getFrom()[0].toString(), is("from@localhost"));
        assertThat(receivedMessage.getRecipients(Message.RecipientType.TO)[0].toString(), is("henry@example.com"));
        assertThat(receivedMessage.getSubject(), is("Test Email"));
        assertThat(receivedMessage.getContent(), instanceOf(MimeMultipart.class));

        MimeMultipart multipart = (MimeMultipart) receivedMessage.getContent();
        assertThat(multipart.getCount(), is(1));
        assertThat(multipart.getBodyPart(0).getContentType(), is("text/plain; charset=utf-8"));
        assertThat(multipart.getBodyPart(0).getContent(), is("Hello World!"));
    }

    @Test
    public void htmlMail() throws Exception {
        new Mail(this.context)
            .smtpHost("localhost")
            .smtpPort(this.greenMail.getSmtp().getPort())
            .from("from@localhost")
            .to("henry@example.com")
            .subject("Test Email")
            .htmlBody("<html>Hello World!</html>")
            .run();

        MimeMessage[] receivedMessages = this.greenMail.getReceivedMessages();

        assertThat(receivedMessages.length, is(1));

        MimeMessage receivedMessage = receivedMessages[0];
        assertThat(receivedMessage.getFrom()[0].toString(), is("from@localhost"));
        assertThat(receivedMessage.getRecipients(Message.RecipientType.TO)[0].toString(), is("henry@example.com"));
        assertThat(receivedMessage.getSubject(), is("Test Email"));
        assertThat(receivedMessage.getContent(), instanceOf(MimeMultipart.class));

        MimeMultipart multipart = (MimeMultipart) receivedMessage.getContent();
        assertThat(multipart.getCount(), is(1));
        assertThat(multipart.getBodyPart(0).getContentType(), is("text/html; charset=utf-8"));
        assertThat(multipart.getBodyPart(0).getContent(), is("<html>Hello World!</html>"));
    }

}