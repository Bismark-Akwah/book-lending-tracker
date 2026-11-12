import java.util.ArrayList;
import java.util.List;

class Member {
    public String name;
    public MemberKind kind;
    public int id;
    public List<String> history = new ArrayList<>();
    static int counter = 1;

    public Member(String name, MemberKind kind) {
        this.id = counter;
        counter++;
        this.name = name;
        this.kind = kind;
    }
}