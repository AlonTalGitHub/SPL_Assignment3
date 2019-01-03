package bgu.spl.net.api.bidi;

public class Error extends ServerToClient{

    //------Private Fields------
    private int rOpCode;

    //------Public Constructor------
    public Error(int rOpCode) {
        this.op_code = 11; //The opCode of ERROR
        this.rOpCode = rOpCode; //The opCode of the handled message
    }

    //------Public Methods------
    public int getrOpCode(){
        return rOpCode;
    }



}
