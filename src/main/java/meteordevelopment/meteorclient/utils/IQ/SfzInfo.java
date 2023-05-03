package meteordevelopment.meteorclient.utils.IQ;

public class SfzInfo {
    String success;
    Result result;

    public SfzInfo(String success, Result result) {
        this.success = success;
        this.result = result;
    }

    public String getSuccess() {
        return this.success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public Result getResult() {
        return this.result;
    }

    public void setResult(Result result) {
        this.result = result;
    }
}
