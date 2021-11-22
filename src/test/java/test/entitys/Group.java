package test.entitys;

public class Group {
    private Long id;
    private String nickName;

    public Group(Long id, String nickName) {
        this.id = id;
        this.nickName = nickName;
    }

    public long getId() {
        return id.longValue();
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}
