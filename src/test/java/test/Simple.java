package test;

import io.github.kloping.MySpringTool.StarterApplication;
import io.github.kloping.MySpringTool.annotations.AutoStand;
import io.github.kloping.MySpringTool.annotations.CommentScan;
import test.entitys.pvpQQH0.PvpQQH0;
import test.entitys.pvpQQVoice.PvpQQVoice;
import test.interfaces.M2;
import test.interfaces.PvpQq;

@CommentScan(path = "test")
public class Simple {
    public static void main(String[] args) {
        StarterApplication.addConfFile("./src/test/java/conf.txt");
        StarterApplication.run(Simple.class);

        PvpQQH0 v2 = pvpQq.get1("createHeroList");

        System.out.println("end");

    }

    public static String c1(String arg) {
        int i1 = arg.indexOf("(");
        int i2 = arg.lastIndexOf(")");
        return arg.substring(i1 + 1, i2);
    }

    @AutoStand
    static M2 m2;

    @AutoStand
    static PvpQq pvpQq;


}
