package com.epam.aidial.cfg.cli;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(
        name = "dial-admin",
        mixinStandardHelpOptions = true,
        description = "DIAL Admin CLI tool",
        subcommands = {ValidateCommand.class}
)
public class DialAdminCommand implements Runnable {

    @Override
    public void run() {
        // no subcommand given — print usage (handled by picocli)
    }
}
