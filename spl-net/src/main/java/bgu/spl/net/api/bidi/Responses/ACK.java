
package bgu.spl.net.api.bidi.Responses;

import bgu.spl.net.api.bidi.Response;

public class ACK extends Response {

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

    public ACK(int rOpCode) {
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