package de.mannheim.ids.korap.sru;

/** 
 * 
 * @author margaretha
 *
 */
public class Annotation {

    private long start;
    private long end;
    private String value;
    private int hitLevel;
    
    public Annotation (String value, long start, long end, int hitLevel) {
        this.value = value;
        this.start = start;
        this.end = end;
        this.hitLevel = hitLevel;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getHitLevel() {
        return hitLevel;
    }
    
    public void setHitLevel(int hitLevel) {
        this.hitLevel = hitLevel;
    }
}
