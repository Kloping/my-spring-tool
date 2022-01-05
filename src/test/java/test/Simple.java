package test;

import io.github.kloping.MySpringTool.StarterApplication;
import io.github.kloping.MySpringTool.annotations.AutoStand;
import io.github.kloping.MySpringTool.annotations.CommentScan;
import test.entitys.pvpQQH0.PvpQQH0;
import test.interfaces.M2;
import test.interfaces.PvpQq;
import test.interfaces.SearchPics;

import java.util.Arrays;

@CommentScan(path = "test")
public class Simple {
    public static void main(String[] args) {
        StarterApplication.addConfFile("./src/test/java/conf.txt");
        StarterApplication.run(Simple.class);

        PvpQQH0 v2 = pvpQq.get1(null);

        System.out.println(m2.doc());

        System.out.println("end");

        System.out.println(Arrays.toString(searchPics.parsePic("https://v.kuaishou.com/hbGyGf", null)));
    }

    public static String c1(String arg) {
        int i1 = arg.indexOf("(");
        int i2 = arg.lastIndexOf(")");
        return arg.substring(i1 + 1, i2);
    }

    @AutoStand
    static SearchPics searchPics;

    @AutoStand
    static M2 m2;

    @AutoStand
    static PvpQq pvpQq;

}
