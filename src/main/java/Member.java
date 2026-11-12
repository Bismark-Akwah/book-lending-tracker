import java.util.ArrayList;
import java.util.List;

class Member {
    public String name;
    public String kind; // "STANDARD" or "STUDENT"
    public int id;
    public List<String> history = new ArrayList<>();
    static int counter = 1;

    public Member(String name, String kind) {
        this.id = counter;
        counter++;
        this.name = name;
        this.kind = kind;
    }
}