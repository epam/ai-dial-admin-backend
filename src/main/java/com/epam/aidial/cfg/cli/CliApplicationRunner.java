package com.epam.aidial.cfg.cli;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

@Component
@Profile("cli")
@RequiredArgsConstructor
public class CliApplicationRunner implements ApplicationRunner {

    private final DialAdminCommand dialAdminCommand;
    private final IFactory factory;

    @Override
    public void run(ApplicationArguments args) {
        int exitCode = new CommandLine(dialAdminCommand, factory)
                .execute(args.getSourceArgs());
        System.exit(exitCode);
    }
}
