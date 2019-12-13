package com.github.masterdxy.light.dubbo.agent.bootstrap;

import org.springframework.boot.Banner;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.ansi.AnsiStyle;
import org.springframework.core.env.Environment;

import java.io.PrintStream;

/**
 * Created by tomoyo on 2017/7/4.
 */
public class DubboAgentBanner implements Banner {

    private static final String[] BANNER = {" _ __  _ __    ,___    _,                  \n" +
            "( /  )( /  )  /   /   / |               _/_\n" +
            " /--<  /--'  /       /--|  _,  _  _ _   /  \n" +
            "/   \\_/     (___/  _/   |_(_)_(/_/ / /_(__ \n" +
            "                           /|              \n" +
            "                          (/               "};

    private static final String RPCAGENT = " :: DubboAgent :: ";

    private static final int STRAP_LINE_SIZE = 42;

    @Override
    public void printBanner(Environment environment, Class<?> sourceClass,
                            PrintStream printStream) {
        for (String line : BANNER) {
            printStream.println(line);
        }
        String version = "1.0.0 RELEASE";
        version = " (v" + version + ")";
        String padding = "";
        while (padding.length() < STRAP_LINE_SIZE
                - (version.length() + RPCAGENT.length())) {
            padding += " ";
        }

        printStream.println(AnsiOutput.toString(AnsiColor.GREEN, RPCAGENT,
                AnsiColor.DEFAULT, padding, AnsiStyle.FAINT, version));
        printStream.println();
    }
}
