# FSMTP-V3
## SMTP/POP3 Server & Encrypted Database

### Functionality

#### SMTP \( Server \)

1. SMTP & ESMTP Commands \[_HELO_, _EHLO_, _MAIL_, _RCPT_, 
   _DATA_, _RSET_, _NOOP_, _HELP_, _VRFY_, _EXPN_, _BDAT_\]
1. SMTP & ESMTP Capabilities \[_BINARYMIME_, _PIPELINING_, 
   _CHUNKING_, _SMTPUTF8_, _ENHANCEDSTATUSCODES_, _AUTH_\]
1. Both SSL & Plain Non-Blocking IO servers on ports 465 and 25.
1. SPF and DMARC validation, including rejection based on DMARC policy.
1. Message Relaying, when an user is authenticated.

#### SMTP \( Client \)

1. Transaction-based SMTP/ESMTP client, which adopts to the target server.
1. Error feedback to the sender.
1. Non-Blocking IO, ofcourse.

#### POP3

1. POP3 Commands \[_USER_, _PASS_, _TOP_, _UIDL_, _CAPA_,
   _RETR_, _DELE_, _RSET_, _NOOP_, _STAT_, _LIST_\]    
1. POP3 Capabilities \[_RESP-CODES_, _AUTH-RESP-CODES_,
   _PIPELINING_\]
1. Both SSL & Plain Non-Blocking IO servers on port 995 and 110.

### Technologies of choice:

***Programming language***: Java \
***Database***: MongoDB \
***Templating Engine***: Apache FreeMarker

### Examples

#### POP3

```
+OK Fannst POP3 Secure server ready at 2021-02-16T17:05:04.208934

capa
+OK Capabilities follow.
EXPIRE NEVER
LOGIN-DELAY 0
TOP
UIDL
USER
RESP-CODES
AUTH-RESP-CODE
PIPELINING
IMPLEMENTATION Fannst POP3 by Luke A.C.A. Rieff
.

user ...[username]...
-ERR [AUTH] Domain not handled by this system.

user ...[username@domain]...
+OK Username accepted, supply password.

pass ...[invalid password]...
-ERR [AUTH] Invalid credentials.

pass ...[password]...
+OK Welcome back, Firstname Lastname

stat
+OK 87 29881392

list
+OK Mailbox listing follows.
1 7888
2 2364
3 1872561
4 10960692
5 10960736
6 104516
7 12043
8 1867
9 551
10 7957
...[more]...
.

uidl
+OK Mailbox listing follows.
1 4afe58fa-7871-4823-a1ae-0a3725a79c0b
2 470386c5-2ca0-4a92-ab56-869584300f01
3 12042fe6-2399-41df-91a2-e23d74ccc75b
4 e5e12433-7b9d-46e7-856e-e5541f6e19d3
5 f0681f2b-9f92-4074-9a1f-68f42ac407a1
6 79d14443-1c26-4376-97ff-169a45b6859e
7 4c83e267-cd75-47b4-b224-9bbab29ec56b
8 cc29a9e3-c924-4abd-aabe-b885cd789406
9 9a27c290-6f54-48e4-812e-7997dec62147
10 9b4a8540-1f35-4746-990a-6846d006e83d
...[more]...
.

top 10 0 0
-ERR Too many arguments.
top 10 0
+OK Message top follows.
...[headers]...
.

top 11 0
+OK Message top follows.
...[headers]...

--_----pcQcuyHPMhmYbiLabml5MQ===_D0/DF-44149-F14F3206
Content-Transfer-Encoding: quoted-printable
Content-Type: text/plain; charset="UTF-8"

...[data]...
--_----pcQcuyHPMhmYbiLabml5MQ===_D0/DF-44149-F14F3206
Content-Transfer-Encoding: quoted-printable
Content-Type: text/html; charset="UTF-8"

...[data]...
--_----pcQcuyHPMhmYbiLabml5MQ===_D0/DF-44149-F14F3206--


.

noop
+OK OK europe-mail-a001.fannst.nl.

rset
+OK OK flushed europe-mail-a001.fannst.nl.

stat
+OK 87 29881392

dele 99
-ERR Max index: 88

quit
+OK Fannst POP3 Server signing off.

```

#### SMTP

