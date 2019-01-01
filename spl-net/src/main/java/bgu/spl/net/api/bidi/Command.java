package bgu.spl.net.api.bidi;

public class Command {
    private String Ans;
    protected int op_code;

    public String getAns() {
        return Ans;
    }

    public void setAns(String ans) {
        Ans = ans;
    }

    public int getOp_code() {
        return op_code;
    }
}