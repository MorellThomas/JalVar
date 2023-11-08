package javajs.util;

public class JSONException extends RuntimeException {
    public JSONException(String message) {
        super(message);
    }
    
    public JSONException(Throwable cause) {
        super(cause.getMessage());
        this.cause = cause;
    }

    public Throwable getCause() {
        return this.cause;
    }
}

}
