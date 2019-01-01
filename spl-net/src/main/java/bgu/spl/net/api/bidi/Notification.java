package bgu.spl.net.api.bidi;

public class Notification {

    //------Private Fields------
    private int opCode;
    private String kind;
    private String user;
    private String content;

    //------Public Constructor------
    public Notification(int opCode, String kind, String user, String content) {
        this.opCode = opCode;
        this.kind = kind;
        this.user = user;
        this.content = content;
    }

    //------Public Methods------
    public String createMessage() {
        return "9 " + " " + opCode + " " + kind + " " + user + " " + content;
    }

}



