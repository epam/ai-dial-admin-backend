package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.InterceptorRunnerDto;
import com.epam.aidial.cfg.web.facade.InterceptorRunnerFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api/v1/interceptor-runners")
@Validated
@LogExecution
@RequiredArgsConstructor
public class InterceptorRunnerController {

    private final InterceptorRunnerFacade interceptorRunnerFacade;

    @GetMapping(produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<InterceptorRunnerDto> getAll() {
        return interceptorRunnerFacade.getAllInterceptorRunners();
    }

    @GetMapping(path = "/{interceptorRunnerName}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public InterceptorRunnerDto getInterceptorRunner(@PathVariable String interceptorRunnerName) {
        return interceptorRunnerFacade.getInterceptorRunner(interceptorRunnerName);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createInterceptorRunner(@RequestBody @Valid InterceptorRunnerDto interceptorRunnerDto) {
        interceptorRunnerFacade.createInterceptorRunner(interceptorRunnerDto);
    }

    @PutMapping(path = "/{interceptorRunnerName}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateInterceptorRunner(@PathVariable("interceptorRunnerName") String interceptorRunnerName,
                                        @RequestBody @Valid InterceptorRunnerDto interceptorRunnerDto) {
        interceptorRunnerFacade.updateInterceptorRunner(interceptorRunnerName, interceptorRunnerDto);
    }

    @DeleteMapping(path = "/{interceptorRunnerName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInterceptorRunner(@PathVariable("interceptorRunnerName") String interceptorRunnerName,
                                        @RequestParam(name = "removeInterceptor", required = false, defaultValue = "true") boolean removeInterceptor) {
        interceptorRunnerFacade.deleteInterceptorRunner(interceptorRunnerName, removeInterceptor);
    }

    @GetMapping(path = "/{interceptorRunnerName}/revision/{revision}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public InterceptorRunnerDto getSnapshot(@PathVariable String interceptorRunnerName, @PathVariable Integer revision) {
        return interceptorRunnerFacade.getSnapshot(interceptorRunnerName, revision);
    }

    @GetMapping(path = "/revision/{revision}", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<InterceptorRunnerDto> getAllAtRevision(@PathVariable Integer revision) throws Exception {
        return interceptorRunnerFacade.getAllAtRevision(revision);
    }
}