```
220 Fannst ESMTP Secure Ready at: 2021-02-16T17:09:54.068851 - fsmtp

ehlo s
250-OK, europe-mail-a001.fannst.nl at your service, (145.53.228.5) [145.53.228.5:41786]
250-ENHANCEDSTATUSCODES
250-CHUNKING
250-BINARYMIME
250-SIZE 16777216
250-PIPELINING
250-8BITMIME
250-SMTPUTF8
250 AUTH PLAIN

help
214-OK ( HELO MAIL RCPT DATA QUIT NOOP RSET EHLO HELP VRFY EXPN AUTH BDAT
214 XAUTHOR ) Fannst ESMTP Mail server: https://fannst.nl/mail - fsmtp

vrfy webmaster
250 2.0.0 Luke Rieff <webmaster@fannst.nl> - fsmtp

mail from:<luke.rieff@gmail.com>
250 2.4.5 OK, from: <luke.rieff@gmail.com> - fsmtp

rcpt to:<webmaster@fannst.nl>
250 2.4.5 OK, to: Luke Rieff <webmaster@fannst.nl> - fsmtp

data
354 2.0.0 End data with <CR><LF>.<CR><LF> - fsmtp

subject: hello world 
from: <luke.rieff@gmail.com>
to: <webmaster@fannst.nl>
content-type: text/plain

hello world

.
250 2.0.0 OK, transferred 122 bytes, with speed 0KB/s - fsmtp

rset
250 2.0.0 Flushed europe-mail-a001.fannst.nl. - fsmtp

quit
221 2.2.0 OK, closing transmission channel. - fsmtp

```

#### Message Composer

```html                                                                     
X-Mailer: Skynet Mailer, FSMTP-V3
MIME-Version: 1.0
Content-Type: multipart/alternative; 
      boundary="____MuK5sSpW44jAo6guTPvcnoc02u1VrNnbKFBK7FWUbO____"
Date: Tue, 16 Feb 2021 15:57:56 +0100 (CET)
From: Fannst Mail <delivery@skynet.fannst.nl>
To: <webmaster@fannst.nl>
Subject: Delivery Failure
Message-ID: D1fq6gii34pmU6lF1rBtrMGB6gNRgDgPp8wM_1613487476119@europe-mail-a001.fannst.nl
received: from europe-mail-a001.fannst.nl (vmi507012.contaboserver.net [207.180.225.138]) by europ
       e-mail-a001.fannst.nl with ESMTP; Tue, 16 Feb 2021 15:57:56 +0100 (CET)
x-fannst-spf: pass (europe-mail-a001.fannst.nl: 207.180.225.138 listed in SPF IPv4s) client-ip=207
       .180.225.138
x-fannst-dmarc: pass (europe-mail-a001.fannst.nl: null) client-ip=207.180.225.138
x-fannst-auth: europe-mail-a001.fannst.nl; dkim=pass (not implemented); 
      spf=pass (207.180.225.138 listed in SPF IPv4s); dmarc=pass (null)

--____MuK5sSpW44jAo6guTPvcnoc02u1VrNnbKFBK7FWUbO____
Content-Type: text/html
Content-Transfer-Encoding: quoted-printable

<=21DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://w=
ww.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns=3D"http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv=3D"Content-Type" content=3D"text/html; charset=3DUTF-8" /=
>
<title>Transaction Failure</title>
<meta name=3D"viewport" content=3D"width=3Ddevice-width, initial-scale=3D1=
.0"/>
</head>
<body>
<div class=3D"wrapper">
<div class=3D"wrapper__content">
<h1>Transaction failure for message.</h1>
<p>
The Fannst Servers failed to deliver your message ( in transaction ).
For more info, we've logged the events bellow:
</p>
<div class=3D"wrapper__content-failure">
<ul>
<li>
<div>
<span><strong>Timestamp:</strong> 1,613,487,476,083</span>
<span><strong>Command:</strong> RCPT TO:&lt;someone@example.com&=
gt;</span>
</div>
<p>
550 no mailbox by that name is currently available
</p>
</li>
</ul>
</div>
</div>
<style>
.footer {
margin-top: 40px;
text-align: center;
}
</style>
<div class=3D"footer">
<p>
This is an automated email sent to <a href=3D"mailto:webmaster@fannst.nl">=
webmaster@fannst.nl</a>
by the Fannst Mail Servers.
</p>
<hr />
<p>
<em>Fannst Mail is a product by <a href=3D"https://me.fannst.nl">Luke A.C.=
A. Rieff</a></em>
<br />
<small>Copyright Fannst 2021 - All Rights Reserved</small>
</p>
</div> </div>
</body>
</html>


--____MuK5sSpW44jAo6guTPvcnoc02u1VrNnbKFBK7FWUbO____--
```