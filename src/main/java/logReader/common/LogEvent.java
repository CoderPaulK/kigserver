package logReader.common;

/**
 * Created by noone_000 on 11/14/2015.
 */
public class LogEvent<T> {
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public T getMessage() {
        return message;
    }

    public void setMessage(T message) {
        this.message = message;
    }


    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }
    String dest;
    String source;
    T message;
}
