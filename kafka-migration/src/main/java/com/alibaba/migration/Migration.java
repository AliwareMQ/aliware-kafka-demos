package com.alibaba.migration;

import com.alibaba.migration.cmd.Cmd;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Set;
import org.reflections.Reflections;

public class Migration {

    @Parameter(names = "--help", description = "--help followed by CMD will show help for specific CMD", help = true)
    private String help = null;

    @Parameter(names = "--cmdList", help = true)
    private boolean cmdList = false;

    private Map<String, Cmd> cmdMap = Maps.newHashMap();

    public void run(String[] args) {
        JCommander jCommander = new JCommander(this);
        if(!initCmd(jCommander, args)){
            return;
        }

        if (help(jCommander)) {
            return;
        }
        if (list(jCommander)) {
            return;
        }
        run0(jCommander, args);
    }

    private void run0(JCommander jCommander, String[] args) {

        Cmd cmd = cmdMap.get(jCommander.getParsedCommand());
        if (cmd == null) {
            jCommander.usage();
        } else {
            cmd.run();
        }
    }

    private boolean list(JCommander jCommander) {
        if (cmdList) {
            for (String c : cmdMap.keySet()) {
                System.out.println(String.format("%16s - %s", c, jCommander.getCommandDescription(c)));
            }
            return true;
        }
        return false;
    }

    private boolean help(JCommander jCommander) {
        if (help != null) {
            JCommander cmd = jCommander.getCommands().get(help);
            if (cmd == null) {
                jCommander.usage();
            } else {
                cmd.usage();
            }
            return true;
        }
        return false;
    }

    private boolean initCmd(JCommander jCommander, String[] args) {
        for (Class<? extends Cmd> c : findClasses()) {
            try {
                if (c.isMemberClass()) {
                    continue;
                }
                Cmd cmd = c.newInstance();
                String cmdName = cmd.cmdName();
                Cmd cmdTask = cmd.newCmd();
                cmdMap.put(cmdName, cmdTask);
                jCommander.addCommand(cmdName, cmdTask);
            } catch (InstantiationException | IllegalAccessException ignored) {
            }
        }

        boolean parseSuccess;
        try {
            jCommander.parse(args);
            parseSuccess = true;
        } catch (Exception e) {
            System.out.println(String.format("Parse cmd failed. Exception: %s", e.getMessage()));
            if (null != jCommander.getParsedCommand()) {
                jCommander.getCommands().get(jCommander.getParsedCommand()).usage();
            } else {
                jCommander.usage();
            }
            parseSuccess = false;
        }
        return parseSuccess;
    }

    private Set<Class<? extends Cmd>> findClasses() {
        Reflections reflections = new Reflections(Cmd.class.getPackage().getName());
        return reflections.getSubTypesOf(Cmd.class);
    }
}
