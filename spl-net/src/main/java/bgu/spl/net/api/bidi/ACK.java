
package bgu.spl.net.api.bidi;

public class ACK {

    //------Private Fields------
    private int opCode;
    private String optional;
    private boolean option;

    //------Public Constructors------
    public ACK(int opCode, String optional) {
        this.opCode = opCode;
        this.option = true;
        this.optional = optional;
    }

    public ACK(int opCode) {
        this.opCode = opCode;
        this.option = false;
    }

    //------Public Methods------

    public String createMessage() {

        if (option) {
            return "10" + opCode + " " + optional;
        }

        return "10 " + opCode;
    }
}