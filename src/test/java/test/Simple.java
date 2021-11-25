package test;

import io.github.kloping.MySpringTool.StarterApplication;
import io.github.kloping.MySpringTool.annotations.AutoStand;
import io.github.kloping.MySpringTool.annotations.CommentScan;
import test.interfaces.M2;

@CommentScan(path = "test")
public class Simple {
    public static void main(String[] args) {
        StarterApplication.addConfFile("./src/test/java/conf.txt");
        StarterApplication.run(Simple.class);
    }

    @AutoStand
    static M2 m2;
}
