package de.mannheim.ids.korap.sru;

/**
 * @author margaretha
 *
 */
public class Annotation {

    private int id;
    private long start;
    private long end;
    private String value;
    private boolean isKeyword;

    public Annotation (int id, String value, long start, long end, boolean isKeyword) {
        this.id = id;
        this.value = value;
        this.start = start;
        this.end = end;
        this.isKeyword = isKeyword;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public boolean isKeyword() {
        return isKeyword;
    }

    public void setKeyword(boolean isKeyword) {
        this.isKeyword = isKeyword;
    }
}
