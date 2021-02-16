package nl.fannst.templates;

import freemarker.template.*;
import nl.fannst.mime.Address;
import nl.fannst.smtp.client.transactions.TransactionError;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;

public class FreeWriterRenderer {
    Configuration m_Configuration;

    private static FreeWriterRenderer INSTANCE;

    private FreeWriterRenderer() throws IOException {
        m_Configuration = new Configuration(new Version("2.3.28"));

        m_Configuration.setClassForTemplateLoading(getClass(), "/html/");
        m_Configuration.setDefaultEncoding("UTF-8");
        m_Configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        m_Configuration.setLogTemplateExceptions(false);
        m_Configuration.setWrapUncheckedExceptions(true);
        m_Configuration.setFallbackOnNullLoopVariable(false);
    }

    public static FreeWriterRenderer getInstance() {
        return INSTANCE;
    }

    public static void createInstance() throws IOException {
        INSTANCE = new FreeWriterRenderer();
    }

    private String renderTemplate(HashMap<String, Object> root, String file) throws IOException, TemplateException {
        Template template = m_Configuration.getTemplate(file);

        StringWriter stringWriter = new StringWriter();
        template.process(root, stringWriter);

        return stringWriter.toString();
    }

    public String renderPreTransactionFailure(String title, String message, Address sender) throws IOException, TemplateException {
        HashMap<String, Object> root = new HashMap<>();

        root.put("title", title);
        root.put("message", message);

        root.put("username", sender.getUsername());
        root.put("domain", sender.getDomain());

        return renderTemplate(root, "failures/pre-transaction-failure.ftl");
    }

    public String renderTransactionFailure(LinkedList<TransactionError> errors, Address sender) throws IOException, TemplateException {
        HashMap<String, Object> root = new HashMap<>();

        root.put("username", sender.getUsername());
        root.put("domain", sender.getDomain());

        ArrayList<HashMap<String, Object>> events = new ArrayList<>();
        for (TransactionError error : errors) {
            HashMap<String, Object> elem = new HashMap<>();
            elem.put("command", error.getTransactionName());
            elem.put("timestamp", error.getTimestamp());
            elem.put("message", error.getMessage());

            events.add(elem);
        }

        root.put("events", events);

        return renderTemplate(root, "failures/transaction-failure.ftl");
    }
}
