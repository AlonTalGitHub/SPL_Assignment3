
package bgu.spl.net.api.Commands;

import bgu.spl.net.api.bidi.Command;

public class ACK extends Command {

    //------Private Fields------
    protected int rOpCode; //The opCode of the handled message

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