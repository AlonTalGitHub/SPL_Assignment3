
package bgu.spl.net.api.bidi.Responses;

import bgu.spl.net.api.bidi.Response;

public class ACK extends Response {

    //------Private Fields------
    protected int rOpCode; //The opCode of the handled message
    protected boolean optional;

    //------Public Constructors------
    public ACK() {
        this.op_code = 10; //The opCode of ACK
    }

    public ACK(int rOpCode) {
        this.rOpCode = rOpCode;
    }

    //------Public Methods------
    public int getrOpCode() {
        return rOpCode;
    }


}