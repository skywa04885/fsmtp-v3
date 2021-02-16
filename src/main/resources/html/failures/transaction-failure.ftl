<#outputformat "HTML">
<#compress>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>Transaction Failure</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    </head>
    <body>
        <div class="wrapper">
            <div class="wrapper__content">
                <h1>Transaction failure for message.</h1>
                <p>
                    The Fannst Servers failed to deliver your message ( in transaction ).
                    For more info, we've logged the events bellow:
                </p>
                <div class="wrapper__content-failure">
                    <ul>
                        <#list events as event>
                            <li>
                                <div>
                                    <span><strong>Timestamp:</strong> ${event.timestamp}</span>
                                    <span><strong>Command:</strong> ${event.command}</span>
                                </div>
                                <p>
                                    ${event.message}
                                </p>
                            </li>
                        </#list>
                    </ul>
                </div>
            </div>
            <#include "../footer.ftl">
        </div>
    </body>
</html>
</#compress>
</#outputformat>