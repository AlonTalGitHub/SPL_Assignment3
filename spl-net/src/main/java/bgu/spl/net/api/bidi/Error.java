package bgu.spl.net.api.bidi;

public class Error {

    //------Private Fields------
    private int opCode;

    //------Public Constructor------
    public Error(int opCode) {
        this.opCode = opCode;
    }

    //------Public Methods------
    public String createMessage() { //TODO: return T???
        return "11 " + opCode;
    }



}
