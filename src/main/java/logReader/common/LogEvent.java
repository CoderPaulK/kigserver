package logReader.common;

/**
 * Created by noone_000 on 11/14/2015.
 */
public class LogEvent<T> {
    //Long time; // this is the key as well
    String dest;
    String source;
    String ability;
    String effect;
    T message;

    public String getAbility() { return ability; }

    public void setAbility(String ability) { this.ability = ability; }

    public String getEffect() { return effect; }

    public void setEffect(String effect) { this.effect = effect; }

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
    /*
    public Long getTime() { return time;}

    public void setTime(Long time) { this.time = time; }
    */
}
