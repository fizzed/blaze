package com.fizzed.blaze;

import com.fizzed.blaze.postoffice.Mail;

public class PostOffices {

    static public Mail mail() {
        return new Mail(Contexts.currentContext());
    }

}