
package bgu.spl.net.api.bidi;

public class ACK extends ServerToClient {

    //------Private Fields------
    private int rOpCode; //The opCode of the handled message
    private String optional;
    private boolean option;

    //------Public Constructors------
    public ACK(int rOpCode, String optional) {
        this.op_code = 10; //The opCode of ACK
        this.rOpCode = rOpCode; //The opCode of the handled message
        this.option = true;
        this.optional = optional;
    }

    public ACK(int opCode) {
        this.op_code = 10; //The opCode of ACK
        this.rOpCode = rOpCode; //The opCode of the handled message
        this.option = false;
    }

    //------Public Methods------
    public int getrOpCode() {
        return rOpCode;
    }

    public boolean isWithOptional() {
        return option == true;
    }

    public String getOptional() {
        return optional;
    }

}