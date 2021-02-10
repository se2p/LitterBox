package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;

import java.util.ArrayList;
import java.util.List;

public class MessageNaming extends AbstractIssueFinder {
    public static final String NAME = "message_naming";
    private List<String> visitedNames;

    private static final String[] MESSAGE_LANGUAGES = {"ацҳамҭа", "boodskap", "መልእክት", "الرسالة", "ismarıc", "паведамленне",
            "съобщение", "missatge", "نامەی", "zpráva", "neges", "besked", "Nachricht", "μήνυμα", "message", "mensaje", "teade",
            "mezua", "پیام", "viesti", "teachtaireacht", "teachdaireachd", "mensaxe", "מסר", "poruka", "mesaj", "üzenet", "հաղորդագրություն",
            "pesan", "dæmiUmNafnÁSkilaboðum", "messaggio", "メッセージ", "შეტყობინება", "សារ", "메시지", "peyam", "žinutė", "ziņa", "karere",
            "мэссэж", "melding", "bericht", "molaetša", "ସନ୍ଦେଶ", "wiadomość", "mensagem", "Mensagem", "qillqa", "ki hāŋa", "mesaj",
            "сообщение", "správa", "sporočilo", "порука", "meddelande", "ujumbe", "ข้อความ", "molaetsa", "haber", "повідомлення", "xabar",
            "tin nhắn", "umyalezo", "消息", "umyalezo wokuqala"};

    @Override
    public void visit(Program node) {
        visitedNames = new ArrayList<>(node.getSymbolTable().getMessages().keySet());
        super.visit(node);
    }

    @Override
    public void visit(Broadcast node) {
        if (node.getMessage().getMessage() instanceof StringLiteral) {
            if (checkName(((StringLiteral) node.getMessage().getMessage()).getText())) {
                Hint hint = new Hint(getName());
                hint.setParameter(Hint.HINT_MESSAGE, ((StringLiteral) node.getMessage().getMessage()).getText());
                addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
            }
        }
    }

    @Override
    public void visit(BroadcastAndWait node) {
        if (node.getMessage().getMessage() instanceof StringLiteral) {
            if (checkName(((StringLiteral) node.getMessage().getMessage()).getText())) {
                Hint hint = new Hint(getName());
                hint.setParameter(Hint.HINT_MESSAGE, ((StringLiteral) node.getMessage().getMessage()).getText());
                addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
            }
        }
    }

    @Override
    public void visit(ReceptionOfMessage node) {
        if (node.getMsg().getMessage() instanceof StringLiteral) {
            if (checkName(((StringLiteral) node.getMsg().getMessage()).getText())) {
                Hint hint = new Hint(getName());
                hint.setParameter(Hint.HINT_MESSAGE, ((StringLiteral) node.getMsg().getMessage()).getText());
                addIssue(node, node.getMetadata(), IssueSeverity.LOW, hint);
            }
        }
    }

    private boolean checkName(String name) {
        String trimmedName = trimName(name);

        for (String standard : MESSAGE_LANGUAGES) {
            if (trimmedName.equals(standard)) {
                return true;
            }
        }
        for (String visitedName : visitedNames) {
            if (!name.equals(visitedName) && trimmedName.equals(trimName(visitedName))) {
                return true;
            }
        }
        return false;
    }

    private String trimName(String name) {
        String trimmedName = name;
        while (trimmedName.length() > 0 && (Character.isDigit(trimmedName.charAt(trimmedName.length() - 1))
                || Character.isWhitespace(trimmedName.charAt(trimmedName.length() - 1)))) {
            trimmedName = trimmedName.substring(0, trimmedName.length() - 1);
        }
        return trimmedName;
    }

    @Override
    public boolean isDuplicateOf(Issue first, Issue other) {
        if (first == other) {
            // Don't check against self
            return false;
        }

        if (first.getFinder() != other.getFinder()) {
            // Can only be a duplicate if it's the same finder
            return false;
        }

        String firstName = getText(first.getCodeLocation());
        String secondName = getText(other.getCodeLocation());

        return trimName(firstName).equals(trimName(secondName));
    }

    private String getText(ASTNode codeLocation) {
        if (codeLocation instanceof Broadcast) {
            StringExpr expr = ((Broadcast) codeLocation).getMessage().getMessage();
            return ((StringLiteral) expr).getText();
        } else if (codeLocation instanceof BroadcastAndWait) {
            StringExpr expr = ((BroadcastAndWait) codeLocation).getMessage().getMessage();
            return ((StringLiteral) expr).getText();
        } else {
            StringExpr expr = ((ReceptionOfMessage) codeLocation).getMsg().getMessage();
            return ((StringLiteral) expr).getText();
        }
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
